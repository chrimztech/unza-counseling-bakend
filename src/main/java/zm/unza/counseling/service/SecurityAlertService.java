package zm.unza.counseling.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.ClinicSecurityAlertInboundRequest;
import zm.unza.counseling.dto.request.PanicButtonRequest;
import zm.unza.counseling.dto.request.SecurityAlertCreateRequest;
import zm.unza.counseling.dto.response.SecurityAlertResponse;
import zm.unza.counseling.entity.SecurityAlert;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.SecurityAlertRepository;
import zm.unza.counseling.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Central service for the SecurityAlert feature: creating alerts (manual report, panic
 * button, auto-escalation from crisis detection, or mirrored in from the clinic system),
 * acknowledging/resolving them, and keeping the clinic system in sync.
 */
@Service
@Slf4j
public class SecurityAlertService {

    private final SecurityAlertRepository securityAlertRepository;
    private final UserRepository userRepository;
    private final ClinicAlertSyncService clinicAlertSyncService;

    // Optional / lazy — the app must keep working even if STOMP messaging isn't wired up.
    private SimpMessagingTemplate messagingTemplate;

    public SecurityAlertService(SecurityAlertRepository securityAlertRepository,
                                 UserRepository userRepository,
                                 ClinicAlertSyncService clinicAlertSyncService) {
        this.securityAlertRepository = securityAlertRepository;
        this.userRepository = userRepository;
        this.clinicAlertSyncService = clinicAlertSyncService;
    }

    @Autowired(required = false)
    public void setMessagingTemplate(@Lazy SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    private boolean isWebSocketAvailable() {
        return messagingTemplate != null;
    }

    // ── Create ────────────────────────────────────────────────────────────

    @Transactional
    public SecurityAlert createManual(SecurityAlertCreateRequest request, Long reporterUserId) {
        User reporter = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found: " + reporterUserId));

        SecurityAlert alert = new SecurityAlert();
        alert.setCategory(request.getCategory());
        alert.setSeverity(request.getSeverity());
        alert.setOriginSystem(SecurityAlert.OriginSystem.COUNSELLING);
        alert.setSourceType(SecurityAlert.SourceType.MANUAL_REPORT);
        alert.setSubjectStudentId(request.getSubjectStudentId());
        alert.setSubjectName(request.getSubjectName());
        alert.setReportedByUserId(String.valueOf(reporter.getId()));
        alert.setReportedByName(reporter.getFullName());
        alert.setDescription(request.getDescription());
        alert.setLatitude(request.getLatitude());
        alert.setLongitude(request.getLongitude());
        alert.setStatus(SecurityAlert.Status.NEW);
        alert.setOccurredAt(LocalDateTime.now());

        return saveCreateAndSync(alert);
    }

    @Transactional
    public SecurityAlert createPanic(PanicButtonRequest request, Long userId) {
        User self = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found: " + userId));

        SecurityAlert alert = new SecurityAlert();
        alert.setCategory(SecurityAlert.Category.PANIC_BUTTON);
        alert.setSeverity(SecurityAlert.Severity.CRITICAL);
        alert.setOriginSystem(SecurityAlert.OriginSystem.COUNSELLING);
        alert.setSourceType(SecurityAlert.SourceType.PANIC_BUTTON);
        alert.setSubjectStudentId(self.getStudentId());
        alert.setSubjectName(self.getFullName());
        alert.setReportedByUserId(String.valueOf(self.getId()));
        alert.setReportedByName(self.getFullName());
        alert.setDescription("Panic button activated by " + self.getFullName());
        alert.setLatitude(request.getLatitude());
        alert.setLongitude(request.getLongitude());
        alert.setStatus(SecurityAlert.Status.NEW);
        alert.setOccurredAt(LocalDateTime.now());

        return saveCreateAndSync(alert);
    }

    /**
     * Auto-escalation from the crisis-keyword scanner (severity CRITICAL only). Called from
     * AppointmentServiceImpl and MessageService alongside their existing CrisisAlert creation.
     */
    @Transactional
    public SecurityAlert createFromCrisisDetection(User subject, List<String> triggeredKeywords) {
        SecurityAlert alert = new SecurityAlert();
        alert.setCategory(deriveCategory(triggeredKeywords));
        alert.setSeverity(SecurityAlert.Severity.CRITICAL);
        alert.setOriginSystem(SecurityAlert.OriginSystem.COUNSELLING);
        alert.setSourceType(SecurityAlert.SourceType.CRISIS_DETECTION);
        if (subject != null) {
            alert.setSubjectStudentId(subject.getStudentId());
            alert.setSubjectName(subject.getFullName());
        }
        String keywordSummary = (triggeredKeywords == null || triggeredKeywords.isEmpty())
                ? "" : String.join(", ", triggeredKeywords);
        alert.setDescription("Auto-detected by crisis keyword scan. Triggered keywords: " + keywordSummary);
        alert.setStatus(SecurityAlert.Status.NEW);
        alert.setOccurredAt(LocalDateTime.now());

        return saveCreateAndSync(alert);
    }

    /**
     * Create a local alert mirrored in from the clinic system. Does NOT sync back out to the
     * clinic (that would create an infinite loop) — it only broadcasts locally.
     */
    @Transactional
    public SecurityAlert createFromExternal(ClinicSecurityAlertInboundRequest request) {
        SecurityAlert alert = new SecurityAlert();
        alert.setCategory(request.getCategory());
        alert.setSeverity(request.getSeverity());
        alert.setOriginSystem(SecurityAlert.OriginSystem.CLINIC);
        alert.setSourceType(request.getSourceType());
        alert.setSubjectStudentId(request.getSubjectStudentId());
        alert.setSubjectName(request.getSubjectName());
        alert.setReportedByName(request.getReportedByName());
        alert.setDescription(request.getDescription());
        alert.setLatitude(request.getLatitude());
        alert.setLongitude(request.getLongitude());
        alert.setStatus(SecurityAlert.Status.NEW);
        alert.setExternalAlertId(request.getExternalAlertId());
        alert.setExternalSystem(SecurityAlert.ExternalSystem.CLINIC);
        alert.setOccurredAt(request.getOccurredAt() != null ? request.getOccurredAt() : LocalDateTime.now());

        SecurityAlert saved = securityAlertRepository.save(alert);
        broadcast(saved);
        return saved;
    }

    private SecurityAlert.Category deriveCategory(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return SecurityAlert.Category.OTHER;
        }
        boolean suicide = keywords.stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(k -> k.toLowerCase(Locale.ROOT).contains("suicide"));
        if (suicide) return SecurityAlert.Category.SUICIDE;

        boolean selfHarm = keywords.stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(k -> {
                    String lower = k.toLowerCase(Locale.ROOT);
                    return lower.contains("self harm") || lower.contains("self-harm") || lower.contains("cutting");
                });
        if (selfHarm) return SecurityAlert.Category.SELF_HARM;

        return SecurityAlert.Category.OTHER;
    }

