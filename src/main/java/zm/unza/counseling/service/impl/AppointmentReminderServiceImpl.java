package zm.unza.counseling.service.impl;

import zm.unza.counseling.service.AppointmentReminderService;
import org.springframework.stereotype.Service;

/**
 * Implementation of AppointmentReminderService
 */
@Service
public class AppointmentReminderServiceImpl implements AppointmentReminderService {

    @Override
    public void sendReminders() {
        // Implementation for sending appointment reminders
        // This would typically query upcoming appointments and send notifications
    }

    @Override
    public void sendDailySummary() {
        // Implementation for sending daily appointment summary
        // This would send a summary of today's appointments to counselors
    }
}