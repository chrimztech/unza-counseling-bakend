package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.MentalHealthAcademicDtos;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.DashboardStatsResponse;
import zm.unza.counseling.service.AnalyticsService;
import zm.unza.counseling.service.DashboardService;

@RestController
@RequestMapping("/api/analytics")
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
}