    private SecurityAlert saveCreateAndSync(SecurityAlert alert) {
        SecurityAlert saved = securityAlertRepository.save(alert);
        broadcast(saved);
        try {
            clinicAlertSyncService.syncCreate(saved);
            // syncCreate may have populated externalAlertId/externalSystem on success
            securityAlertRepository.save(saved);
        } catch (Exception e) {
            log.warn("Outbound sync failed for security alert {}: {}", saved.getId(), e.getMessage());
        }
        return saved;
    }

    // ── Read ──────────────────────────────────────────────────────────────

    public List<SecurityAlert> list(SecurityAlert.Status status, SecurityAlert.Category category) {
        return securityAlertRepository.search(status, category);
    }

    // ── Transition ────────────────────────────────────────────────────────

    @Transactional
    public SecurityAlert acknowledge(Long id, String actorName) {
        SecurityAlert alert = securityAlertRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Security alert not found: " + id));

        alert.setStatus(SecurityAlert.Status.ACKNOWLEDGED);
        alert.setAcknowledgedByName(actorName);
        alert.setAcknowledgedAt(LocalDateTime.now());
        SecurityAlert saved = securityAlertRepository.save(alert);
        broadcast(saved);
        syncStatusIfLinked(saved);
        return saved;
    }

    @Transactional
    public SecurityAlert resolve(Long id, String resolutionNotes, boolean falsePositive, String actorName) {
        SecurityAlert alert = securityAlertRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Security alert not found: " + id));

        alert.setStatus(falsePositive ? SecurityAlert.Status.FALSE_POSITIVE : SecurityAlert.Status.RESOLVED);
        alert.setResolvedByName(actorName);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionNotes(resolutionNotes);
        SecurityAlert saved = securityAlertRepository.save(alert);
        broadcast(saved);
        syncStatusIfLinked(saved);
        return saved;
    }

    private void syncStatusIfLinked(SecurityAlert alert) {
        if (alert.getExternalAlertId() != null && !alert.getExternalAlertId().isBlank()) {
            try {
                clinicAlertSyncService.syncStatusUpdate(alert);
            } catch (Exception e) {
                log.warn("Failed to sync status update to clinic system for alert {}: {}", alert.getId(), e.getMessage());
            }
        }
    }

    /**
     * Applies a status update received FROM the clinic system for an alert that originated
     * here (or was previously created via createFromExternal). Updates the row directly and
     * does NOT call back out to the clinic system, to avoid an infinite sync loop.
     */
    @Transactional
    public SecurityAlert applyExternalStatusUpdate(Long localAlertId, SecurityAlert.Status status,
                                                     String actorName, String resolutionNotes) {
        SecurityAlert alert = securityAlertRepository.findById(localAlertId)
                .orElseThrow(() -> new NoSuchElementException("Security alert not found: " + localAlertId));

        alert.setStatus(status);
        if (status == SecurityAlert.Status.ACKNOWLEDGED) {
            alert.setAcknowledgedByName(actorName);
            alert.setAcknowledgedAt(LocalDateTime.now());
        } else if (status == SecurityAlert.Status.RESOLVED || status == SecurityAlert.Status.FALSE_POSITIVE) {
            alert.setResolvedByName(actorName);
            alert.setResolvedAt(LocalDateTime.now());
            alert.setResolutionNotes(resolutionNotes);
        }

        SecurityAlert saved = securityAlertRepository.save(alert);
        broadcast(saved);
        return saved;
    }

    // ── Broadcast ─────────────────────────────────────────────────────────

    private void broadcast(SecurityAlert alert) {
        try {
            if (isWebSocketAvailable()) {
                messagingTemplate.convertAndSend("/topic/security-alerts", SecurityAlertResponse.from(alert));
            } else {
                log.debug("WebSocket not available - security alert {} stored but not broadcast in real-time", alert.getId());
            }
        } catch (Exception e) {
            log.warn("Failed to broadcast security alert {}: {}", alert.getId(), e.getMessage());
        }
    }
}
