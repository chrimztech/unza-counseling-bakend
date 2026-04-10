package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for anonymous login
 */
@Data
public class AnonymousLoginRequest {

    @NotBlank(message = "Device identifier is required")
    private String deviceIdentifier;

    private String displayName;
    private String ipAddress;
    private String userAgent;

    // Getters and Setters
    public String getDeviceIdentifier() { return deviceIdentifier; }
    public void setDeviceIdentifier(String deviceIdentifier) { this.deviceIdentifier = deviceIdentifier; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
