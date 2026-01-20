package zm.unza.counseling.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettingsRequest {
    private String key;
    private Object value;
    private String type;
    private String category;
    private String description;
    private boolean active;
}