package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.AcademicPerformanceSummary;
import zm.unza.counseling.dto.AcademicStatistics;
import zm.unza.counseling.dto.GpaTrendData;
import zm.unza.counseling.dto.StudentAtRiskDto;
import zm.unza.counseling.dto.request.AcademicPerformanceRequest;
import zm.unza.counseling.dto.response.AcademicPerformanceResponse;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.service.AcademicPerformanceService;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/academic-performance")
@RequiredArgsConstructor
@Tag(name = "Academic Performance", description = "APIs for managing student academic performance records")
public class AcademicPerformanceController {

    private final AcademicPerformanceService academicPerformanceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Create academic performance record", description = "Creates a new academic performance record for a student")
    public ResponseEntity<ApiResponse<AcademicPerformanceResponse>> createAcademicPerformance(
            @Valid @RequestBody AcademicPerformanceRequest request) {
        AcademicPerformanceResponse response = academicPerformanceService.createAcademicPerformance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Academic performance record created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    @Operation(summary = "Get academic performance by ID", description = "Retrieves a specific academic performance record")
    public ResponseEntity<ApiResponse<AcademicPerformanceResponse>> getById(@PathVariable Long id) {
        AcademicPerformanceResponse response = academicPerformanceService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Academic performance retrieved successfully"));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    @Operation(summary = "Get all records for a client", description = "Retrieves all academic performance records for a specific client")
    public ResponseEntity<ApiResponse<List<AcademicPerformanceResponse>>> getByClientId(@PathVariable Long clientId) {
        List<AcademicPerformanceResponse> response = academicPerformanceService.getByClientId(clientId);
        return ResponseEntity.ok(ApiResponse.success(response, "Academic performance records retrieved successfully"));
    }

    @GetMapping("/client/{clientId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    @Operation(summary = "Get paginated records for a client", description = "Retrieves paginated academic performance records")
    public ResponseEntity<ApiResponse<Page<AcademicPerformanceResponse>>> getByClientIdPaginated(
            @PathVariable Long clientId, Pageable pageable) {
        Page<AcademicPerformanceResponse> response = academicPerformanceService.getByClientIdPaginated(clientId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Academic performance records retrieved successfully"));
    }

    @GetMapping("/client/{clientId}/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    @Operation(summary = "Get latest record for a client", description = "Retrieves the most recent academic performance record")
    public ResponseEntity<ApiResponse<AcademicPerformanceResponse>> getLatestForClient(@PathVariable Long clientId) {
        AcademicPerformanceResponse response = academicPerformanceService.getLatestForClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(response, "Latest academic performance retrieved successfully"));
    }

    @GetMapping("/client/{clientId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    @Operation(summary = "Get client summary", description = "Retrieves an academic performance summary for a client")
    public ResponseEntity<ApiResponse<AcademicPerformanceSummary>> getClientSummary(@PathVariable Long clientId) {
        AcademicPerformanceSummary response = academicPerformanceService.getClientSummary(clientId);
        return ResponseEntity.ok(ApiResponse.success(response, "Academic summary retrieved successfully"));
    }

    @GetMapping("/client/{clientId}/gpa-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    @Operation(summary = "Get GPA trend", description = "Retrieves GPA trend data for a client")
    public ResponseEntity<ApiResponse<List<GpaTrendData>>> getGpaTrend(@PathVariable Long clientId) {
        List<GpaTrendData> response = academicPerformanceService.getGpaTrend(clientId);
        return ResponseEntity.ok(ApiResponse.success(response, "GPA trend retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Update academic performance", description = "Updates an existing academic performance record")
    public ResponseEntity<ApiResponse<AcademicPerformanceResponse>> updateAcademicPerformance(
            @PathVariable Long id,
            @Valid @RequestBody AcademicPerformanceRequest request) {
        AcademicPerformanceResponse response = academicPerformanceService.updateAcademicPerformance(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Academic performance updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete academic performance", description = "Deletes an academic performance record")
    public ResponseEntity<ApiResponse<Void>> deleteAcademicPerformance(@PathVariable Long id) {
        academicPerformanceService.deleteAcademicPerformance(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Academic performance deleted successfully"));
    }

    @GetMapping("/at-risk")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get students at risk", description = "Retrieves all students with at-risk academic standing")
    public ResponseEntity<ApiResponse<List<StudentAtRiskDto>>> getStudentsAtRisk() {
        List<StudentAtRiskDto> response = academicPerformanceService.getStudentsAtRisk();
        return ResponseEntity.ok(ApiResponse.success(response, "At-risk students retrieved successfully"));
    }

    @GetMapping("/low-gpa")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get students with low GPA", description = "Retrieves students with GPA below threshold")
    public ResponseEntity<ApiResponse<List<AcademicPerformanceResponse>>> getStudentsWithLowGpa(
            @RequestParam(defaultValue = "2.0") BigDecimal threshold) {
        List<AcademicPerformanceResponse> response = academicPerformanceService.getStudentsWithLowGpa(threshold);
        return ResponseEntity.ok(ApiResponse.success(response, "Low GPA students retrieved successfully"));
    }

    @GetMapping("/faculty/{faculty}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get by faculty", description = "Retrieves academic performance records by faculty")
    public ResponseEntity<ApiResponse<List<AcademicPerformanceResponse>>> getByFaculty(@PathVariable String faculty) {
        List<AcademicPerformanceResponse> response = academicPerformanceService.getByFaculty(faculty);
        return ResponseEntity.ok(ApiResponse.success(response, "Records retrieved successfully"));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get statistics", description = "Retrieves overall academic performance statistics")
    public ResponseEntity<ApiResponse<AcademicStatistics>> getStatistics() {
        AcademicStatistics response = academicPerformanceService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(response, "Statistics retrieved successfully"));
    }
}