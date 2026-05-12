package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.ScholarshipRequest;
import zm.unza.counseling.dto.request.ScholarshipRecommendationRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.ScholarshipRecommendationResponse;
import zm.unza.counseling.dto.response.ScholarshipResponse;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.ScholarshipRecommendation;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.ScholarshipService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/scholarships", "/v1/scholarships"})
@Tag(name = "Scholarships", description = "Scholarship management and student recommendation APIs")
public class ScholarshipController {

    private final ScholarshipService scholarshipService;
    private final UserRepository userRepository;

    public ScholarshipController(ScholarshipService scholarshipService, UserRepository userRepository) {
        this.scholarshipService = scholarshipService;
        this.userRepository = userRepository;
    }

    // ---- Scholarship endpoints ----

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Create scholarship", description = "Creates a new scholarship opportunity")
    public ResponseEntity<ApiResponse<ScholarshipResponse>> createScholarship(
            @Valid @RequestBody ScholarshipRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        ScholarshipResponse response = scholarshipService.createScholarship(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Scholarship created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get scholarship by ID")
    public ResponseEntity<ApiResponse<ScholarshipResponse>> getScholarship(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(scholarshipService.getScholarship(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get all scholarships")
    public ResponseEntity<ApiResponse<List<ScholarshipResponse>>> getAllScholarships() {
        return ResponseEntity.ok(ApiResponse.success(scholarshipService.getAllScholarships(), "Scholarships retrieved successfully"));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get active (open, not past deadline) scholarships")
    public ResponseEntity<ApiResponse<List<ScholarshipResponse>>> getActiveScholarships() {
        return ResponseEntity.ok(ApiResponse.success(scholarshipService.getActiveScholarships(), "Active scholarships retrieved"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Update scholarship")
    public ResponseEntity<ApiResponse<ScholarshipResponse>> updateScholarship(
            @PathVariable Long id,
            @Valid @RequestBody ScholarshipRequest request) {
        return ResponseEntity.ok(ApiResponse.success(scholarshipService.updateScholarship(id, request), "Scholarship updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete scholarship")
    public ResponseEntity<ApiResponse<Void>> deleteScholarship(@PathVariable Long id) {
        scholarshipService.deleteScholarship(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Scholarship deleted successfully"));
    }

    // ---- Eligible students ----

    @GetMapping("/{id}/eligible-students")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get students eligible for a scholarship (not yet recommended)")
    public ResponseEntity<ApiResponse<List<?>>> getEligibleStudents(@PathVariable Long id) {
        List<Client> students = scholarshipService.getEligibleStudents(id);
        List<Object> result = students.stream().map(c -> {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("id", c.getId());
            map.put("studentId", c.getStudentId());
            map.put("name", c.getFirstName() + " " + c.getLastName());
            map.put("email", c.getEmail());
            map.put("phone", c.getPhoneNumber());
            map.put("faculty", c.getFaculty());
            map.put("programme", c.getProgramme());
            map.put("yearOfStudy", c.getYearOfStudy());
            map.put("gpa", c.getGpa());
            map.put("riskLevel", c.getRiskLevel());
            map.put("riskScore", c.getRiskScore());
            map.put("totalSessions", c.getTotalSessions());
            return (Object) map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result, "Eligible students retrieved"));
    }

    // ---- Recommendation endpoints ----

    @PostMapping("/recommendations")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Recommend a student for a scholarship")
    public ResponseEntity<ApiResponse<ScholarshipRecommendationResponse>> createRecommendation(
            @Valid @RequestBody ScholarshipRecommendationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long counselorId = resolveUserId(userDetails);
        ScholarshipRecommendationResponse response = scholarshipService.createRecommendation(request, counselorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Student recommended successfully"));
    }

    @GetMapping("/recommendations")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get all recommendations")
    public ResponseEntity<ApiResponse<List<ScholarshipRecommendationResponse>>> getAllRecommendations() {
        return ResponseEntity.ok(ApiResponse.success(scholarshipService.getAllRecommendations(), "Recommendations retrieved"));
    }

    @GetMapping("/recommendations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get recommendation by ID")
    public ResponseEntity<ApiResponse<ScholarshipRecommendationResponse>> getRecommendation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(scholarshipService.getRecommendation(id)));
    }

    @GetMapping("/{scholarshipId}/recommendations")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get all recommendations for a scholarship, ranked by vulnerability score")
    public ResponseEntity<ApiResponse<List<ScholarshipRecommendationResponse>>> getRecommendationsByScholarship(
            @PathVariable Long scholarshipId) {
        return ResponseEntity.ok(ApiResponse.success(
                scholarshipService.getRecommendationsByScholarship(scholarshipId),
                "Recommendations retrieved"));
    }

    @GetMapping("/recommendations/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get all scholarship recommendations for a student")
    public ResponseEntity<ApiResponse<List<ScholarshipRecommendationResponse>>> getRecommendationsByClient(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(
                scholarshipService.getRecommendationsByClient(clientId),
                "Student recommendations retrieved"));
    }

    @GetMapping("/recommendations/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get recommendations by status")
    public ResponseEntity<ApiResponse<List<ScholarshipRecommendationResponse>>> getRecommendationsByStatus(
            @PathVariable ScholarshipRecommendation.RecommendationStatus status) {
        return ResponseEntity.ok(ApiResponse.success(scholarshipService.getRecommendationsByStatus(status), "Recommendations retrieved"));
    }

    @PutMapping("/recommendations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Update recommendation details")
    public ResponseEntity<ApiResponse<ScholarshipRecommendationResponse>> updateRecommendation(
            @PathVariable Long id,
            @RequestBody ScholarshipRecommendationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                scholarshipService.updateRecommendation(id, request),
                "Recommendation updated"));
    }

    @PatchMapping("/recommendations/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve, reject, or mark a recommendation as awarded")
    public ResponseEntity<ApiResponse<ScholarshipRecommendationResponse>> updateRecommendationStatus(
            @PathVariable Long id,
            @RequestBody ScholarshipRecommendationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = resolveUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(
                scholarshipService.updateRecommendationStatus(id, request, adminId),
                "Recommendation status updated"));
    }

    @DeleteMapping("/recommendations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Delete a recommendation")
    public ResponseEntity<ApiResponse<Void>> deleteRecommendation(@PathVariable Long id) {
        scholarshipService.deleteRecommendation(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Recommendation deleted"));
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails instanceof User u) return u.getId();
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
