package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for signing a consent form
 */
@Data
public class SignConsentRequest {

    @NotNull(message = "Consent form ID is required")
    private Long consentFormId;

    private String ipAddress;
    private String userAgent;

    // Getters and Setters
    public Long getConsentFormId() { return consentFormId; }
    public void setConsentFormId(Long consentFormId) { this.consentFormId = consentFormId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}