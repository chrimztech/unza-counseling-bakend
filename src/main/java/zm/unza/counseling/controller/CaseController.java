package zm.unza.counseling.controller;

import zm.unza.counseling.dto.request.CaseAssignmentRequest;
import zm.unza.counseling.dto.request.CreateCaseRequest;
import zm.unza.counseling.dto.AppointmentDto;
import zm.unza.counseling.dto.response.CaseAssignmentResponse;
import zm.unza.counseling.dto.response.CaseResponse;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.service.AppointmentService;
import zm.unza.counseling.service.CaseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/cases", "/api/cases", "/v1/cases", "/cases"})
public class CaseController {

    private final CaseService caseService;
    private final AppointmentService appointmentService;

    public CaseController(CaseService caseService, AppointmentService appointmentService) {
        this.caseService = caseService;
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<CaseResponse> createCase(@Valid @RequestBody CreateCaseRequest request) {
        return ResponseEntity.ok(caseService.createCase(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaseResponse> getCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(caseService.getCaseById(id));
    }

    @GetMapping("/{caseId}/appointments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AppointmentDto>> getAppointmentsByCase(@PathVariable Long caseId, Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByCaseId(caseId, pageable));
    }

    @GetMapping("/number/{caseNumber}")
    public ResponseEntity<CaseResponse> getCaseByCaseNumber(@PathVariable String caseNumber) {
        return ResponseEntity.ok(caseService.getCaseByCaseNumber(caseNumber));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CaseResponse>> getCasesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(caseService.getCasesByClient(clientId));
    }

    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<List<CaseResponse>> getCasesByCounselor(@PathVariable Long counselorId) {
        return ResponseEntity.ok(caseService.getCasesByCounselor(counselorId));
    }

    @GetMapping
    public ResponseEntity<List<CaseResponse>> getAllCases(
            @RequestParam(required = false) Case.CaseStatus status,
            @RequestParam(required = false) Case.CasePriority priority,
            @RequestParam(required = false) Long counselorId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        List<CaseResponse> filteredCases = caseService.getAllCases().stream()
                .filter(caseItem -> status == null || caseItem.getStatus() == status)
                .filter(caseItem -> priority == null || caseItem.getPriority() == priority)
                .filter(caseItem -> counselorId == null || Objects.equals(caseItem.getCounselorId(), counselorId))
                .filter(caseItem -> clientId == null || Objects.equals(caseItem.getClientId(), clientId))
                .filter(caseItem -> matchesSearch(caseItem, search))
                .sorted(buildComparator(sortBy, sortDirection))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filteredCases);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CaseResponse> updateCase(@PathVariable Long id, @Valid @RequestBody CreateCaseRequest request) {
        return ResponseEntity.ok(caseService.updateCase(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CaseResponse> updateCaseStatus(@PathVariable Long id, @RequestBody Case.CaseStatus status) {
        return ResponseEntity.ok(caseService.updateCaseStatus(id, status));
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<CaseResponse> updateCasePriority(@PathVariable Long id, @RequestBody Case.CasePriority priority) {
        return ResponseEntity.ok(caseService.updateCasePriority(id, priority));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCase(@PathVariable Long id) {
        caseService.deleteCase(id);
        return ResponseEntity.noContent().build();
    }

    // Case Assignment Endpoints

    /**
     * Assign a counselor to a case
     */
    @PostMapping("/assign")
    public ResponseEntity<CaseAssignmentResponse> assignCounselorToCase(@Valid @RequestBody CaseAssignmentRequest request) {
        return ResponseEntity.ok(caseService.assignCounselorToCase(request));
    }

    /**
     * Get assignment history for a case
     */
    @GetMapping("/{caseId}/assignments")
    public ResponseEntity<List<CaseAssignmentResponse>> getCaseAssignmentHistory(@PathVariable Long caseId) {
        return ResponseEntity.ok(caseService.getCaseAssignmentHistory(caseId));
    }

    /**
     * Get all active assignments for a counselor
     */
    @GetMapping("/counselor/{counselorId}/active-assignments")
    public ResponseEntity<List<CaseAssignmentResponse>> getActiveAssignmentsForCounselor(@PathVariable Long counselorId) {
        return ResponseEntity.ok(caseService.getActiveAssignmentsForCounselor(counselorId));
    }

    /**
     * Get case statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getCaseStats() {
        return ResponseEntity.ok(caseService.getCaseStatistics());
    }

    @GetMapping("/stats/counselor")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getCaseStatsByCounselor(@RequestParam Long counselorId) {
        return ResponseEntity.ok(caseService.getCaseStatisticsByCounselor(counselorId));
    }

    private boolean matchesSearch(CaseResponse caseItem, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String normalized = search.toLowerCase(Locale.ROOT);
        return contains(caseItem.getCaseNumber(), normalized)
                || contains(caseItem.getSubject(), normalized)
                || contains(caseItem.getDescription(), normalized)
                || contains(caseItem.getClientName(), normalized)
                || contains(caseItem.getClientEmail(), normalized)
                || contains(caseItem.getCounselorName(), normalized);
    }

    private boolean contains(String value, String normalizedSearch) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedSearch);
    }

    private Comparator<CaseResponse> buildComparator(String sortBy, String sortDirection) {
        Comparator<CaseResponse> comparator = switch (sortBy) {
            case "updatedAt" -> Comparator.comparing(CaseResponse::getUpdatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
            case "caseNumber" -> Comparator.comparing(
                    caseItem -> caseItem.getCaseNumber() != null ? caseItem.getCaseNumber().toLowerCase(Locale.ROOT) : "",
                    String::compareTo
            );
            case "priority" -> Comparator.comparing(
                    caseItem -> caseItem.getPriority() != null ? caseItem.getPriority().name() : "",
                    String::compareTo
            );
            case "status" -> Comparator.comparing(
                    caseItem -> caseItem.getStatus() != null ? caseItem.getStatus().name() : "",
                    String::compareTo
            );
            default -> Comparator.comparing(CaseResponse::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
        };

        if ("DESC".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }
}
