package zm.unza.counseling.service;

/**
 * Service interface for appointment reminder operations
 */
public interface AppointmentReminderService {
    
    /**
     * Send appointment reminders
     */
    void sendReminders();
    
    /**
     * Send daily appointment summary
     */
    void sendDailySummary();
}