package zm.unza.counseling.dto.response;

/**
 * Response DTO for individual settings items
 */
public class SettingsItemResponse {

    private String key;
    private String value;
    private String type;
    private String description;

    // Constructors
    public SettingsItemResponse() {}

    public SettingsItemResponse(String key, String value, String type, String description) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.description = description;
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}