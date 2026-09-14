package zm.unza.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesDto {
    private String userId;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean pushNotifications;
    private Boolean appointmentReminders;
    private Boolean sessionUpdates;
    private Boolean riskAlerts;
    private Boolean messageNotifications;
    private Boolean systemUpdates;
    private Integer reminderHoursBefore;
}
