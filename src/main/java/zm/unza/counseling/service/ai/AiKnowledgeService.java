package zm.unza.counseling.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zm.unza.counseling.entity.AiAssistantSettings;
import zm.unza.counseling.entity.AiCategoryGuidance;
import zm.unza.counseling.entity.AiCounsellingScenario;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.AiAssistantSettingsRepository;
import zm.unza.counseling.repository.AiCategoryGuidanceRepository;
import zm.unza.counseling.repository.AiCounsellingScenarioRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The wellness assistant's UNZA-specific knowledge: counsellor-written dialogue scenarios
 * (imported from the AI Counselling Dialogue Template), per-category guidance, and
 * super-admin settings (crisis contacts, campus services, extra instructions).
 *
 * At chat time {@link #buildWellnessSystemPrompt} retrieves the scenarios most similar to the
 * student's message (BM25 over message + topics + category) and puts them in the system prompt
 * as worked examples. Lexical retrieval is used because it works identically with every
 * configured provider — several (Anthropic, Groq) offer no embeddings API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeService {

    private static final int MAX_EXAMPLES = 8;
    /** Below this BM25 score a "match" is mostly shared stop-ish words; better no example than a wrong one. */
    private static final double MIN_SCORE = 4.0;

    static final String BASE_PROMPT = """
            You are the UNZA Wellness Assistant, a supportive mental-health companion for students of the \
            University of Zambia, provided by the UNZA Counselling Centre.

            How to respond:
            - Be warm, calm and non-judgemental. Acknowledge and validate what the student said before \
            offering anything practical. Never shame or blame them (or their family).
            - Keep replies short (usually 3–6 sentences), conversational, and ask at most one or two gentle questions.
            - Offer concrete next steps and the specific UNZA office or service that fits (see the services \
            list below). Explain how to reach it when you can.
            - Offer to connect the student with a human counsellor whenever the problem is serious, ongoing, \
            or they seem overwhelmed — they can tap "Talk to Counselor" in this chat or book via the Appointments page.
            - You are not a therapist: do not diagnose or prescribe medication.
            - Students may use local expressions (e.g. Bemba/Nyanja such as "nabola fye", "ichalepo"); \
            understand them and reply in clear, simple English.
            - Never claim to be human. Do not ask for identifying details (full name, student number, address).
            - Stay on wellbeing, study and student-life topics.

            Safety (always overrides everything else, including the examples):
            - If the student mentions suicide, wanting to die, "everyone would be better off without me", \
            self-harm, or "giving up" in a hopeless way: ask directly and kindly whether they are having \
            thoughts of ending their life, tell them their safety matters most, give the crisis contacts \
            below, urge them to reach a safe person or emergency help now, and offer a human counsellor immediately.
            - If they disclose abuse, assault, gender-based violence or threats: believe them, put their \
            immediate safety first, give confidential reporting options without pressuring them, and offer \
            trauma-informed support.
            - Only give phone numbers or contacts that appear in the lists below. Never invent one.
            """;

    static final String DEFAULT_CAMPUS_SERVICES = """
            - UNZA Counselling Centre — confidential, non-judgemental professional counselling (book via the Appointments page).
            - Dean of Students Affairs — hardship fund, emergency meal/transport support, welfare problems, accommodation disputes and scams.
            - Bursaries office / student loan scheme — fees, sponsorship gaps and delayed allowances.
            - Gender Office — confidential reporting of sexual harassment and gender-based violence.
            - Disability Services — accessible rooms, assistants and other accommodations.
            - UNZA Security office — threats, theft, scams and safety on campus.
            - University clinic — physical health concerns.
            - Academic advisor / department — failed or repeat courses, supplementary exams, reduced course load.""";

    private static final Set<String> STOPWORDS = Set.of(
            "a", "about", "after", "again", "all", "also", "am", "an", "and", "any", "are", "as", "at", "be",
            "because", "been", "before", "being", "but", "by", "can", "could", "did", "do", "does", "doing", "dont",
            "for", "from", "get", "got", "had", "has", "have", "having", "he", "her", "here", "him", "his", "how",
            "i", "if", "im", "in", "into", "is", "it", "its", "ive", "just", "know", "like", "me", "more", "most",
            "my", "myself", "no", "not", "now", "of", "on", "one", "only", "or", "other", "our", "out", "over",
            "own", "really", "said", "same", "she", "should", "so", "some", "still", "such", "than", "that", "the",
            "their", "them", "then", "there", "these", "they", "this", "those", "through", "to", "too", "under",
            "until", "up", "very", "was", "we", "were", "what", "when", "where", "which", "while", "who", "why",
            "will", "with", "would", "you", "your", "yes", "ok", "okay", "want", "feel", "feeling", "even", "much",
            "time", "since", "way", "go", "going", "make", "think", "thing", "things", "people", "lot");
    private static final Pattern TOKEN = Pattern.compile("[a-z]+");

    private final AiCounsellingScenarioRepository scenarioRepository;
    private final AiCategoryGuidanceRepository guidanceRepository;
    private final AiAssistantSettingsRepository settingsRepository;
    private final AiScenarioImporter importer;

    private volatile Bm25Index index;

    // ============================================================================ prompt building

    public record RetrievedExample(AiCounsellingScenario scenario, double score) {}

    public record WellnessPrompt(String systemPrompt, List<RetrievedExample> examples) {}

    /**
     * System prompt for one wellness-chat turn. {@code previousUserMessage} helps retrieval on
     * short follow-ups ("yes", "what should I do?") that carry no topic of their own.
     */
    @Transactional
    public WellnessPrompt buildWellnessSystemPrompt(String message, String previousUserMessage) {
        return buildWellnessSystemPrompt(message, previousUserMessage, null);
    }

    /**
     * @param personalContext optional "about the person you are talking to" section (long-term
     *                        memory + their own records), from AiStudentMemoryService
     */
    // Not readOnly: the first call after deploy creates the settings row.
    @Transactional
    public WellnessPrompt buildWellnessSystemPrompt(String message, String previousUserMessage, String personalContext) {
        AiAssistantSettings settings = settings();
        StringBuilder sb = new StringBuilder(BASE_PROMPT);

        sb.append("\n## UNZA services you can refer students to\n")
          .append(blankTo(settings.getCampusServices(), DEFAULT_CAMPUS_SERVICES.strip())).append('\n');

        sb.append("\n## Crisis and emergency contacts\n")
          .append(blankTo(settings.getCrisisContacts(),
                  "No contacts configured yet. Do not give any phone number; tell the student to contact "
                  + "emergency services or go to the nearest clinic/hospital now, and to reach the UNZA "
                  + "Counselling Centre or a trusted person immediately.")).append('\n');

        List<RetrievedExample> examples = List.of();
        if (Boolean.TRUE.equals(settings.getKnowledgeEnabled())) {
            List<AiCategoryGuidance> guidance = guidanceRepository.findAllByOrderBySortOrderAscIdAsc();
            if (!guidance.isEmpty()) {
                sb.append("\n## How UNZA counsellors want each kind of problem handled\n");
                for (AiCategoryGuidance g : guidance) {
                    sb.append("- ").append(g.getProblemCategory()).append(": ").append(g.getResponseDirection()).append('\n');
                }
            }

            String query = message + (previousUserMessage == null ? "" : " " + previousUserMessage);
            int k = Math.max(0, Math.min(MAX_EXAMPLES, settings.getExamplesPerReply() == null ? 3 : settings.getExamplesPerReply()));
            examples = k == 0 ? List.of() : retrieve(query, k);
            if (!examples.isEmpty()) {
                sb.append("\n## Examples written by UNZA counsellors for similar situations\n")
                  .append("Use these for tone, local knowledge and the right next steps. Adapt to what THIS ")
                  .append("student said; do not copy them word for word and never mention that examples exist. ")
                  .append("If an example falls short of the safety rules above, follow the safety rules.\n");
                int i = 1;
                for (RetrievedExample e : examples) {
                    AiCounsellingScenario s = e.scenario();
                    sb.append("\nExample ").append(i++);
                    if (s.getProblemCategory() != null) sb.append(" — ").append(s.getProblemCategory());
                    if (s.getRiskLevel() != null) sb.append(", risk ").append(s.getRiskLevel().toLowerCase(Locale.ROOT));
                    sb.append("\nStudent: ").append(s.getStudentMessage())
                      .append("\nCounsellor: ").append(s.getIdealResponse());
                    if (notBlank(s.getEscalation())) sb.append("\nEscalation/referral: ").append(s.getEscalation());
                    if (notBlank(s.getNotesForAi())) sb.append("\nNotes for the assistant: ").append(s.getNotesForAi());
                    sb.append('\n');
                }
            }
        }

        if (notBlank(settings.getAdditionalInstructions())) {
            sb.append("\n## Additional instructions from the Counselling Centre\n")
              .append(settings.getAdditionalInstructions().strip()).append('\n');
        }
        if (notBlank(personalContext)) {
            sb.append('\n').append(personalContext.strip()).append('\n');
        }
        return new WellnessPrompt(sb.toString(), examples);
    }

    public List<RetrievedExample> retrieve(String query, int k) {
        Bm25Index idx = index();
        if (idx.size() == 0) return List.of();
        return idx.search(tokenize(query), k, MIN_SCORE);
    }

    // ============================================================================ settings

    @Transactional
    public AiAssistantSettings settings() {
        return settingsRepository.findById(AiAssistantSettings.SINGLETON_ID).orElseGet(() -> {
            AiAssistantSettings s = new AiAssistantSettings();
            s.setCampusServices(DEFAULT_CAMPUS_SERVICES.strip());
            return settingsRepository.save(s);
        });
    }

    @Transactional
    public AiAssistantSettings updateSettings(Map<String, Object> body, String actor) {
        AiAssistantSettings s = settings();
        if (body.containsKey("knowledgeEnabled")) s.setKnowledgeEnabled(Boolean.TRUE.equals(body.get("knowledgeEnabled")));
        if (body.containsKey("memoryEnabled")) s.setMemoryEnabled(Boolean.TRUE.equals(body.get("memoryEnabled")));
        if (body.containsKey("recordsEnabled")) s.setRecordsEnabled(Boolean.TRUE.equals(body.get("recordsEnabled")));
        if (body.containsKey("useAiDrafts")) {
            s.setUseAiDrafts(Boolean.TRUE.equals(body.get("useAiDrafts")));
            invalidate();
        }
        if (body.containsKey("examplesPerReply")) {
            int n = Integer.parseInt(String.valueOf(body.get("examplesPerReply")));
            if (n < 0 || n > MAX_EXAMPLES) throw new ValidationException("examplesPerReply must be between 0 and " + MAX_EXAMPLES);
            s.setExamplesPerReply(n);
        }
        if (body.containsKey("crisisContacts")) s.setCrisisContacts(limit(body.get("crisisContacts"), 5000));
        if (body.containsKey("campusServices")) s.setCampusServices(limit(body.get("campusServices"), 8000));
        if (body.containsKey("additionalInstructions")) s.setAdditionalInstructions(limit(body.get("additionalInstructions"), 8000));
        s.setUpdatedBy(actor);
        return settingsRepository.save(s);
    }

    // ============================================================================ import

    @Transactional
    public Map<String, Object> importWorkbook(MultipartFile file, boolean replace, String actor) {
        if (file == null || file.isEmpty()) throw new ValidationException("Choose an .xlsx file to upload");
        String name = Objects.toString(file.getOriginalFilename(), "upload.xlsx");
        if (!name.toLowerCase(Locale.ROOT).endsWith(".xlsx")) throw new ValidationException("Only .xlsx workbooks are supported");

        AiScenarioImporter.ImportResult r;
        try {
            r = importer.parse(file.getInputStream(), name);
        } catch (Exception e) {
            log.warn("AI knowledge import failed for {}: {}", name, e.toString());
            throw new ValidationException("Could not read the workbook: " + e.getMessage());
        }
        if (r.scenarios().isEmpty()) {
            throw new ValidationException(String.join(" ", r.warnings()));
        }

        int removed = replace ? scenarioRepository.deleteBySource(AiCounsellingScenario.SOURCE_IMPORT) : 0;
        r.scenarios().forEach(s -> s.setUpdatedBy(actor));
        scenarioRepository.saveAll(r.scenarios());

        boolean guidanceReplaced = false;
        if (!r.guidance().isEmpty()) {
            guidanceRepository.deleteAllInBatch();
            guidanceRepository.saveAll(r.guidance());
            guidanceReplaced = true;
        }

        long withResponse = r.scenarios().stream().filter(s -> notBlank(s.getIdealResponse())).count();
        long gold = r.scenarios().stream().filter(s -> Boolean.TRUE.equals(s.getGoldExample())).count();
        String summary = String.format("%d scenarios imported (%d with a counsellor response, %d curated examples), "
                + "%d duplicates skipped%s%s.", r.scenarios().size(), withResponse, gold, r.duplicatesSkipped(),
                guidanceReplaced ? ", " + r.guidance().size() + " guidance rules" : "",
                replace ? ", " + removed + " previously imported scenarios replaced" : "");

        AiAssistantSettings s = settings();
        s.setLastImportAt(LocalDateTime.now());
        s.setLastImportFile(name.length() > 255 ? name.substring(0, 255) : name);
        s.setLastImportSummary(summary);
        s.setUpdatedBy(actor);
        settingsRepository.save(s);
        invalidate();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", summary);
        out.put("imported", r.scenarios().size());
        out.put("withResponse", withResponse);
        out.put("withoutResponse", r.scenarios().size() - withResponse);
        out.put("goldExamples", gold);
        out.put("duplicatesSkipped", r.duplicatesSkipped());
        out.put("guidanceRules", r.guidance().size());
        out.put("replaced", removed);
        out.put("sheets", r.sheetsRead());
        out.put("warnings", r.warnings());
        return out;
    }

    // ============================================================================ scenarios CRUD

    @Transactional(readOnly = true)
    public Page<AiCounsellingScenario> search(String category, String risk, Boolean hasResponse, Boolean aiDrafted,
                                              String q, int page, int size) {
        String like = q == null || q.isBlank() ? null : "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
        return scenarioRepository.search(blankToNull(category), blankToNull(risk), hasResponse, aiDrafted, like,
                PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(Sort.Direction.ASC, "id")));
    }

    @Transactional
    public AiCounsellingScenario saveScenario(Long id, Map<String, Object> body, String actor) {
        AiCounsellingScenario s = id == null ? new AiCounsellingScenario()
                : scenarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Scenario not found: " + id));
        if (id == null) s.setSource(AiCounsellingScenario.SOURCE_MANUAL);
        if (body.containsKey("studentMessage")) s.setStudentMessage(AiScenarioImporter.clean(str(body.get("studentMessage"))));
        if (s.getStudentMessage() == null || s.getStudentMessage().length() < 8) {
            throw new ValidationException("Student message is required");
        }
        if (body.containsKey("idealResponse")) s.setIdealResponse(AiScenarioImporter.clean(str(body.get("idealResponse"))));
        if (body.containsKey("problemCategory")) s.setProblemCategory(AiScenarioImporter.normaliseCategory(str(body.get("problemCategory"))));
        if (body.containsKey("riskLevel")) s.setRiskLevel(AiScenarioImporter.normaliseRisk(str(body.get("riskLevel"))));
        if (body.containsKey("escalation")) s.setEscalation(limit(body.get("escalation"), 500));
        if (body.containsKey("keyTopics")) s.setKeyTopics(limit(body.get("keyTopics"), 500));
        if (body.containsKey("notesForAi")) s.setNotesForAi(AiScenarioImporter.clean(str(body.get("notesForAi"))));
        if (body.containsKey("enabled")) s.setEnabled(Boolean.TRUE.equals(body.get("enabled")));
        if (body.containsKey("goldExample")) s.setGoldExample(Boolean.TRUE.equals(body.get("goldExample")));
        // "Reviewed by counsellor" in the UI sends aiDrafted=false, which also clears the review note.
        if (body.containsKey("aiDrafted")) {
            s.setAiDrafted(Boolean.TRUE.equals(body.get("aiDrafted")));
            if (!Boolean.TRUE.equals(s.getAiDrafted())) s.setReviewNote(null);
        }
        s.setUpdatedBy(actor);
        AiCounsellingScenario saved = scenarioRepository.save(s);
        invalidate();
        return saved;
    }

    @Transactional
    public void deleteScenario(Long id) {
        if (!scenarioRepository.existsById(id)) throw new ResourceNotFoundException("Scenario not found: " + id);
        scenarioRepository.deleteById(id);
        invalidate();
    }

    // ============================================================================ guidance

    public List<AiCategoryGuidance> guidance() {
        return guidanceRepository.findAllByOrderBySortOrderAscIdAsc();
    }

    @Transactional
    public List<AiCategoryGuidance> replaceGuidance(List<Map<String, Object>> rows) {
        List<AiCategoryGuidance> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            String cat = AiScenarioImporter.normaliseCategory(str(r.get("problemCategory")));
            String dir = AiScenarioImporter.clean(str(r.get("responseDirection")));
            if (cat == null || dir == null) continue;
            AiCategoryGuidance g = new AiCategoryGuidance();
            g.setProblemCategory(cat);
            g.setExampleOpening(limit(r.get("exampleOpening"), 500));
            g.setResponseDirection(dir);
            g.setSortOrder(out.size());
            out.add(g);
        }
        guidanceRepository.deleteAllInBatch();
        return guidanceRepository.saveAll(out);
    }

    // ============================================================================ stats

    @Transactional(readOnly = true)
    public Map<String, Object> stats() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", scenarioRepository.count());
        out.put("withResponse", scenarioRepository.countByHasResponseTrue());
        out.put("activeExamples", scenarioRepository.countByEnabledTrueAndHasResponseTrue());
        out.put("aiDrafts", scenarioRepository.countByAiDraftedTrue());
        out.put("guidanceRules", guidanceRepository.count());
        Map<String, Long> byCategory = new LinkedHashMap<>();
        scenarioRepository.countByCategory().forEach(r -> byCategory.put(r[0] == null ? "Uncategorised" : (String) r[0], (Long) r[1]));
        out.put("byCategory", byCategory);
        Map<String, Long> byRisk = new LinkedHashMap<>();
        scenarioRepository.countByRisk().forEach(r -> byRisk.put(r[0] == null ? "UNSPECIFIED" : (String) r[0], (Long) r[1]));
        out.put("byRisk", byRisk);
        return out;
    }

    // ============================================================================ BM25 index

    private Bm25Index index() {
        Bm25Index idx = index;
        if (idx == null) {
            synchronized (this) {
                idx = index;
                if (idx == null) {
                    boolean useDrafts = settingsRepository.findById(AiAssistantSettings.SINGLETON_ID)
                            .map(s -> !Boolean.FALSE.equals(s.getUseAiDrafts())).orElse(true);
                    List<AiCounsellingScenario> pool = scenarioRepository.findByEnabledTrueAndHasResponseTrue().stream()
                            .filter(s -> useDrafts || !Boolean.TRUE.equals(s.getAiDrafted()))
                            .toList();
                    idx = new Bm25Index(pool);
                    index = idx;
                    log.info("AI knowledge index built with {} scenarios", idx.size());
                }
            }
        }
        return idx;
    }

    public void invalidate() {
        index = null;
    }

    static List<String> tokenize(String text) {
        if (text == null) return List.of();
        List<String> out = new ArrayList<>();
        var m = TOKEN.matcher(text.toLowerCase(Locale.ROOT).replace("'", ""));
        while (m.find()) {
            String t = m.group();
            if (t.length() < 3 || STOPWORDS.contains(t)) continue;
            out.add(stem(t));
        }
        return out;
    }

    /** Deliberately crude suffix stripping — enough to match "failed/failing/fails", "stressed/stress". */
    static String stem(String t) {
        if (t.length() > 5 && t.endsWith("ing")) return t.substring(0, t.length() - 3);
        if (t.length() > 4 && t.endsWith("ies")) return t.substring(0, t.length() - 3) + "y";
        if (t.length() > 4 && t.endsWith("ed")) return t.substring(0, t.length() - 2);
        if (t.length() > 5 && t.endsWith("ly")) return t.substring(0, t.length() - 2);
        if (t.length() > 4 && t.endsWith("es")) return t.substring(0, t.length() - 2);
        if (t.length() > 3 && t.endsWith("s") && !t.endsWith("ss")) return t.substring(0, t.length() - 1);
        return t;
    }

    private static final class Bm25Index {
        private static final double K1 = 1.2, B = 0.75, GOLD_BOOST = 1.15, RELATIVE_CUTOFF = 0.45;
        private final List<AiCounsellingScenario> docs = new ArrayList<>();
        private final List<Map<String, Integer>> tfs = new ArrayList<>();
        private final List<Integer> lengths = new ArrayList<>();
        private final Map<String, Integer> df = new HashMap<>();
        private final double avgLen;

        Bm25Index(List<AiCounsellingScenario> scenarios) {
            long total = 0;
            for (AiCounsellingScenario s : scenarios) {
                // Topics and category are short but high-signal, so they're counted twice.
                String text = s.getStudentMessage() + " " + Objects.toString(s.getKeyTopics(), "") + " "
                        + Objects.toString(s.getKeyTopics(), "") + " " + Objects.toString(s.getProblemCategory(), "").replace('/', ' ');
                List<String> tokens = tokenize(text);
                Map<String, Integer> tf = new HashMap<>();
                tokens.forEach(t -> tf.merge(t, 1, Integer::sum));
                tf.keySet().forEach(t -> df.merge(t, 1, Integer::sum));
                docs.add(s);
                tfs.add(tf);
                lengths.add(tokens.size());
                total += tokens.size();
            }
            avgLen = docs.isEmpty() ? 1 : (double) total / docs.size();
        }

        int size() { return docs.size(); }

        List<RetrievedExample> search(List<String> query, int k, double minScore) {
            Set<String> terms = new LinkedHashSet<>(query);
            if (terms.isEmpty()) return List.of();
            int n = docs.size();
            List<RetrievedExample> scored = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Map<String, Integer> tf = tfs.get(i);
                double score = 0;
                for (String t : terms) {
                    Integer f = tf.get(t);
                    if (f == null) continue;
                    int d = df.getOrDefault(t, 0);
                    double idf = Math.log(1 + (n - d + 0.5) / (d + 0.5));
                    score += idf * (f * (K1 + 1)) / (f + K1 * (1 - B + B * lengths.get(i) / avgLen));
                }
                if (Boolean.TRUE.equals(docs.get(i).getGoldExample())) score *= GOLD_BOOST;
                if (score >= minScore) scored.add(new RetrievedExample(docs.get(i), score));
            }
            scored.sort(Comparator.comparingDouble(RetrievedExample::score).reversed());
            if (scored.isEmpty()) return scored;
            // Drop tail matches far weaker than the best one: an off-topic example misleads more than it helps.
            double cutoff = scored.get(0).score() * RELATIVE_CUTOFF;
            List<RetrievedExample> kept = scored.stream().filter(e -> e.score() >= cutoff).limit(k).toList();
            return kept;
        }
    }

    // ============================================================================ helpers

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private static String blankTo(String s, String fallback) { return notBlank(s) ? s.strip() : fallback; }
    private static String blankToNull(String s) { return notBlank(s) ? s.trim() : null; }
    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
    private static String limit(Object o, int max) {
        String s = o == null ? null : String.valueOf(o).strip();
        if (s == null || s.isEmpty()) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
