package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.AssessmentSubmissionRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.SelfAssessment;
import zm.unza.counseling.service.SelfAssessmentService;

import java.util.List;

@RestController
@RequestMapping("/api/self-assessments")
@RequiredArgsConstructor
@Tag(name = "Self Assessments", description = "Self-assessment operations")
public class SelfAssessmentController {

    private final SelfAssessmentService selfAssessmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'CLIENT', 'COUNSELOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SelfAssessment>>> getAllAssessments() {
        return ResponseEntity.ok(ApiResponse.success(selfAssessmentService.getAllAssessments()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<SelfAssessment>> createAssessment(@RequestBody SelfAssessment assessment) {
        return ResponseEntity.ok(ApiResponse.success(selfAssessmentService.createAssessment(assessment)));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Void>> submitAssessment(@RequestBody AssessmentSubmissionRequest request) {
        selfAssessmentService.submitAssessment(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Assessment submitted successfully"));
    }
}