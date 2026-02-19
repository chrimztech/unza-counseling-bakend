package zm.unza.counseling.dto.settings;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for organization settings
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationSettingsDTO {
    
    @NotBlank(message = "Organization name is required")
    private String organizationName;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Contact email is required")
    private String contactEmail;
    
    @NotBlank(message = "Contact phone is required")
    private String contactPhone;
    
    private OperatingHoursDTO operatingHours;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OperatingHoursDTO {
        private String openTime;  // Format: "HH:mm"
        private String closeTime; // Format: "HH:mm"
    }
}
