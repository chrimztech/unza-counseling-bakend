package zm.unza.counseling.controller;

import zm.unza.counseling.dto.request.CaseAssignmentRequest;
import zm.unza.counseling.dto.request.CreateCaseRequest;
import zm.unza.counseling.dto.response.CaseAssignmentResponse;
import zm.unza.counseling.dto.response.CaseResponse;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.service.CaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/cases", "/api/cases", "/v1/cases", "/cases"})
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @PostMapping
    public ResponseEntity<CaseResponse> createCase(@Valid @RequestBody CreateCaseRequest request) {
        return ResponseEntity.ok(caseService.createCase(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaseResponse> getCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(caseService.getCaseById(id));
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
    public ResponseEntity<List<CaseResponse>> getAllCases() {
        return ResponseEntity.ok(caseService.getAllCases());
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
}
