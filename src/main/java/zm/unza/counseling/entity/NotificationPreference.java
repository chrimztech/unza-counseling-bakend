package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "email_notifications", nullable = false)
    private Boolean emailNotifications = true;

    @Column(name = "sms_notifications", nullable = false)
    private Boolean smsNotifications = false;

    @Column(name = "push_notifications", nullable = false)
    private Boolean pushNotifications = true;

    @Column(name = "appointment_reminders", nullable = false)
    private Boolean appointmentReminders = true;

    @Column(name = "session_updates", nullable = false)
    private Boolean sessionUpdates = true;

    @Column(name = "risk_alerts", nullable = false)
    private Boolean riskAlerts = true;

    @Column(name = "message_notifications", nullable = false)
    private Boolean messageNotifications = true;

    @Column(name = "system_updates", nullable = false)
    private Boolean systemUpdates = true;

    @Column(name = "reminder_hours_before", nullable = false)
    private Integer reminderHoursBefore = 24;
}
