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
}
