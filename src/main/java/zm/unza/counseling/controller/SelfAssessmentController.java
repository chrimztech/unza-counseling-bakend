package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.AssessmentSubmissionRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.SelfAssessment;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.service.SelfAssessmentService;
import zm.unza.counseling.service.UserService;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/self-assessments", "/api/self-assessments", "/v1/self-assessments", "/self-assessments"})
@RequiredArgsConstructor
@Tag(name = "Self Assessments", description = "Self-assessment operations")
public class SelfAssessmentController {

    private final SelfAssessmentService selfAssessmentService;
    private final UserService userService;

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

    @GetMapping("/client")
    @PreAuthorize("hasAnyRole('STUDENT', 'CLIENT', 'COUNSELOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SelfAssessment>>> getSelfAssessmentsByClient(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(selfAssessmentService.getSelfAssessmentsByClient(user.getId())));
    }

    @GetMapping("/client/{clientId}/latest")
    @PreAuthorize("hasAnyRole('STUDENT', 'CLIENT', 'COUNSELOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<SelfAssessment>> getLatestSelfAssessmentForClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(selfAssessmentService.getLatestSelfAssessmentForClient(clientId)));
    }

    @GetMapping("/client/{clientId}/trend")
    @PreAuthorize("hasAnyRole('STUDENT', 'CLIENT', 'COUNSELOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<?>> getSelfAssessmentTrend(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(selfAssessmentService.getSelfAssessmentTrend(clientId)));
    }
}