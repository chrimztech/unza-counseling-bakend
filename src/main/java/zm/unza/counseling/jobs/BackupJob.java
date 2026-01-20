package zm.unza.counseling.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zm.unza.counseling.service.BackupService;

/**
 * Scheduled job for automatic data backup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BackupJob implements Job {

    private final BackupService backupService;

    /**
     * Perform daily backup at 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void performDailyBackup() {
        log.info("Starting daily backup job");
        try {
            backupService.performBackup("daily");
            log.info("Daily backup completed successfully");
        } catch (Exception e) {
            log.error("Failed to perform daily backup", e);
        }
    }

    /**
     * Perform weekly backup on Sundays at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 ? * SUN")
    public void performWeeklyBackup() {
        log.info("Starting weekly backup job");
        try {
            backupService.performBackup("weekly");
            log.info("Weekly backup completed successfully");
        } catch (Exception e) {
            log.error("Failed to perform weekly backup", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // This method is called by Quartz scheduler
        log.info("Executing BackupJob via Quartz");
        try {
            performDailyBackup();
        } catch (Exception e) {
            log.error("Error executing BackupJob", e);
            throw new JobExecutionException(e);
        }
    }
}