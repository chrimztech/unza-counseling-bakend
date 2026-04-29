package zm.unza.counseling.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.CaseAssignmentRequest;
import zm.unza.counseling.dto.request.CreateCaseRequest;
import zm.unza.counseling.dto.response.CaseAssignmentResponse;
import zm.unza.counseling.dto.response.CaseResponse;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.CaseAssignment;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.ClientIntakeForm;
import zm.unza.counseling.entity.Counselor;
import zm.unza.counseling.entity.PersonalDataForm;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.CaseAssignmentRepository;
import zm.unza.counseling.repository.CaseRepository;
import zm.unza.counseling.repository.ClientIntakeFormRepository;
import zm.unza.counseling.repository.PersonalDataFormRepository;
import zm.unza.counseling.repository.ReportRepository;
import zm.unza.counseling.repository.SessionRepository;
import zm.unza.counseling.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CaseService {

    private static final Logger log = LoggerFactory.getLogger(CaseService.class);

    private final CaseRepository caseRepository;
    private final AppointmentRepository appointmentRepository;
    private final CaseAssignmentRepository caseAssignmentRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final ReportRepository reportRepository;
    private final ClientIntakeFormRepository clientIntakeFormRepository;
    private final PersonalDataFormRepository personalDataFormRepository;
    private final CaseDocumentService caseDocumentService;
    private final ClientIdentityService clientIdentityService;
    private final CounselorIdentityService counselorIdentityService;
    private final NotificationService notificationService;

    public CaseService(
            CaseRepository caseRepository,
            AppointmentRepository appointmentRepository,
            CaseAssignmentRepository caseAssignmentRepository,
            UserRepository userRepository,
            SessionRepository sessionRepository,
            ReportRepository reportRepository,
            ClientIntakeFormRepository clientIntakeFormRepository,
            PersonalDataFormRepository personalDataFormRepository,
            CaseDocumentService caseDocumentService,
            ClientIdentityService clientIdentityService,
            CounselorIdentityService counselorIdentityService,
            NotificationService notificationService
    ) {
        this.caseRepository = caseRepository;
        this.appointmentRepository = appointmentRepository;
        this.caseAssignmentRepository = caseAssignmentRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.reportRepository = reportRepository;
        this.clientIntakeFormRepository = clientIntakeFormRepository;
        this.personalDataFormRepository = personalDataFormRepository;
        this.caseDocumentService = caseDocumentService;
        this.clientIdentityService = clientIdentityService;
        this.counselorIdentityService = counselorIdentityService;
        this.notificationService = notificationService;
    }

    @Transactional
    public CaseResponse createCase(CreateCaseRequest request) {
        Client client = getClientOrThrow(request.getClientId());

        Case caseEntity = new Case();
        caseEntity.setClient(client);
        applyCaseDetails(caseEntity, request);
        caseEntity.setLastActivityAt(LocalDateTime.now());

        Counselor counselor = null;
        if (request.getCounselorId() != null) {
            counselor = getCounselorOrThrow(request.getCounselorId());
            applyAssignmentMetadata(caseEntity, counselor, resolveAssignmentActor(counselor));
        }

        Case savedCase = caseRepository.save(caseEntity);
        if (counselor != null) {
            ensureActiveAssignment(savedCase, counselor, "Initial assignment on case creation", null);
        }
        notifyCaseCreated(savedCase, counselor, getCurrentUser());

        return convertToResponse(savedCase);
    }

    public CaseResponse getCaseById(Long id) {
        return convertToResponse(getCaseOrThrow(id));
    }

    public CaseResponse getCaseByCaseNumber(String caseNumber) {
        Case caseEntity = caseRepository.findByCaseNumber(caseNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with case number: " + caseNumber));
        return convertToResponse(caseEntity);
    }

    public List<CaseResponse> getCasesByClient(Long clientId) {
        Client client = getClientOrThrow(clientId);
        return caseRepository.findByClient(client).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CaseResponse> getCasesByCounselor(Long counselorId) {
        Counselor counselor = getCounselorOrThrow(counselorId);
        return caseRepository.findByCounselor(counselor).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CaseResponse> getAllCases() {
        return caseRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CaseResponse updateCase(Long id, CreateCaseRequest request) {
        Case caseEntity = getCaseOrThrow(id);
        boolean counselorChanged = false;

        if (!Objects.equals(caseEntity.getClient().getId(), request.getClientId())) {
            if (appointmentRepository.countAllByCaseId(id) > 0) {
                throw new ValidationException("Cannot move a case to a different client after appointments have been linked");
            }
            caseEntity.setClient(getClientOrThrow(request.getClientId()));
        }

        applyCaseDetails(caseEntity, request);
        caseEntity.setLastActivityAt(LocalDateTime.now());

        if (request.getCounselorId() != null) {
            Counselor counselor = getCounselorOrThrow(request.getCounselorId());
            if (!sameCounselor(caseEntity.getCounselor(), counselor)) {
                assertCaseAllowsAssignment(caseEntity);
                applyAssignmentMetadata(caseEntity, counselor, resolveAssignmentActor(counselor));
                ensureActiveAssignment(caseEntity, counselor, "Counselor assigned during case update", null);
                counselorChanged = true;
            } else {
                if (caseEntity.getAssignedBy() == null || caseEntity.getAssignedAt() == null) {
                    applyAssignmentMetadata(caseEntity, counselor, resolveAssignmentActor(counselor));
                }
                ensureActiveAssignment(caseEntity, counselor, "Counselor assignment synchronized", null);
            }
        }

        Case updatedCase = caseRepository.save(caseEntity);
        if (counselorChanged && updatedCase.getCounselor() != null) {
            notifyCaseAssigned(updatedCase, updatedCase.getCounselor(), getCurrentUser());
        }
        return convertToResponse(updatedCase);
    }

    @Transactional
    public CaseResponse updateCaseStatus(Long id, Case.CaseStatus status) {
        if (status == null) {
            throw new ValidationException("Case status is required");
        }

        Case caseEntity = getCaseOrThrow(id);
        if (status == Case.CaseStatus.CLOSED && hasOpenAppointments(caseEntity)) {
            throw new ValidationException("Cannot close a case while linked appointments are still active");
        }

        applyStatusTransition(caseEntity, status);
        if (status == Case.CaseStatus.CLOSED || status == Case.CaseStatus.RESOLVED || status == Case.CaseStatus.REFERRED) {
            closeActiveAssignments(caseEntity);
        }

        Case updatedCase = caseRepository.save(caseEntity);
        notifyCaseStatusUpdated(updatedCase);
        return convertToResponse(updatedCase);
    }

    @Transactional
    public CaseResponse updateCasePriority(Long id, Case.CasePriority priority) {
        if (priority == null) {
            throw new ValidationException("Case priority is required");
        }

        Case caseEntity = getCaseOrThrow(id);
        caseEntity.setPriority(priority);
        caseEntity.setLastActivityAt(LocalDateTime.now());
        Case updatedCase = caseRepository.save(caseEntity);
        return convertToResponse(updatedCase);
    }

    @Transactional
    public void deleteCase(Long id) {
        Case caseEntity = getCaseOrThrow(id);

        if (sessionRepository.countByLinkedCaseId(id) > 0) {
            throw new ValidationException("Case cannot be permanently deleted because session records already exist. Close the case instead to preserve history.");
        }
        if (reportRepository.countByCaseId(id) > 0) {
            throw new ValidationException("Case cannot be permanently deleted because reports already reference it. Close the case instead to preserve history.");
        }

        clearCaseLinks(caseEntity);
        caseRepository.delete(caseEntity);
    }

    @Transactional
    public CaseAssignmentResponse assignCounselorToCase(CaseAssignmentRequest request) {
        Case caseEntity = getCaseOrThrow(request.getCaseId());
        Counselor counselor = getCounselorOrThrow(request.getCounselorId());
        assertCaseAllowsAssignment(caseEntity);

        List<CaseAssignment> activeAssignments = caseAssignmentRepository.findByCaseEntityAndStatus(caseEntity, "ACTIVE");
        boolean alreadyAssignedToCounselor = activeAssignments.stream()
                .anyMatch(assignment -> assignment.getAssignedTo() != null
                        && assignment.getAssignedTo().getId().equals(counselor.getId()));
        if (alreadyAssignedToCounselor) {
            throw new ValidationException("Case is already assigned to this counselor");
        }

        User assignedBy = resolveAssignmentActor(counselor);
        closeAssignments(activeAssignments);
        applyAssignmentMetadata(caseEntity, counselor, assignedBy);
        caseEntity.setLastActivityAt(LocalDateTime.now());
        caseRepository.save(caseEntity);

        CaseAssignment assignment = createCaseAssignment(
                caseEntity,
                counselor,
                assignedBy,
                request.getAssignmentReason(),
                request.getAssignmentNotes()
        );

        notifyCaseAssigned(caseEntity, counselor, assignedBy);

        return convertToAssignmentResponse(assignment);
    }

    public List<CaseAssignmentResponse> getCaseAssignmentHistory(Long caseId) {
        Case caseEntity = getCaseOrThrow(caseId);
        return caseAssignmentRepository.findByCaseEntity(caseEntity).stream()
                .map(this::convertToAssignmentResponse)
                .collect(Collectors.toList());
    }

    public List<CaseAssignmentResponse> getActiveAssignmentsForCounselor(Long counselorId) {
        Counselor counselor = getCounselorOrThrow(counselorId);
        return caseAssignmentRepository.findByAssignedToAndStatus(counselor, "ACTIVE").stream()
                .map(this::convertToAssignmentResponse)
                .collect(Collectors.toList());
    }

    public Object getCaseStatistics() {
        return buildCaseStatistics(caseRepository.findAll());
    }

    public Object getCaseStatisticsByCounselor(Long counselorId) {
        Counselor counselor = getCounselorOrThrow(counselorId);
        return buildCaseStatistics(caseRepository.findByCounselor(counselor));
    }

    private void applyCaseDetails(Case caseEntity, CreateCaseRequest request) {
        caseEntity.setPriority(request.getPriority() != null ? request.getPriority() : Case.CasePriority.MEDIUM);
        caseEntity.setSubject(normalize(request.getSubject()));
        caseEntity.setDescription(normalizeRequired(request.getDescription(), "Case description is required"));
        caseEntity.setNotes(normalize(request.getNotes()));
    }

    private void applyAssignmentMetadata(Case caseEntity, Counselor counselor, User assignedBy) {
        caseEntity.setCounselor(counselor);
        caseEntity.setAssignedAt(LocalDateTime.now());
        caseEntity.setAssignedBy(assignedBy != null ? assignedBy.getId() : null);
    }

    private void applyStatusTransition(Case caseEntity, Case.CaseStatus status) {
        LocalDateTime now = LocalDateTime.now();
        caseEntity.setStatus(status);
        caseEntity.setLastActivityAt(now);

        switch (status) {
            case CLOSED -> {
                if (caseEntity.getActualResolutionDate() == null) {
                    caseEntity.setActualResolutionDate(now);
                }
                if (caseEntity.getClosedAt() == null) {
                    caseEntity.setClosedAt(now);
                }
            }
            case RESOLVED -> {
                if (caseEntity.getActualResolutionDate() == null) {
                    caseEntity.setActualResolutionDate(now);
                }
                caseEntity.setClosedAt(null);
            }
            default -> {
                caseEntity.setActualResolutionDate(null);
                caseEntity.setClosedAt(null);
            }
        }
    }

    private void clearCaseLinks(Case caseEntity) {
        caseDocumentService.deleteDocumentsByCase(caseEntity.getId());
        unlinkCaseForms(caseEntity);
        appointmentRepository.unlinkAllByCaseId(caseEntity.getId());
        caseAssignmentRepository.deleteByCaseEntity(caseEntity);
    }

    private void unlinkCaseForms(Case caseEntity) {
        clientIntakeFormRepository.findByCaseEntity(caseEntity).ifPresent(form -> {
            form.setCaseEntity(null);
            clientIntakeFormRepository.save(form);
        });

        personalDataFormRepository.findByCaseEntity(caseEntity).ifPresent(form -> {
            form.setCaseEntity(null);
            personalDataFormRepository.save(form);
        });
    }

    private void ensureActiveAssignment(Case caseEntity, Counselor counselor, String reason, String notes) {
        List<CaseAssignment> activeAssignments = caseAssignmentRepository.findByCaseEntityAndStatus(caseEntity, "ACTIVE");
        boolean alreadySynchronized = activeAssignments.stream()
                .anyMatch(assignment -> assignment.getAssignedTo() != null
                        && assignment.getAssignedTo().getId().equals(counselor.getId()));
        if (alreadySynchronized) {
            return;
        }

        User assignedBy = resolveAssignmentActor(counselor);
        closeAssignments(activeAssignments);
        createCaseAssignment(caseEntity, counselor, assignedBy, reason, notes);
    }

    private void closeActiveAssignments(Case caseEntity) {
        closeAssignments(caseAssignmentRepository.findByCaseEntityAndStatus(caseEntity, "ACTIVE"));
    }

    private void closeAssignments(List<CaseAssignment> assignments) {
        LocalDateTime now = LocalDateTime.now();
        for (CaseAssignment assignment : assignments) {
            assignment.setStatus("CLOSED");
            assignment.setUpdatedAt(now);
            assignment.setClosedAt(now);
            caseAssignmentRepository.save(assignment);
        }
    }

    private boolean hasOpenAppointments(Case caseEntity) {
        return appointmentRepository.findByCaseEntity(caseEntity).stream()
                .map(Appointment::getStatus)
                .anyMatch(this::isActiveAppointmentStatus);
    }

    private boolean isActiveAppointmentStatus(Appointment.AppointmentStatus status) {
        return status == null || switch (status) {
            case SCHEDULED, CONFIRMED, IN_PROGRESS, RESCHEDULED, PENDING, UNASSIGNED -> true;
            case COMPLETED, CANCELLED, NO_SHOW, MISSED -> false;
        };
    }

    private boolean sameCounselor(Counselor current, Counselor requested) {
        return current != null && requested != null && Objects.equals(current.getId(), requested.getId());
    }

    private void assertCaseAllowsAssignment(Case caseEntity) {
        if (caseEntity.getStatus() == Case.CaseStatus.CLOSED) {
            throw new ValidationException("Closed cases must be reopened before assigning a counselor");
        }
    }

    private CaseAssignment createCaseAssignment(
            Case caseEntity,
            Counselor counselor,
            User assignedBy,
            String reason,
            String notes
    ) {
        CaseAssignment assignment = new CaseAssignment();
        assignment.setCaseEntity(caseEntity);
        assignment.setAssignedTo(counselor);
        assignment.setAssignedBy(assignedBy);
        assignment.setAssignmentReason(normalize(reason));
        assignment.setAssignmentNotes(normalize(notes));
        assignment.setStatus("ACTIVE");
        return caseAssignmentRepository.save(assignment);
    }

    private Case getCaseOrThrow(Long id) {
        return caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with id: " + id));
    }

    private Client getClientOrThrow(Long clientId) {
        return clientIdentityService.getOrCreateClient(clientId);
    }

    private Counselor getCounselorOrThrow(Long counselorId) {
        return counselorIdentityService.getOrCreateCounselor(counselorId);
    }

    private User resolveAssignmentActor(Counselor fallbackCounselor) {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser : fallbackCounselor;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
                return null;
            }
            return userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElse(null);
        }
        return null;
    }

    private void notifyCaseCreated(Case caseEntity, Counselor counselor, User actor) {
        if (caseEntity.getClient() != null) {
            safeNotify(
                    caseEntity.getClient().getId(),
                    "Case opened",
                    "Your counseling case " + caseEntity.getCaseNumber() + " has been opened.",
                    "CASE",
                    "MEDIUM",
                    "/cases/" + caseEntity.getId()
            );
        }

        if (counselor != null && !sameUser(actor, counselor)) {
            safeNotify(
                    counselor.getId(),
                    "New case assignment",
                    "Case " + caseEntity.getCaseNumber() + " has been assigned to you for "
                            + caseEntity.getClient().getFullName() + ".",
                    "CASE",
                    "HIGH",
                    "/cases/" + caseEntity.getId()
            );
        }

        if (actor == null || actor.isClient()) {
            List<Long> adminIds = getAdminRecipientIds(actor != null ? actor.getId() : null);
            if (!adminIds.isEmpty()) {
                safeNotifyMany(
                        adminIds,
                        "New counseling request",
                        caseEntity.getClient().getFullName() + " has a new counseling case "
                                + caseEntity.getCaseNumber() + " that needs review.",
                        "CASE",
                        "HIGH",
                        "/cases/" + caseEntity.getId()
                );
            }
        }
    }

    private void notifyCaseAssigned(Case caseEntity, Counselor counselor, User actor) {
        if (counselor != null && !sameUser(actor, counselor)) {
            safeNotify(
                    counselor.getId(),
                    "Case assigned",
                    "You have been assigned case " + caseEntity.getCaseNumber() + " for "
                            + caseEntity.getClient().getFullName() + ".",
                    "CASE",
                    "HIGH",
                    "/cases/" + caseEntity.getId()
            );
        }

        if (caseEntity.getClient() != null) {
            safeNotify(
                    caseEntity.getClient().getId(),
                    "Counselor assigned",
                    "A counselor has been assigned to your case " + caseEntity.getCaseNumber() + ".",
                    "CASE",
                    "MEDIUM",
                    "/cases/" + caseEntity.getId()
            );
        }
    }

    private void notifyCaseStatusUpdated(Case caseEntity) {
        if (caseEntity.getClient() != null) {
            safeNotify(
                    caseEntity.getClient().getId(),
                    "Case status updated",
                    "Your case " + caseEntity.getCaseNumber() + " is now "
                            + caseEntity.getStatus().name().replace('_', ' ').toLowerCase() + ".",
                    "CASE",
                    "MEDIUM",
                    "/cases/" + caseEntity.getId()
            );
        }
    }

    private List<Long> getAdminRecipientIds(Long excludedUserId) {
        return java.util.stream.Stream.concat(
                        userRepository.findByRolesName(zm.unza.counseling.entity.Role.ERole.ROLE_ADMIN).stream(),
                        userRepository.findByRolesName(zm.unza.counseling.entity.Role.ERole.ROLE_SUPER_ADMIN).stream()
                )
                .map(User::getId)
                .filter(id -> excludedUserId == null || !excludedUserId.equals(id))
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean sameUser(User left, User right) {
        return left != null && right != null && Objects.equals(left.getId(), right.getId());
    }

    private void safeNotify(Long userId, String title, String message, String type, String priority, String actionUrl) {
        try {
            notificationService.sendNotification(userId, title, message, type, priority, actionUrl);
        } catch (Exception exception) {
            log.warn("Failed to create notification for user {}", userId, exception);
        }
    }

    private void safeNotifyMany(List<Long> userIds, String title, String message, String type, String priority, String actionUrl) {
        try {
            notificationService.sendNotifications(userIds, title, message, type, priority, actionUrl);
        } catch (Exception exception) {
            log.warn("Failed to create notifications for users {}", userIds, exception);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new ValidationException(message);
        }
        return normalized;
    }

    private CaseAssignmentResponse convertToAssignmentResponse(CaseAssignment assignment) {
        CaseAssignmentResponse response = new CaseAssignmentResponse();
        response.setId(assignment.getId());
        response.setCaseId(assignment.getCaseEntity().getId());
        response.setCaseNumber(assignment.getCaseEntity().getCaseNumber());

        if (assignment.getAssignedBy() != null) {
            response.setAssignedBy(assignment.getAssignedBy().getId());
            response.setAssignedByName(assignment.getAssignedBy().getFullName());
        }

        response.setAssignedTo(assignment.getAssignedTo().getId());
        response.setAssignedToName(assignment.getAssignedTo().getFullName());
        response.setAssignmentReason(assignment.getAssignmentReason());
        response.setAssignmentNotes(assignment.getAssignmentNotes());
        response.setStatus(assignment.getStatus());
        response.setAssignedAt(assignment.getAssignedAt());

        return response;
    }

    private CaseResponse convertToResponse(Case caseEntity) {
        CaseResponse response = new CaseResponse();
        response.setId(caseEntity.getId());
        response.setCaseNumber(caseEntity.getCaseNumber());
        response.setClientId(caseEntity.getClient().getId());
        response.setClientName(caseEntity.getClient().getFullName());
        response.setClientEmail(caseEntity.getClient().getEmail());
        if (caseEntity.getCounselor() != null) {
            response.setCounselorId(caseEntity.getCounselor().getId());
            response.setCounselorName(caseEntity.getCounselor().getFullName());
        }
        response.setAssignedBy(caseEntity.getAssignedBy());
        if (caseEntity.getAssignedBy() != null) {
            response.setAssignedByName(userRepository.findById(caseEntity.getAssignedBy())
                    .map(User::getFullName)
                    .orElse(null));
        }
        response.setStatus(caseEntity.getStatus());
        response.setPriority(caseEntity.getPriority());
        response.setSubject(caseEntity.getSubject());
        response.setDescription(caseEntity.getDescription());
        response.setNotes(caseEntity.getNotes());
        response.setCreatedAt(caseEntity.getCreatedAt());
        response.setUpdatedAt(caseEntity.getUpdatedAt());
        response.setAssignedAt(caseEntity.getAssignedAt());
        response.setLastActivityAt(caseEntity.getLastActivityAt());
        response.setExpectedResolutionDate(caseEntity.getExpectedResolutionDate());
        response.setActualResolutionDate(caseEntity.getActualResolutionDate());
        response.setClosedAt(caseEntity.getClosedAt());
        response.setEscalationLevel(caseEntity.getEscalationLevel());
        response.setTags(caseEntity.getTags());
        response.setCustomFields(caseEntity.getCustomFields());
        response.setAppointmentCount(Math.toIntExact(appointmentRepository.countByCaseEntity(caseEntity)));
        return response;
    }

    private Map<String, Object> buildCaseStatistics(List<Case> cases) {
        long totalCases = cases.size();
        long openCases = cases.stream().filter(caseEntity -> caseEntity.getStatus() == Case.CaseStatus.OPEN).count();
        long inProgressCases = cases.stream().filter(caseEntity -> caseEntity.getStatus() == Case.CaseStatus.IN_PROGRESS).count();
        long closedCases = cases.stream().filter(caseEntity -> caseEntity.getStatus() == Case.CaseStatus.CLOSED).count();
        long onHoldCases = cases.stream().filter(caseEntity -> caseEntity.getStatus() == Case.CaseStatus.ON_HOLD).count();
        long resolvedCases = cases.stream()
                .filter(caseEntity -> caseEntity.getStatus() == Case.CaseStatus.RESOLVED || caseEntity.getStatus() == Case.CaseStatus.CLOSED)
                .count();
        long activeCases = cases.stream()
                .filter(caseEntity -> caseEntity.getStatus() == Case.CaseStatus.OPEN || caseEntity.getStatus() == Case.CaseStatus.IN_PROGRESS)
                .count();
        long highPriorityCases = cases.stream()
                .filter(caseEntity -> caseEntity.getPriority() == Case.CasePriority.HIGH || caseEntity.getPriority() == Case.CasePriority.CRITICAL)
                .count();

        Map<String, Long> casesByPriority = new LinkedHashMap<>();
        for (Case.CasePriority priority : Case.CasePriority.values()) {
            long count = cases.stream().filter(caseEntity -> caseEntity.getPriority() == priority).count();
            casesByPriority.put(priority.name(), count);
        }

        Map<String, Long> casesByStatus = new LinkedHashMap<>();
        for (Case.CaseStatus status : Case.CaseStatus.values()) {
            long count = cases.stream().filter(caseEntity -> caseEntity.getStatus() == status).count();
            casesByStatus.put(status.name(), count);
        }

        double averageCaseDuration = cases.stream()
                .filter(caseEntity -> caseEntity.getCreatedAt() != null)
                .mapToLong(caseEntity -> {
                    LocalDateTime end = caseEntity.getClosedAt() != null
                            ? caseEntity.getClosedAt()
                            : caseEntity.getUpdatedAt() != null ? caseEntity.getUpdatedAt() : LocalDateTime.now();
                    return Math.max(ChronoUnit.DAYS.between(caseEntity.getCreatedAt(), end), 0);
                })
                .average()
                .orElse(0);

        YearMonth currentMonth = YearMonth.now();
        long casesCreatedThisMonth = cases.stream()
                .filter(caseEntity -> caseEntity.getCreatedAt() != null)
                .filter(caseEntity -> YearMonth.from(caseEntity.getCreatedAt()).equals(currentMonth))
                .count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCases", totalCases);
        stats.put("openCases", openCases);
        stats.put("activeCases", activeCases);
        stats.put("inProgressCases", inProgressCases);
        stats.put("closedCases", closedCases);
        stats.put("onHoldCases", onHoldCases);
        stats.put("resolvedCases", resolvedCases);
        stats.put("highPriorityCases", highPriorityCases);
        stats.put("casesByPriority", casesByPriority);
        stats.put("casesByStatus", casesByStatus);
        stats.put("averageCaseDuration", Math.round(averageCaseDuration * 10.0) / 10.0);
        stats.put("casesCreatedThisMonth", casesCreatedThisMonth);
        return stats;
    }
}
