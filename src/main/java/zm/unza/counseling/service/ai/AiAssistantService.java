package zm.unza.counseling.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zm.unza.counseling.entity.ChatMessage;
import zm.unza.counseling.repository.ChatMessageRepository;

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

    /** How many previous exchanges (user+assistant pairs) are sent as conversation context. */
    private static final int HISTORY_EXCHANGES = 10;

    private static final List<Pattern> CRISIS_PATTERNS = List.of(
            Pattern.compile("suicid", Pattern.CASE_INSENSITIVE),
            Pattern.compile("kill(ing)? my ?self", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(hurt|harm)(ing)? my ?self", Pattern.CASE_INSENSITIVE),
            Pattern.compile("self[- ]?harm", Pattern.CASE_INSENSITIVE),
            Pattern.compile("end (it all|my life)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(want|wish) (to|i could) die", Pattern.CASE_INSENSITIVE),
            Pattern.compile("can'?t go on", Pattern.CASE_INSENSITIVE),
            Pattern.compile("no reason to live", Pattern.CASE_INSENSITIVE),
            Pattern.compile("better off dead", Pattern.CASE_INSENSITIVE)
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
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper mapper;
    private final AiKnowledgeService knowledge;

    public boolean isAvailable() {
        return gateway.isAvailable();
    }

    public static boolean detectCrisis(String text) {
        if (text == null) return false;
        return CRISIS_PATTERNS.stream().anyMatch(p -> p.matcher(text).find());
    }

    /**
     * Wellness chat reply with the user's recent history in this session as context, grounded in
     * the counsellor-written scenarios most similar to what the student said.
     */
    public AiResult wellnessReply(Long userId, String sessionId, String message, boolean crisisDetected) {
        List<AiMessage> convo = new ArrayList<>(recentHistory(userId, sessionId));
        String previousUser = null;
        for (int i = convo.size() - 1; i >= 0; i--) {
            if ("user".equals(convo.get(i).role())) { previousUser = convo.get(i).content(); break; }
        }
        String systemPrompt = knowledge.buildWellnessSystemPrompt(message, previousUser).systemPrompt();
        String userTurn = message;
        if (crisisDetected) {
            // Belt and braces: the system prompt already covers this, but make it explicit for this turn.
            userTurn = message + "\n\n[System note: this message may indicate a risk of self-harm. "
                    + "Prioritise the person's immediate safety in your reply.]";
        }
        convo.add(AiMessage.user(userTurn));
        return gateway.chat(systemPrompt, convo);
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

    private List<AiMessage> recentHistory(Long userId, String sessionId) {
        List<ChatMessage> rows = chatMessageRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<ChatMessage> recent = rows.stream()
                .filter(m -> Objects.equals(sessionId, m.getSessionId()))
                .limit(HISTORY_EXCHANGES)
                .toList();
        List<AiMessage> out = new ArrayList<>();
        for (int i = recent.size() - 1; i >= 0; i--) {            // oldest first
            ChatMessage m = recent.get(i);
            if (m.getMessage() != null && !m.getMessage().isBlank()) out.add(AiMessage.user(m.getMessage()));
            if (m.getResponse() != null && !m.getResponse().isBlank()) out.add(AiMessage.assistant(m.getResponse()));
        }
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
