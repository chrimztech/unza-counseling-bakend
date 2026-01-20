package zm.unza.counseling.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zm.unza.counseling.service.DataCleanupService;

/**
 * Scheduled job for automatic data cleanup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataCleanupJob implements Job {

    private final DataCleanupService dataCleanupService;

    /**
     * Perform data cleanup on the 1st of each month at 5:00 AM
     */
    @Scheduled(cron = "0 0 5 1 * ?")
    public void performMonthlyCleanup() {
        log.info("Starting monthly data cleanup job");
        try {
            dataCleanupService.performCleanup();
            log.info("Monthly data cleanup completed successfully");
        } catch (Exception e) {
            log.error("Failed to perform monthly data cleanup", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // This method is called by Quartz scheduler
        log.info("Executing DataCleanupJob via Quartz");
        try {
            performMonthlyCleanup();
        } catch (Exception e) {
            log.error("Error executing DataCleanupJob", e);
            throw new JobExecutionException(e);
        }
    }
}