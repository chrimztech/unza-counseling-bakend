package zm.unza.counseling.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for user consent information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConsentResponse {

    private Long id;
    private Long userId;
    private Long consentFormId;
    private String consentFormTitle;
    private String consentFormVersion;
    private LocalDateTime consentDate;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getConsentFormId() { return consentFormId; }
    public void setConsentFormId(Long consentFormId) { this.consentFormId = consentFormId; }

    public String getConsentFormTitle() { return consentFormTitle; }
    public void setConsentFormTitle(String consentFormTitle) { this.consentFormTitle = consentFormTitle; }

    public String getConsentFormVersion() { return consentFormVersion; }
    public void setConsentFormVersion(String consentFormVersion) { this.consentFormVersion = consentFormVersion; }

    public LocalDateTime getConsentDate() { return consentDate; }
    public void setConsentDate(LocalDateTime consentDate) { this.consentDate = consentDate; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}