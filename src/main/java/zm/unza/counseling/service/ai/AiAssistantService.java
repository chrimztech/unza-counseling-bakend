package zm.unza.counseling.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zm.unza.counseling.entity.User;

import java.util.*;
import java.util.regex.Pattern;

/**
 * The app's AI features, built on {@link AiGatewayService}: the student wellness chat and
 * counselor session-note drafting. Prompts live here so web and mobile behave identically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private static final List<Pattern> CRISIS_PATTERNS = List.of(
            Pattern.compile("suicid", Pattern.CASE_INSENSITIVE),
            Pattern.compile("kill(ing)? my ?self", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(hurt|harm)(ing)? my ?self", Pattern.CASE_INSENSITIVE),
            Pattern.compile("self[- ]?harm", Pattern.CASE_INSENSITIVE),
            Pattern.compile("end (it all|my life)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(want|wish) (to|i could) die", Pattern.CASE_INSENSITIVE),
            Pattern.compile("can'?t go on", Pattern.CASE_INSENSITIVE),
            Pattern.compile("no reason to live", Pattern.CASE_INSENSITIVE),
            Pattern.compile("better off dead", Pattern.CASE_INSENSITIVE),
            // Indirect expressions of suicidal thinking (the wording students most often use).
            Pattern.compile("better off without me", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(no ?one|nobody) (would|will) (miss|care|notice)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(don'?t|do not|no longer) want to (be here|live|exist|wake up|be alive)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(tired|sick) of (living|being alive|life)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("wish i (was|were) (dead|never born)|wish i had never been born", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(take|swallow|took) all (my|the|of my) (pills|tablets|medication)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(disappear|go to sleep) (forever|and never wake)", Pattern.CASE_INSENSITIVE)
    );

    // The wellness system prompt is assembled per turn by AiKnowledgeService (base rules + campus
    // services + crisis contacts + category guidance + retrieved counsellor scenarios).

    static final String SESSION_NOTES_SYSTEM_PROMPT = """
            You are a clinical documentation assistant for counselors at the UNZA Counseling Centre.
            Turn the counselor's rough session input into a concise, professional session note.
            Use neutral, objective language. Do not invent facts that were not provided; if something is \
            unknown, leave it out. Do not diagnose.
            """;

    private final AiGatewayService gateway;
    private final ObjectMapper mapper;
    private final AiKnowledgeService knowledge;
    private final AiStudentMemoryService memory;

    public boolean isAvailable() {
        return gateway.isAvailable();
    }

    public static boolean detectCrisis(String text) {
        if (text == null) return false;
        return CRISIS_PATTERNS.stream().anyMatch(p -> p.matcher(text).find());
    }

    /**
     * Wellness chat reply. Context: the long-term memory summary and the person's own records
     * (via {@link AiStudentMemoryService}), every not-yet-summarised exchange verbatim, and the
     * counsellor-written scenarios most similar to what they said.
     */
    /** The reply plus a step-by-step account of how it was produced (for the "How I worked this out" panel). */
    public record WellnessReply(AiResult result, List<Map<String, Object>> trace) {}

    public WellnessReply wellnessReply(User user, String sessionId, String message, boolean crisisDetected) {
        List<AiMessage> convo = new ArrayList<>(memory.verbatimHistory(user.getId(), sessionId));
        int verbatimTurns = convo.size();
        String previousUser = null;
        for (int i = convo.size() - 1; i >= 0; i--) {
            if ("user".equals(convo.get(i).role())) { previousUser = convo.get(i).content(); break; }
        }
        AiKnowledgeService.WellnessPrompt prompt =
                knowledge.buildWellnessSystemPrompt(message, previousUser, memory.personalContext(user));
        String userTurn = message;
        if (crisisDetected) {
            // Belt and braces: the system prompt already covers this, but make it explicit for this turn.
            userTurn = message + "\n\n[System note: this message may indicate a risk of self-harm. "
                    + "Prioritise the person's immediate safety in your reply.]";
        }
        convo.add(AiMessage.user(userTurn));
        AiResult result = gateway.chat(prompt.systemPrompt(), convo);
        return new WellnessReply(result, buildTrace(user, verbatimTurns, prompt, crisisDetected, result));
    }

    /**
     * A truthful, student-readable account of the steps taken for this reply. It names what was
     * used (kinds of records, categories of counsellor scenarios, the model) but never values,
     * scores or counsellor-written text.
     */
    private List<Map<String, Object>> buildTrace(User user, int verbatimTurns, AiKnowledgeService.WellnessPrompt prompt,
                                                 boolean crisis, AiResult result) {
        List<Map<String, Object>> steps = new ArrayList<>();
        steps.add(step("safety", "Checked your message for safety concerns",
                crisis ? "Signs that you may be going through something serious — a counsellor has been asked to reach out to you."
                       : "Nothing urgent detected.", crisis ? "alert" : "done"));

        Map<String, Object> info = memory.traceInfo(user, verbatimTurns);
        String mem = (String) info.get("memory");
        steps.add(step("memory", "Recalled our earlier conversations",
                mem != null ? "Used " + mem + "." : "This is the start of our conversation.", mem != null ? "done" : "skipped"));

        @SuppressWarnings("unchecked") List<String> records = (List<String>) info.getOrDefault("records", List.of());
        String recordsNote = (String) info.get("recordsNote");
        steps.add(step("records", "Looked at your wellbeing records",
                recordsNote != null ? recordsNote : records.isEmpty() ? "No records yet." : String.join("; ", records) + ".",
                recordsNote != null ? "skipped" : "done"));

        List<String> categories = prompt.examples().stream()
                .map(e -> e.scenario().getProblemCategory()).filter(Objects::nonNull).distinct().toList();
        int n = prompt.examples().size();
        steps.add(step("knowledge", "Compared with situations UNZA counsellors have handled",
                n == 0 ? "No close match, so I used the Counselling Centre's general guidance."
                       : "Found " + n + " similar situation" + (n == 1 ? "" : "s")
                         + (categories.isEmpty() ? "" : " (" + String.join(", ", categories) + ")")
                         + " to guide my tone and suggestions.", n == 0 ? "skipped" : "done"));

        steps.add(step("guidance", "Applied UNZA counselling guidance and safety rules",
                "Kept to supportive, non-judgemental advice, pointed to real UNZA services, and didn't diagnose.", "done"));

        // Deliberately no provider/model/timing here: students don't need vendor details
        // (admins see those under AI Providers).
        steps.add(step("reply", "Wrote this reply",
                "Brought it all together into a reply written for you, in plain and supportive language.", "done"));
        return steps;
    }

    private static Map<String, Object> step(String key, String title, String detail, String status) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("key", key);
        m.put("title", title);
        m.put("detail", detail);
        m.put("status", status);   // done | skipped | alert
        return m;
    }

    /** Free-text session-note draft (used by the mobile "AI Session Notes" screen). */
    public AiResult sessionNoteFromText(String counselorInput) {
        return gateway.chat(SESSION_NOTES_SYSTEM_PROMPT + """

                Format the note with these headings: Summary, Key Points, Interventions, Client Response, \
                Follow-up, Recommendations.
                """, List.of(AiMessage.user(counselorInput)), 1500);
    }

    /** Structured session-note draft (used by the web AISessionNoteGenerator). */
    public Map<String, Object> structuredSessionNote(Map<String, Object> sessionData) {
        String input;
        try {
            input = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sessionData);
        } catch (Exception e) {
            input = String.valueOf(sessionData);
        }
        AiResult r = gateway.chat(SESSION_NOTES_SYSTEM_PROMPT + """

                Respond with ONLY a JSON object (no markdown fences) of this exact shape:
                {"summary": string, "keyPoints": string[], "interventions": string[], \
                "clientResponses": string[], "followUp": string[], "recommendations": string[]}
                """, List.of(AiMessage.user("Session input:\n" + input)), 1500);

        Map<String, Object> out = new LinkedHashMap<>();
        JsonNode json = extractJson(r.content());
        if (json != null && json.isObject()) {
            out.put("summary", json.path("summary").asText(""));
            for (String k : List.of("keyPoints", "interventions", "clientResponses", "followUp", "recommendations")) {
                List<String> items = new ArrayList<>();
                json.path(k).forEach(n -> items.add(n.asText()));
                out.put(k, items);
            }
        } else {
            // Model ignored the format — still give the counselor the text rather than failing.
            out.put("summary", r.content());
            for (String k : List.of("keyPoints", "interventions", "clientResponses", "followUp", "recommendations")) {
                out.put(k, List.of());
            }
        }
        out.put("provider", r.providerName());
        out.put("model", r.model());
        return out;
    }

    private JsonNode extractJson(String text) {
        if (text == null) return null;
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) return null;
        try {
            return mapper.readTree(text.substring(start, end + 1));
        } catch (Exception e) {
            log.debug("AI session note was not valid JSON: {}", e.getMessage());
            return null;
        }
    }
}
