package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.MentalHealthAcademicDtos;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.DashboardStatsResponse;
import zm.unza.counseling.service.AnalyticsService;
import zm.unza.counseling.service.DashboardService;

@RestController
@RequestMapping({"/api/v1/analytics", "/api/analytics", "/v1/analytics", "/analytics"})
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Business intelligence and reporting")
public class AnalyticsController {

    private final DashboardService dashboardService;
    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get analytics overview", description = "Retrieves general system analytics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getAnalyticsOverview() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getStats(), "Analytics overview retrieved successfully"));
    }

    @GetMapping("/intervention-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get intervention report", description = "Provides a summary of required interventions and high-priority cases.")
    public ResponseEntity<ApiResponse<MentalHealthAcademicDtos.InterventionReport>> getInterventionReport() {
        MentalHealthAcademicDtos.InterventionReport report = analyticsService.getInterventionReport();
        return ResponseEntity.ok(ApiResponse.success(report, "Intervention report retrieved successfully"));
    }

    @GetMapping("/counselor-performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get counselor performance analytics", description = "Retrieves performance metrics for counselors")
    public ResponseEntity<ApiResponse<?>> getCounselorPerformanceAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getCounselorPerformanceAnalytics()));
    }

    @GetMapping("/client-demographics")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get client demographics", description = "Retrieves demographic information about clients")
    public ResponseEntity<ApiResponse<?>> getClientDemographics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getClientDemographics()));
    }

    @GetMapping("/session-analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get session analytics", description = "Retrieves analytics about counseling sessions")
    public ResponseEntity<ApiResponse<?>> getSessionAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getSessionAnalytics()));
    }

    @GetMapping("/risk-assessment")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get risk assessment analytics", description = "Retrieves analytics about risk assessments")
    public ResponseEntity<ApiResponse<?>> getRiskAssessmentAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getRiskAssessmentAnalytics()));
    }

    @GetMapping("/time-analysis")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get time analysis", description = "Retrieves time-based analytics")
    public ResponseEntity<ApiResponse<?>> getTimeAnalysis() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTimeAnalysis()));
    }

    @GetMapping("/outcomes")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get outcomes analytics", description = "Retrieves outcome analytics")
    public ResponseEntity<ApiResponse<?>> getOutcomesAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getOutcomesAnalytics()));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export analytics", description = "Export analytics data")
    public ResponseEntity<byte[]> exportAnalytics(@RequestParam(defaultValue = "csv") String format) {
        byte[] data = analyticsService.exportAnalytics(format);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=analytics." + format)
                .body(data);
    }
}