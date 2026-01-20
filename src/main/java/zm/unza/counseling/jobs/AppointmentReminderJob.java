package zm.unza.counseling.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zm.unza.counseling.service.AppointmentReminderService;

/**
 * Scheduled job for appointment reminders
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderJob implements Job {

    private final AppointmentReminderService appointmentReminderService;

    /**
     * Send appointment reminders 1 hour before scheduled time
     */
    @Scheduled(cron = "0 0 * * * *")
    public void sendAppointmentReminders() {
        log.info("Starting appointment reminder check");
        try {
            appointmentReminderService.sendReminders();
            log.info("Appointment reminders sent successfully");
        } catch (Exception e) {
            log.error("Failed to send appointment reminders", e);
        }
    }

    /**
     * Send daily appointment summary at 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyAppointmentSummary() {
        log.info("Starting daily appointment summary");
        try {
            appointmentReminderService.sendDailySummary();
            log.info("Daily appointment summary sent successfully");
        } catch (Exception e) {
            log.error("Failed to send daily appointment summary", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // This method is called by Quartz scheduler
        log.info("Executing AppointmentReminderJob via Quartz");
        try {
            sendAppointmentReminders();
        } catch (Exception e) {
            log.error("Error executing AppointmentReminderJob", e);
            throw new JobExecutionException(e);
        }
    }
}