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

    /** Combined detection: the shared keyword scanner plus the AI chat's extra phrase patterns. */
    public CrisisDetectionService.CrisisResult scan(String message) {
        CrisisDetectionService.CrisisResult shared = crisisDetectionService.scan(message);
        if (shared.isCrisis()) return shared;
        if (AiAssistantService.detectCrisis(message)) {
            return new CrisisDetectionService.CrisisResult(CrisisDetectionService.Severity.HIGH,
                    List.of("AI chat crisis phrase"));
        }
        return shared;
    }

    /** Never throws — the student's chat reply must not fail because escalation did. */
    public void escalate(User user, Long chatMessageId, CrisisDetectionService.CrisisResult crisis) {
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
