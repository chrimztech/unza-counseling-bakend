package zm.unza.counseling.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for all settings combined response
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllSettingsDTO {
    private OrganizationSettingsDTO organization;
    private AppointmentSettingsDTO appointments;
    private NotificationSettingsDTO notifications;
    private SecuritySettingsDTO security;
}
