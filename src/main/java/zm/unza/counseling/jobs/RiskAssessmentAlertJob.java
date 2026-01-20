package zm.unza.counseling.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zm.unza.counseling.service.RiskAssessmentAlertService;

/**
 * Scheduled job for risk assessment alerts
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentAlertJob implements Job {

    private final RiskAssessmentAlertService riskAssessmentAlertService;

    /**
     * Check for high-risk assessments every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkHighRiskAssessments() {
        log.info("Starting high-risk assessment check");
        try {
            riskAssessmentAlertService.checkHighRiskAssessments();
            log.info("High-risk assessment check completed");
        } catch (Exception e) {
            log.error("Failed to check high-risk assessments", e);
        }
    }

    /**
     * Send daily risk summary at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyRiskSummary() {
        log.info("Starting daily risk summary generation");
        try {
            riskAssessmentAlertService.sendDailyRiskSummary();
            log.info("Daily risk summary sent successfully");
        } catch (Exception e) {
            log.error("Failed to send daily risk summary", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // This method is called by Quartz scheduler
        log.info("Executing RiskAssessmentAlertJob via Quartz");
        try {
            checkHighRiskAssessments();
        } catch (Exception e) {
            log.error("Error executing RiskAssessmentAlertJob", e);
            throw new JobExecutionException(e);
        }
    }
}