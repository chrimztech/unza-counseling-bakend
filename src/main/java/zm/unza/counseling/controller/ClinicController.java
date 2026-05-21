package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.ClinicReferralRequest;
import zm.unza.counseling.dto.request.ClinicVisitRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.ClinicReferralResponse;
import zm.unza.counseling.dto.response.ClinicVisitFrequencyResponse;
import zm.unza.counseling.dto.response.ClinicVisitResponse;
import zm.unza.counseling.entity.ClinicReferral;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.ClinicService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/clinic")
@Tag(name = "Clinic Integration", description = "Referrals to the university clinic and visit frequency tracking")
public class ClinicController {

    private final ClinicService clinicService;
    private final UserRepository userRepository;

    public ClinicController(ClinicService clinicService, UserRepository userRepository) {
        this.clinicService = clinicService;
        this.userRepository = userRepository;
    }

    // ── Referrals ─────────────────────────────────────────────────────────

    @PostMapping("/referrals")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Create referral", description = "Refer a client to the university clinic")
    public ResponseEntity<ApiResponse<ClinicReferralResponse>> createReferral(
            @Valid @RequestBody ClinicReferralRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long counselorId = resolveUserId(userDetails);
        ClinicReferralResponse resp = clinicService.createReferral(request, counselorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(resp, "Referral created successfully"));
    }

    @GetMapping("/referrals")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "List all referrals")
    public ResponseEntity<ApiResponse<Page<ClinicReferralResponse>>> getAllReferrals(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(clinicService.getAllReferrals(pageable)));
    }

    @GetMapping("/referrals/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get referral by ID")
    public ResponseEntity<ApiResponse<ClinicReferralResponse>> getReferral(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(clinicService.getReferralById(id)));
    }

    @GetMapping("/referrals/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get all referrals for a client")
    public ResponseEntity<ApiResponse<List<ClinicReferralResponse>>> getReferralsByClient(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(clinicService.getReferralsByClient(clientId)));
    }

    @GetMapping("/referrals/case/{caseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get referrals linked to a case")
    public ResponseEntity<ApiResponse<Page<ClinicReferralResponse>>> getReferralsByCase(
            @PathVariable Long caseId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(clinicService.getReferralsByCase(caseId, pageable)));
    }

    @PatchMapping("/referrals/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Update referral status", description = "Move a referral through its lifecycle (PENDING→SENT→ACCEPTED→COMPLETED or DECLINED)")
    public ResponseEntity<ApiResponse<ClinicReferralResponse>> updateReferralStatus(
            @PathVariable Long id,
            @RequestParam ClinicReferral.ReferralStatus status,
            @RequestParam(required = false) String clinicNotes,
            @RequestParam(required = false) String externalReferenceId) {
        return ResponseEntity.ok(ApiResponse.success(
                clinicService.updateReferralStatus(id, status, clinicNotes, externalReferenceId),
                "Referral status updated"));
    }

    @DeleteMapping("/referrals/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Delete a referral")
    public ResponseEntity<ApiResponse<Void>> deleteReferral(@PathVariable Long id) {
        clinicService.deleteReferral(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Referral deleted"));
    }

    // ── Clinic Visits ─────────────────────────────────────────────────────

    @PostMapping("/visits")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Record a clinic visit manually")
    public ResponseEntity<ApiResponse<ClinicVisitResponse>> recordVisit(
            @Valid @RequestBody ClinicVisitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(clinicService.recordVisit(request), "Visit recorded"));
    }

    /**
     * Inbound webhook — the university clinic system calls this endpoint to push
     * visit records into the counseling system automatically.
     *
     * Security: protect this endpoint with an API key header in a real deployment.
     * For now it shares the same ADMIN/COUNSELOR auth; the clinic system should
     * have a dedicated service account.
     */
    @PostMapping("/visits/inbound")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Inbound webhook for clinic system",
               description = "The university clinic calls this endpoint to push visit records automatically. " +
                             "In production, secure this with an API key or a dedicated service account.")
    public ResponseEntity<ApiResponse<ClinicVisitResponse>> inboundVisit(
            @Valid @RequestBody ClinicVisitRequest request) {
        request.setRecordedBy("CLINIC_WEBHOOK");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(clinicService.recordVisit(request), "Visit recorded via webhook"));
    }

    @GetMapping("/visits/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get all clinic visits for a client")
    public ResponseEntity<ApiResponse<List<ClinicVisitResponse>>> getVisitsByClient(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(clinicService.getVisitsByClient(clientId)));
    }

    @GetMapping("/visits/client/{clientId}/frequency")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get visit frequency statistics for a client",
               description = "Returns total visits, visits in last 30/90 days, this year, monthly breakdown, " +
                             "and a frequentVisitor flag (true when >= 3 visits in the last 90 days)")
    public ResponseEntity<ApiResponse<ClinicVisitFrequencyResponse>> getVisitFrequency(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(clinicService.getVisitFrequency(clientId)));
    }

    @GetMapping("/visits/frequent-visitors")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "List frequent clinic visitors",
               description = "Returns clients who visited the clinic >= threshold times within the given number of days")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFrequentVisitors(
            @RequestParam(defaultValue = "3") int threshold,
            @RequestParam(defaultValue = "90") int withinDays) {
        return ResponseEntity.ok(ApiResponse.success(
                clinicService.getFrequentVisitors(threshold, withinDays)));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Long resolveUserId(UserDetails userDetails) {
        String name = userDetails.getUsername();
        var user = userRepository.findByUsername(name)
                .or(() -> userRepository.findByEmail(name))
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found: " + name));
        return user.getId();
    }
}
