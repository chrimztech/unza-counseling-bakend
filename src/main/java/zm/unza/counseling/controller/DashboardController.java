package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.DashboardStatsResponse;
import zm.unza.counseling.service.DashboardService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse stats = dashboardService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard statistics retrieved successfully"));
    }

    @GetMapping("/high-risk-clients")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<?>>> getHighRiskClients() {
        log.info("Fetching high-risk clients for dashboard");
        List<?> highRiskClients = dashboardService.getHighRiskClients();
        return ResponseEntity.ok(ApiResponse.success(highRiskClients, "High-risk clients retrieved successfully"));
    }

    @GetMapping("/recent-clients")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<?>>> getRecentClients() {
        log.info("Fetching recent clients for dashboard");
        List<?> recentClients = dashboardService.getRecentClients();
        return ResponseEntity.ok(ApiResponse.success(recentClients, "Recent clients retrieved successfully"));
    }

    @GetMapping("/performance-metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPerformanceMetrics() {
        log.info("Fetching performance metrics for dashboard");
        Map<String, Object> metrics = dashboardService.getPerformanceMetrics();
        return ResponseEntity.ok(ApiResponse.success(metrics, "Performance metrics retrieved successfully"));
    }

    @GetMapping("/upcoming-appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<?>>> getUpcomingAppointments() {
        log.info("Fetching upcoming appointments for dashboard");
        List<?> appointments = dashboardService.getUpcomingAppointments();
        return ResponseEntity.ok(ApiResponse.success(appointments, "Upcoming appointments retrieved successfully"));
    }
}
