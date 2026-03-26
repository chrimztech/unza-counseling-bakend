package zm.unza.counseling.dto.settings;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for security settings
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecuritySettingsDTO {
    
    @NotNull(message = "Data retention period is required")
    @Min(value = 1, message = "Data retention period must be at least 1 year")
    @Max(value = 10, message = "Data retention period cannot exceed 10 years")
    private Integer dataRetentionPeriod;
    
    @NotNull(message = "Encryption enabled setting is required")
    private Boolean encryptionEnabled;
    
    @NotNull(message = "Audit logging enabled setting is required")
    private Boolean auditLoggingEnabled;
    
    /**
     * Session timeout in minutes (0 means no timeout)
     */
    @Min(value = 0, message = "Session timeout cannot be negative")
    @Max(value = 1440, message = "Session timeout cannot exceed 24 hours")
    private Integer sessionTimeoutMinutes;
    
    /**
     * Enable session timeout
     */
    private Boolean sessionTimeoutEnabled;
    
    /**
     * Show warning before session timeout (in minutes)
     */
    @Min(value = 1, message = "Warning time must be at least 1 minute")
    @Max(value = 30, message = "Warning time cannot exceed 30 minutes")
    private Integer sessionWarningMinutes;
}
