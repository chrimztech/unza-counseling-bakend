package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.AssessmentSubmissionRequest;
import zm.unza.counseling.dto.request.RiskAssessmentRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.RiskAssessment;
import zm.unza.counseling.entity.SelfAssessment;
import zm.unza.counseling.service.RiskAssessmentService;
import zm.unza.counseling.service.SelfAssessmentService;

import java.util.List;

@RestController
@RequestMapping({"/v1/assessments", "/assessments"})
@RequiredArgsConstructor
@Tag(name = "Assessments", description = "Risk and self-assessment operations")
public class AssessmentController {

    private final SelfAssessmentService selfAssessmentService;
    private final RiskAssessmentService riskAssessmentService;

    @GetMapping("/self")
    @PreAuthorize("hasAnyRole('STUDENT', 'CLIENT', 'COUNSELOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SelfAssessment>>> getAllSelfAssessments() {
        return ResponseEntity.ok(ApiResponse.success(selfAssessmentService.getAllAssessments()));
    }

    @PostMapping("/self/submit")
    @PreAuthorize("hasAnyRole('STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Void>> submitSelfAssessment(@RequestBody AssessmentSubmissionRequest request) {
        selfAssessmentService.submitAssessment(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Self-assessment submitted successfully"));
    }

    @PostMapping("/risk")
    @PreAuthorize("hasAnyRole('COUNSELOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<RiskAssessment>> createRiskAssessment(@RequestBody RiskAssessmentRequest request) {
        RiskAssessment assessment = riskAssessmentService.createRiskAssessment(request);
        return ResponseEntity.ok(ApiResponse.success(assessment, "Risk assessment created successfully"));
    }

    @GetMapping("/risk/client/{clientId}")
    @PreAuthorize("hasAnyRole('COUNSELOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RiskAssessment>>> getRiskAssessmentsForClient(@PathVariable Long clientId) {
        List<RiskAssessment> assessments = riskAssessmentService.getAssessmentsForClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(assessments));
    }
}