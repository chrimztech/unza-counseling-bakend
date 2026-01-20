package zm.unza.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettingsResponse {
    private Map<String, Object> organization;
    private Map<String, Object> appointments;
    private Map<String, Object> notifications;
    private Map<String, Object> security;
    private List<SettingsCategoryResponse> allSettings;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SettingsCategoryResponse {
        private String category;
        private List<SettingResponse> settings;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SettingResponse {
        private String key;
        private Object value;
        private String type;
        private String description;
        private boolean active;
    }
}