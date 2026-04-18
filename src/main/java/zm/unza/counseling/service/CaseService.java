package zm.unza.counseling.service;

import zm.unza.counseling.dto.request.CaseAssignmentRequest;
import zm.unza.counseling.dto.request.CreateCaseRequest;
import zm.unza.counseling.dto.response.CaseAssignmentResponse;
import zm.unza.counseling.dto.response.CaseResponse;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.CaseAssignment;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.Counselor;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.CaseAssignmentRepository;
import zm.unza.counseling.repository.CaseRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.CounselorRepository;
import zm.unza.counseling.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final ClientRepository clientRepository;
    private final CounselorRepository counselorRepository;
    private final CaseAssignmentRepository caseAssignmentRepository;
    private final UserRepository userRepository;

    public CaseService(CaseRepository caseRepository, ClientRepository clientRepository, 
                       CounselorRepository counselorRepository, CaseAssignmentRepository caseAssignmentRepository,
                       UserRepository userRepository) {
        this.caseRepository = caseRepository;
        this.clientRepository = clientRepository;
        this.counselorRepository = counselorRepository;
        this.caseAssignmentRepository = caseAssignmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CaseResponse createCase(CreateCaseRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + request.getClientId()));

        Case caseEntity = new Case();
        caseEntity.setClient(client);

        if (request.getCounselorId() != null) {
            Counselor counselor = counselorRepository.findById(request.getCounselorId())
                    .orElseThrow(() -> new RuntimeException("Counselor not found with ID: " + request.getCounselorId()));
            caseEntity.setCounselor(counselor);
            caseEntity.setAssignedAt(LocalDateTime.now());
        }

        caseEntity.setPriority(request.getPriority());
        caseEntity.setSubject(request.getSubject());
        caseEntity.setDescription(request.getDescription());
        caseEntity.setNotes(request.getNotes());
        caseEntity.setLastActivityAt(LocalDateTime.now());

        Case savedCase = caseRepository.save(caseEntity);
        
        // Create case assignment record if counselor is assigned
        if (request.getCounselorId() != null) {
            Counselor counselor = caseEntity.getCounselor();
            createCaseAssignment(savedCase, counselor, null, "Initial assignment on case creation", null);
        }
        
        return convertToResponse(savedCase);
    }

    public CaseResponse getCaseById(Long id) {
        Case caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + id));
        return convertToResponse(caseEntity);
    }

    public CaseResponse getCaseByCaseNumber(String caseNumber) {
        Case caseEntity = caseRepository.findByCaseNumber(caseNumber)
                .orElseThrow(() -> new RuntimeException("Case not found with case number: " + caseNumber));
        return convertToResponse(caseEntity);
    }

    public List<CaseResponse> getCasesByClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        return caseRepository.findByClient(client).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CaseResponse> getCasesByCounselor(Long counselorId) {
        Counselor counselor = counselorRepository.findById(counselorId)
                .orElseThrow(() -> new RuntimeException("Counselor not found with ID: " + counselorId));
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
        Case caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + id));

        if (request.getCounselorId() != null) {
            Counselor counselor = counselorRepository.findById(request.getCounselorId())
                    .orElseThrow(() -> new RuntimeException("Counselor not found with ID: " + request.getCounselorId()));
            caseEntity.setCounselor(counselor);
            caseEntity.setAssignedAt(LocalDateTime.now());
        }

        caseEntity.setPriority(request.getPriority());
        caseEntity.setSubject(request.getSubject());
        caseEntity.setDescription(request.getDescription());
        caseEntity.setNotes(request.getNotes());
        caseEntity.setLastActivityAt(LocalDateTime.now());

        Case updatedCase = caseRepository.save(caseEntity);
        return convertToResponse(updatedCase);
    }

    @Transactional
    public CaseResponse updateCaseStatus(Long id, Case.CaseStatus status) {
        Case caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + id));
        caseEntity.setStatus(status);
        caseEntity.setLastActivityAt(LocalDateTime.now());
        if (status == Case.CaseStatus.CLOSED) {
            caseEntity.setClosedAt(LocalDateTime.now());
        } else {
            caseEntity.setClosedAt(null);
        }
        Case updatedCase = caseRepository.save(caseEntity);
        return convertToResponse(updatedCase);
    }

    @Transactional
    public CaseResponse updateCasePriority(Long id, Case.CasePriority priority) {
        Case caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + id));
        caseEntity.setPriority(priority);
        caseEntity.setLastActivityAt(LocalDateTime.now());
        Case updatedCase = caseRepository.save(caseEntity);
        return convertToResponse(updatedCase);
    }

    @Transactional
    public void deleteCase(Long id) {
        if (!caseRepository.existsById(id)) {
            throw new RuntimeException("Case not found with ID: " + id);
        }
        caseRepository.deleteById(id);
    }

    /**
     * Assign a counselor to a case
     */
    @Transactional
    public CaseAssignmentResponse assignCounselorToCase(CaseAssignmentRequest request) {
        Case caseEntity = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + request.getCaseId()));

        Counselor counselor = counselorRepository.findById(request.getCounselorId())
                .orElseThrow(() -> new RuntimeException("Counselor not found with ID: " + request.getCounselorId()));

        // Get the currently authenticated user (who is making the assignment)
        User assignedBy = getCurrentUser();

        // Close any existing active assignments for this case
        List<CaseAssignment> activeAssignments = caseAssignmentRepository
                .findByCaseEntityAndStatus(caseEntity, "ACTIVE");
        for (CaseAssignment assignment : activeAssignments) {
            assignment.setStatus("CLOSED");
            assignment.setClosedAt(LocalDateTime.now());
            caseAssignmentRepository.save(assignment);
        }

        // Create new assignment
        CaseAssignment assignment = createCaseAssignment(caseEntity, counselor, assignedBy,
                request.getAssignmentReason(), request.getAssignmentNotes());

        // Update the case with the new counselor
        caseEntity.setCounselor(counselor);
        caseEntity.setAssignedAt(LocalDateTime.now());
        caseEntity.setAssignedBy(assignedBy != null ? assignedBy.getId() : null);
        caseEntity.setLastActivityAt(LocalDateTime.now());
        caseRepository.save(caseEntity);

        return convertToAssignmentResponse(assignment);
    }

    /**
     * Get assignment history for a case
     */
    public List<CaseAssignmentResponse> getCaseAssignmentHistory(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));

        return caseAssignmentRepository.findByCaseEntity(caseEntity).stream()
                .map(this::convertToAssignmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all active assignments for a counselor
     */
    public List<CaseAssignmentResponse> getActiveAssignmentsForCounselor(Long counselorId) {
        Counselor counselor = counselorRepository.findById(counselorId)
                .orElseThrow(() -> new RuntimeException("Counselor not found with ID: " + counselorId));

        return caseAssignmentRepository.findByAssignedToAndStatus(counselor, "ACTIVE").stream()
                .map(this::convertToAssignmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get case statistics for dashboard
     */
    public Object getCaseStatistics() {
        return buildCaseStatistics(caseRepository.findAll());
    }

    public Object getCaseStatisticsByCounselor(Long counselorId) {
        Counselor counselor = counselorRepository.findById(counselorId)
                .orElseThrow(() -> new RuntimeException("Counselor not found with ID: " + counselorId));
        return buildCaseStatistics(caseRepository.findByCounselor(counselor));
    }

    /**
     * Create a case assignment record
     */
    private CaseAssignment createCaseAssignment(Case caseEntity, Counselor counselor, User assignedBy,
                                                 String reason, String notes) {
        CaseAssignment assignment = new CaseAssignment();
        assignment.setCaseEntity(caseEntity);
        assignment.setAssignedTo(counselor);
        assignment.setAssignedBy(assignedBy);
        assignment.setAssignmentReason(reason);
        assignment.setAssignmentNotes(notes);
        assignment.setStatus("ACTIVE");
        return caseAssignmentRepository.save(assignment);
    }

    /**
     * Get the currently authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElse(null);
        }
        return null;
    }

    /**
     * Convert CaseAssignment entity to response DTO
     */
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
        response.setStatus(caseEntity.getStatus());
        response.setPriority(caseEntity.getPriority());
        response.setSubject(caseEntity.getSubject());
        response.setDescription(caseEntity.getDescription());
        response.setNotes(caseEntity.getNotes());
        response.setCreatedAt(caseEntity.getCreatedAt());
        response.setUpdatedAt(caseEntity.getUpdatedAt());
        response.setClosedAt(caseEntity.getClosedAt());
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
