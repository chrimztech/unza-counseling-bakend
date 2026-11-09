package zm.unza.counseling.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.*;
import zm.unza.counseling.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Long-term memory and personal context for the wellness assistant.
 *
 * <p><b>Memory.</b> Every chat exchange is stored. Exchanges after
 * {@link AiUserMemory#getSummarizedThroughId()} are sent to the LLM verbatim; once more than
 * {@link #FOLD_THRESHOLD} are waiting, the oldest are folded (by the LLM, in the background) into a
 * running summary, keeping the newest {@link #VERBATIM_KEEP} verbatim. So the assistant always sees
 * a summary of everything older plus every recent message — nothing is dropped.
 *
 * <p><b>Records.</b> The user's own self-assessment scores, upcoming appointments and the latest
 * wellness-analysis status. Counsellor session notes are deliberately never included: they are
 * confidential clinical notes and a student-facing chatbot could repeat them back.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiStudentMemoryService {

    static final int VERBATIM_KEEP = 10;
    static final int FOLD_THRESHOLD = 15;
    /** Safety cap on verbatim history if summarising keeps failing (e.g. provider down). */
    static final int VERBATIM_HARD_CAP = 30;

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM yyyy");
    private static final DateTimeFormatter DAY_TIME = DateTimeFormatter.ofPattern("EEE d MMM, HH:mm");

    private final AiUserMemoryRepository memoryRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiAssistantSettingsRepository settingsRepository;
    private final SelfAssessmentRepository selfAssessmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final MentalHealthAcademicAnalysisRepository analysisRepository;
    private final AiGatewayService gateway;

    private final Set<Long> folding = ConcurrentHashMap.newKeySet();

    // ===================================================================== conversation history

    /** Verbatim turns to send: everything not yet summarised (or the last 10 if memory is off). */
    @Transactional(readOnly = true)
    public List<AiMessage> verbatimHistory(Long userId) {
        List<ChatMessage> rows;
        if (memoryEnabled()) {
            long after = memoryRepository.findById(userId).map(AiUserMemory::getSummarizedThroughId).orElse(0L);
            rows = chatMessageRepository.findByUserIdAndIdGreaterThanOrderByIdAsc(userId, after);
            if (rows.size() > VERBATIM_HARD_CAP) rows = rows.subList(rows.size() - VERBATIM_HARD_CAP, rows.size());
        } else {
            List<ChatMessage> desc = chatMessageRepository.findByUserIdOrderByCreatedAtDesc(userId);
            rows = new ArrayList<>(desc.subList(0, Math.min(VERBATIM_KEEP, desc.size())));
            Collections.reverse(rows);
        }
        List<AiMessage> out = new ArrayList<>();
        for (ChatMessage m : rows) {
            if (notBlank(m.getMessage())) out.add(AiMessage.user(m.getMessage()));
            if (notBlank(m.getResponse())) out.add(AiMessage.assistant(m.getResponse()));
        }
        return out;
    }

    // ===================================================================== personal context

    /**
     * The "what you know about this person" section of the system prompt, or null if there is
     * nothing to add. Never throws — personal context is an enhancement, not a requirement.
     */
    @Transactional(readOnly = true)
    public String personalContext(User user) {
        try {
            StringBuilder sb = new StringBuilder();
            AiAssistantSettings settings = settings();

            if (!Boolean.FALSE.equals(settings.getMemoryEnabled())) {
                memoryRepository.findById(user.getId())
                        .filter(m -> notBlank(m.getSummary()))
                        .ifPresent(m -> sb.append("### What you remember from earlier conversations with this person\n")
                                .append(m.getSummary().strip()).append("\n\n"));
            }

            if (!Boolean.FALSE.equals(settings.getRecordsEnabled()) && !user.isAnonymous()) {
                String records = records(user);
                if (!records.isEmpty()) {
                    sb.append("### This person's records in the counselling system\n").append(records).append('\n');
                }
            }

            if (sb.isEmpty()) return null;
            return "## About the person you are talking to\n"
                    + "Use this to make your support personal and continuous: refer back naturally (\"last time you "
                    + "mentioned...\"), follow up on things they were worried about, and don't make them repeat themselves. "
                    + "Do not recite this section, quote scores, or name clinical labels unless they ask; never say a "
                    + "counsellor wrote something about them. If something here seems out of date, gently check with them.\n\n"
                    + sb;
        } catch (Exception e) {
            log.warn("Could not build personal context for user {}: {}", user.getId(), e.getMessage());
            return null;
        }
    }

    private String records(User user) {
        StringBuilder sb = new StringBuilder();
        if (notBlank(user.getFirstName())) sb.append("- First name: ").append(user.getFirstName().trim()).append('\n');

        selfAssessmentRepository.findTopBySubmittedByUserIdOrderByAssessmentDateDesc(user.getId()).ifPresent(a -> {
            List<String> parts = new ArrayList<>();
            if (a.getPhq9Score() != null) parts.add("PHQ-9 (depression) " + a.getPhq9Score() + "/27, " + phq9(a.getPhq9Score()));
            if (a.getGad7Score() != null) parts.add("GAD-7 (anxiety) " + a.getGad7Score() + "/21, " + gad7(a.getGad7Score()));
            if (a.getPssScore() != null) parts.add("PSS (stress) " + a.getPssScore() + "/40, " + pss(a.getPssScore()));
            if (a.getSleepQuality() != null) parts.add("sleep quality " + a.getSleepQuality());
            if (a.getOverallWellness() != null) parts.add("overall wellness " + a.getOverallWellness());
            List<String> flags = new ArrayList<>();
            if (Boolean.TRUE.equals(a.getAppetiteChanges())) flags.add("appetite changes");
            if (Boolean.TRUE.equals(a.getConcentrationDifficulty())) flags.add("difficulty concentrating");
            if (Boolean.TRUE.equals(a.getSocialWithdrawal())) flags.add("social withdrawal");
            if (!flags.isEmpty()) parts.add("reported " + String.join(", ", flags));
            if (!parts.isEmpty()) {
                LocalDateTime when = a.getAssessmentDate() != null ? a.getAssessmentDate() : a.getCreatedAt();
                sb.append("- Latest self-assessment").append(when != null ? " (" + DAY.format(when) + ")" : "")
                  .append(": ").append(String.join("; ", parts)).append('\n');
            }
        });

        List<Appointment> upcoming = appointmentRepository.findUpcomingByStudent(user, LocalDateTime.now());
        if (!upcoming.isEmpty()) {
            sb.append("- Upcoming counselling appointments: ");
            List<String> items = new ArrayList<>();
            for (Appointment ap : upcoming.subList(0, Math.min(3, upcoming.size()))) {
                String item = ap.getAppointmentDate() != null ? DAY_TIME.format(ap.getAppointmentDate()) : "date to be confirmed";
                if (ap.getType() != null) item += " (" + ap.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ') + ")";
                items.add(item);
            }
            sb.append(String.join("; ", items)).append('\n');
        } else {
            sb.append("- No upcoming counselling appointment booked.\n");
        }

        if (notBlank(user.getStudentId())) {
            clientRepository.findByStudentId(user.getStudentId()).ifPresent(client -> {
                List<MentalHealthAcademicAnalysis> analyses = analysisRepository.findByClientIdOrderByCreatedAtDesc(client.getId());
                if (!analyses.isEmpty()) {
                    MentalHealthAcademicAnalysis a = analyses.get(0);
                    if (a.getMentalHealthStatus() != null) {
                        sb.append("- Latest wellness review status: ")
                          .append(a.getMentalHealthStatus().name().toLowerCase(Locale.ROOT).replace('_', ' '));
                        if (a.getCreatedAt() != null) sb.append(" (").append(DAY.format(a.getCreatedAt())).append(')');
                        sb.append('\n');
                    }
                    List<String> factors = new ArrayList<>();
                    if (Boolean.TRUE.equals(a.getSleepIssues())) factors.add("sleep");
                    if (Boolean.TRUE.equals(a.getFinancialStress())) factors.add("finances");
                    if (Boolean.TRUE.equals(a.getFamilyIssues())) factors.add("family");
                    if (Boolean.TRUE.equals(a.getSocialIsolation())) factors.add("feeling isolated");
                    if (Boolean.TRUE.equals(a.getConcentrationIssues())) factors.add("concentration");
                    if (Boolean.TRUE.equals(a.getMotivationIssues())) factors.add("motivation");
                    if (Boolean.TRUE.equals(a.getSubstanceUseConcern())) factors.add("substance use");
                    if (!factors.isEmpty()) sb.append("- Areas noted as difficult: ").append(String.join(", ", factors)).append('\n');
                }
            });
        }
        return sb.toString();
    }

    // Standard published severity bands.
    private static String phq9(int s) { return s <= 4 ? "minimal" : s <= 9 ? "mild" : s <= 14 ? "moderate" : s <= 19 ? "moderately severe" : "severe"; }
    private static String gad7(int s) { return s <= 4 ? "minimal" : s <= 9 ? "mild" : s <= 14 ? "moderate" : "severe"; }
    private static String pss(int s) { return s <= 13 ? "low" : s <= 26 ? "moderate" : "high"; }

    // ===================================================================== summarising

    /**
     * Folds older exchanges into the user's memory summary once enough are waiting. Runs after the
     * reply has been returned, so it never slows the chat. Safe to call after every message.
     */
    @Async
    public void maybeFoldAsync(Long userId) {
        if (!memoryEnabled() || !folding.add(userId)) return;
        try {
            AiUserMemory memory = memoryRepository.findById(userId).orElseGet(() -> {
                AiUserMemory m = new AiUserMemory();
                m.setUserId(userId);
                return m;
            });
            long after = memory.getSummarizedThroughId() == null ? 0L : memory.getSummarizedThroughId();
            List<ChatMessage> pending = chatMessageRepository.findByUserIdAndIdGreaterThanOrderByIdAsc(userId, after);
            if (pending.size() <= FOLD_THRESHOLD) return;

            List<ChatMessage> toFold = pending.subList(0, pending.size() - VERBATIM_KEEP);
            StringBuilder transcript = new StringBuilder();
            for (ChatMessage m : toFold) {
                String when = m.getCreatedAt() != null ? DAY.format(m.getCreatedAt()) : "";
                if (notBlank(m.getMessage())) transcript.append("[").append(when).append("] Person: ").append(m.getMessage().strip()).append('\n');
                if (notBlank(m.getResponse())) transcript.append("[").append(when).append("] Assistant: ").append(m.getResponse().strip()).append('\n');
            }

            String prompt = """
                    You maintain the long-term memory of a university wellness assistant about ONE person.
                    Merge the existing memory with the new conversation excerpt into an updated memory.

                    Write concise bullet points (max ~250 words total) under these headings, omitting empty ones:
                    - Key facts about their situation (studies, year, family, finances, living situation)
                    - Ongoing concerns and how they have changed over time (with approximate dates)
                    - What has helped or what they said they would try
                    - Things to follow up on (exams, deadlines, appointments, decisions)
                    - Safety: any mention of self-harm, suicidal thoughts, abuse or danger, with dates (keep these even if old)

                    Rules: only record what the person actually said; no diagnosis; no speculation; do not record
                    passwords, ID numbers or other credentials. Prefer newer information when it conflicts with older.
                    Reply with ONLY the updated memory.
                    """;
            String input = "EXISTING MEMORY:\n" + (notBlank(memory.getSummary()) ? memory.getSummary() : "(none yet)")
                    + "\n\nNEW CONVERSATION EXCERPT:\n" + transcript;

            AiResult r = gateway.chat(prompt, List.of(AiMessage.user(input)), 700);
            memory.setSummary(r.content().strip());
            memory.setSummarizedThroughId(toFold.get(toFold.size() - 1).getId());
            memory.setSummarizedCount((memory.getSummarizedCount() == null ? 0 : memory.getSummarizedCount()) + toFold.size());
            memory.setUpdatedAt(LocalDateTime.now());
            memoryRepository.save(memory);
            log.info("AI memory for user {} updated: folded {} exchanges ({} total)", userId, toFold.size(), memory.getSummarizedCount());
        } catch (AiUnavailableException e) {
            log.debug("AI memory fold skipped for user {} (no provider): {}", userId, e.getMessage());
        } catch (Exception e) {
            log.warn("AI memory fold failed for user {}: {}", userId, e.getMessage());
        } finally {
            folding.remove(userId);
        }
    }

    @Transactional
    public void forget(Long userId) {
        memoryRepository.deleteById(userId);
    }

    // ===================================================================== helpers

    private AiAssistantSettings settings() {
        return settingsRepository.findById(AiAssistantSettings.SINGLETON_ID).orElseGet(AiAssistantSettings::new);
    }

    private boolean memoryEnabled() {
        return !Boolean.FALSE.equals(settings().getMemoryEnabled());
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
