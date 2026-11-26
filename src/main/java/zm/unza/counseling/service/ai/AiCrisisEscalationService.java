package zm.unza.counseling.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zm.unza.counseling.entity.CrisisAlert;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.CrisisAlertRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.CrisisDetectionService;
import zm.unza.counseling.service.NotificationService;
import zm.unza.counseling.service.SecurityAlertService;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Turns crisis language in the AI wellness chat into the same CrisisAlert + counselor/admin
 * notification (+ SecurityAlert for CRITICAL) flow that direct messages and appointment
 * requests already use, so a student disclosing risk to the bot reaches a human.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiCrisisEscalationService {

    /** One open alert per user per window — a distressed conversation shouldn't raise one per message. */
    private static final int DEDUPE_HOURS = 6;

    private final CrisisDetectionService crisisDetectionService;
    private final CrisisAlertRepository crisisAlertRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SecurityAlertService securityAlertService;

    /**
     * HIGH-severity phrases from the shared scanner that indicate danger to the person. The others
     * on that list ("depression", "anxiety attack", "alcohol", "useless", "trauma"…) are the normal
     * subject of a wellness conversation: the assistant handles them, and alerting every counsellor
     * each time a student names them would bury the real emergencies.
     */
    private static final Set<String> CHAT_ALERT_HIGH = Set.of(
            "abuse", "abusing me", "being abused", "domestic violence", "beaten", "hitting me",
            "can't go on", "cannot go on", "can not go on", "give up on life", "end it all");

    /**
     * Detection for the AI chat: every CRITICAL phrase from the shared scanner, the danger-related
     * HIGH phrases above, and the chat's own suicide/self-harm patterns.
     */
    public CrisisDetectionService.CrisisResult scan(String message) {
        CrisisDetectionService.CrisisResult shared = crisisDetectionService.scan(message);
        if (shared.severity() == CrisisDetectionService.Severity.CRITICAL) return shared;
        List<String> danger = shared.triggeredKeywords().stream().filter(CHAT_ALERT_HIGH::contains).toList();
        if (!danger.isEmpty()) return new CrisisDetectionService.CrisisResult(CrisisDetectionService.Severity.HIGH, danger);
        List<String> patternWords = AiAssistantService.crisisPatternHits(message).stream()
                .map(h -> h.text().toLowerCase(Locale.ROOT).strip()).distinct().toList();
        if (!patternWords.isEmpty()) {
            return new CrisisDetectionService.CrisisResult(CrisisDetectionService.Severity.HIGH, patternWords);
        }
        return new CrisisDetectionService.CrisisResult(CrisisDetectionService.Severity.NONE, List.of());
    }

    /** Whether a HIGH phrase from the shared list raises an alert when it appears in the AI chat. */
    public static boolean isChatAlertPhrase(String phrase) {
        return CHAT_ALERT_HIGH.contains(phrase);
    }

    /** Never throws — the student's chat reply must not fail because escalation did. */
    public void escalate(User user, Long chatMessageId, CrisisDetectionService.CrisisResult crisis) {
        escalate(user, chatMessageId, crisis, null);
    }

    /**
     * As above, keeping a copy of the flagged message on the alert so the counsellor can still see
     * what was written if the student later clears their chat history.
     */
    public void escalate(User user, Long chatMessageId, CrisisDetectionService.CrisisResult crisis, String messageText) {
        if (user == null || crisis == null || !crisis.isCrisis()) return;
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(DEDUPE_HOURS);
            boolean openRecent = crisisAlertRepository.findByClientId(user.getId()).stream().anyMatch(a ->
                    a.getStatus() == CrisisAlert.AlertStatus.PENDING
                    && a.getCreatedAt() != null && a.getCreatedAt().isAfter(cutoff)
                    && (crisis.severity() != CrisisDetectionService.Severity.CRITICAL
                        || a.getSeverity() == CrisisAlert.Severity.CRITICAL));
            if (openRecent) {
                log.info("AI chat crisis for user {} — open alert already pending, not duplicating", user.getId());
                return;
            }

            boolean critical = crisis.severity() == CrisisDetectionService.Severity.CRITICAL;
            CrisisAlert alert = new CrisisAlert();
            // MESSAGE (not a new enum value): Hibernate maps this column to a native MySQL ENUM that
            // ddl-auto=update won't extend. The keywords field records that the source was the AI chat.
            alert.setSourceType(CrisisAlert.SourceType.MESSAGE);
            alert.setSourceId(chatMessageId);
            alert.setClient(user);
            alert.setSeverity(critical ? CrisisAlert.Severity.CRITICAL : CrisisAlert.Severity.HIGH);
            alert.setTriggeredKeywords("[AI wellness chat] " + String.join(", ", crisis.triggeredKeywords()));
            alert.setSourceExcerpt(CrisisAlert.excerpt(messageText));
            crisisAlertRepository.save(alert);

            if (critical) {
                try {
                    securityAlertService.createFromCrisisDetection(user, crisis.triggeredKeywords());
                } catch (Exception e) {
                    log.warn("Failed to create SecurityAlert from AI chat for user {}: {}", user.getId(), e.getMessage());
                }
            }

            String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                    + (user.getLastName() == null ? "" : user.getLastName())).trim();
            String who = name.isEmpty() ? "A user" : name;
            Set<Long> ids = new HashSet<>();
            userRepository.findByRolesName(Role.ERole.ROLE_COUNSELOR).forEach(u -> ids.add(u.getId()));
            userRepository.findByRolesName(Role.ERole.ROLE_ADMIN).forEach(u -> ids.add(u.getId()));
            notificationService.sendNotifications(ids,
                    critical ? "⚠ CRITICAL: Crisis Indicators in AI Chat" : "⚠ Crisis Indicators in AI Chat",
                    who + " expressed potential crisis indicators to the AI Wellness Assistant. Immediate review recommended.",
                    "CRISIS_ALERT", "CRITICAL", "/counselor/crisis-alerts");
        } catch (Exception e) {
            log.error("Failed to escalate AI chat crisis for user {}: {}", user.getId(), e.getMessage(), e);
        }
    }
}
