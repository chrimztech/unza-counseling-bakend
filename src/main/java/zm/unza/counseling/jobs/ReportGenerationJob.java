package zm.unza.counseling.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zm.unza.counseling.service.ReportService;

/**
 * Scheduled job for automatic report generation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationJob implements Job {

    private final ReportService reportService;

    /**
     * Generate daily reports at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void generateDailyReports() {
        log.info("Starting daily report generation job");
        try {
            reportService.generateScheduledReports("daily");
            log.info("Daily report generation completed successfully");
        } catch (Exception e) {
            log.error("Failed to generate daily reports", e);
        }
    }

    /**
     * Generate weekly reports on Sundays at 3:00 AM
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void generateWeeklyReports() {
        log.info("Starting weekly report generation job");
        try {
            reportService.generateScheduledReports("weekly");
            log.info("Weekly report generation completed successfully");
        } catch (Exception e) {
            log.error("Failed to generate weekly reports", e);
        }
    }

    /**
     * Generate monthly reports on the 1st of each month at 4:00 AM
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void generateMonthlyReports() {
        log.info("Starting monthly report generation job");
        try {
            reportService.generateScheduledReports("monthly");
            log.info("Monthly report generation completed successfully");
        } catch (Exception e) {
            log.error("Failed to generate monthly reports", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // This method is called by Quartz scheduler
        log.info("Executing ReportGenerationJob via Quartz");
        try {
            generateDailyReports();
        } catch (Exception e) {
            log.error("Error executing ReportGenerationJob", e);
            throw new JobExecutionException(e);
        }
    }
}