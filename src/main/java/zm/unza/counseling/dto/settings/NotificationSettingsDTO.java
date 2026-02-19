package zm.unza.counseling.dto.settings;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notification settings
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettingsDTO {
    
    @NotNull(message = "Appointment reminders setting is required")
    private Boolean appointmentReminders;
    
    @NotNull(message = "Follow-up notifications setting is required")
    private Boolean followUpNotifications;
    
    @NotNull(message = "Weekly summary setting is required")
    private Boolean weeklySummary;
    
    @NotNull(message = "Email notifications setting is required")
    private Boolean emailNotifications;
    
    @NotNull(message = "SMS notifications setting is required")
    private Boolean smsNotifications;
}
