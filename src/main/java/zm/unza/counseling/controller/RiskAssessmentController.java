package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.RiskAssessmentRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.RiskAssessment;
import zm.unza.counseling.service.RiskAssessmentService;

import java.util.List;

@RestController
@RequestMapping("/api/risk-assessments")
@RequiredArgsConstructor
@Tag(name = "Risk Assessments", description = "Risk assessment management endpoints")
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get all risk assessments", description = "Retrieve all risk assessments with pagination")
    public ResponseEntity<ApiResponse<Page<RiskAssessment>>> getAllRiskAssessments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(riskAssessmentService.getAllRiskAssessments(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get risk assessment by ID", description = "Retrieve a specific risk assessment")
    public ResponseEntity<ApiResponse<RiskAssessment>> getRiskAssessmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(riskAssessmentService.getRiskAssessmentById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Create risk assessment", description = "Create a new risk assessment")
    public ResponseEntity<ApiResponse<RiskAssessment>> createRiskAssessment(@RequestBody RiskAssessmentRequest request) {
        RiskAssessment assessment = riskAssessmentService.createRiskAssessment(request);
        return ResponseEntity.ok(ApiResponse.success(assessment, "Risk assessment created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Update risk assessment", description = "Update an existing risk assessment")
    public ResponseEntity<ApiResponse<RiskAssessment>> updateRiskAssessment(
            @PathVariable Long id,
            @RequestBody RiskAssessmentRequest request) {
        RiskAssessment assessment = riskAssessmentService.updateRiskAssessment(id, request);
        return ResponseEntity.ok(ApiResponse.success(assessment, "Risk assessment updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete risk assessment", description = "Delete a risk assessment")
    public ResponseEntity<ApiResponse<String>> deleteRiskAssessment(@PathVariable Long id) {
        riskAssessmentService.deleteRiskAssessment(id);
        return ResponseEntity.ok(ApiResponse.success("Risk assessment deleted successfully"));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get risk assessments by client", description = "Retrieve all risk assessments for a specific client")
    public ResponseEntity<ApiResponse<List<RiskAssessment>>> getRiskAssessmentsByClient(@PathVariable Long clientId) {
        List<RiskAssessment> assessments = riskAssessmentService.getRiskAssessmentsByClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(assessments));
    }

    @GetMapping("/high-risk")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get high-risk assessments", description = "Retrieve all high-risk assessments")
    public ResponseEntity<ApiResponse<List<RiskAssessment>>> getHighRiskAssessments() {
        List<RiskAssessment> assessments = riskAssessmentService.getHighRiskAssessments();
        return ResponseEntity.ok(ApiResponse.success(assessments));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get risk assessment statistics", description = "Get statistics about risk assessments")
    public ResponseEntity<ApiResponse<Object>> getRiskAssessmentStats() {
        Object stats = riskAssessmentService.getRiskAssessmentStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Escalate risk assessment", description = "Escalate a risk assessment for urgent attention")
    public ResponseEntity<ApiResponse<RiskAssessment>> escalateRiskAssessment(@PathVariable Long id) {
        RiskAssessment assessment = riskAssessmentService.escalateRiskAssessment(id);
        return ResponseEntity.ok(ApiResponse.success(assessment, "Risk assessment escalated successfully"));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export risk assessment data", description = "Export risk assessment data")
    public ResponseEntity<byte[]> exportRiskAssessmentData(@RequestParam(defaultValue = "csv") String format) {
        byte[] data = riskAssessmentService.exportRiskAssessmentData(format);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=risk-assessments." + format)
                .body(data);
    }
}