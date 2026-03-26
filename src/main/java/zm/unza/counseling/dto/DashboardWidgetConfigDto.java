package zm.unza.counseling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for dashboard widget configuration
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardWidgetConfigDto {
    
    private Long userId;
    private List<WidgetConfig> widgets;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WidgetConfig {
        private String widgetId;
        private String widgetType;
        private Integer positionX;
        private Integer positionY;
        private Integer width;
        private Integer height;
        private Boolean visible;
        private String configJson;
    }
}
