package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.DashboardWidgetConfigDto;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.UserDashboardConfig;
import zm.unza.counseling.repository.UserDashboardConfigRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/dashboard", "/v1/dashboard", "/dashboard"})
@RequiredArgsConstructor
public class DashboardConfigController {

    private final UserDashboardConfigRepository dashboardConfigRepository;

    /**
     * Get user's dashboard widget configuration
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<DashboardWidgetConfigDto>> getDashboardConfig(
            @RequestParam Long userId) {
        List<UserDashboardConfig> configs = dashboardConfigRepository.findByUserIdOrderByPositionYAscPositionXAsc(userId);
        
        List<DashboardWidgetConfigDto.WidgetConfig> widgets = configs.stream()
                .map(config -> DashboardWidgetConfigDto.WidgetConfig.builder()
                        .widgetId(config.getWidgetId())
                        .widgetType(config.getWidgetType())
                        .positionX(config.getPositionX())
                        .positionY(config.getPositionY())
                        .width(config.getWidth())
                        .height(config.getHeight())
                        .visible(config.getVisible())
                        .configJson(config.getConfigJson())
                        .build())
                .collect(Collectors.toList());
        
        DashboardWidgetConfigDto response = DashboardWidgetConfigDto.builder()
                .userId(userId)
                .widgets(widgets)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Save/update user's dashboard widget configuration
     */
    @PostMapping("/config")
    public ResponseEntity<ApiResponse<DashboardWidgetConfigDto>> saveDashboardConfig(
            @RequestBody DashboardWidgetConfigDto request) {
        
        // Clear existing config for this user and save new one
        if (request.getWidgets() != null) {
            for (DashboardWidgetConfigDto.WidgetConfig widget : request.getWidgets()) {
                UserDashboardConfig config = dashboardConfigRepository
                        .findByUserIdAndWidgetId(request.getUserId(), widget.getWidgetId())
                        .orElse(new UserDashboardConfig());
                
                config.setUserId(request.getUserId());
                config.setWidgetId(widget.getWidgetId());
                config.setWidgetType(widget.getWidgetType());
                config.setPositionX(widget.getPositionX());
                config.setPositionY(widget.getPositionY());
                config.setWidth(widget.getWidth());
                config.setHeight(widget.getHeight());
                config.setVisible(widget.getVisible() != null ? widget.getVisible() : true);
                config.setConfigJson(widget.getConfigJson());
                
                dashboardConfigRepository.save(config);
            }
        }
        
        return getDashboardConfig(request.getUserId());
    }

    /**
     * Reset dashboard to default
     */
    @DeleteMapping("/config")
    public ResponseEntity<ApiResponse<String>> resetDashboard(
            @RequestParam Long userId) {
        List<UserDashboardConfig> configs = dashboardConfigRepository.findByUserIdOrderByPositionYAscPositionXAsc(userId);
        dashboardConfigRepository.deleteAll(configs);
        return ResponseEntity.ok(ApiResponse.success("Dashboard reset successfully"));
    }
}
