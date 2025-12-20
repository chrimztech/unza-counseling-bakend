package zm.unza.counseling.jobs;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import zm.unza.counseling.service.RiskAssessmentService;
import zm.unza.counseling.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RiskAssessmentAlertJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentAlertJob.class);

    private final RiskAssessmentService riskAssessmentService;
    private final NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Starting risk assessment alert job at {}", LocalDateTime.now());
        
        try {
            // Get high-risk assessments from the last 24 hours
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            List<String> highRiskClients = riskAssessmentService.getHighRiskClientsSince(yesterday);
            
            for (String clientId : highRiskClients) {
                notificationService.sendSystemNotification(
                    clientId,
                    "High Risk Alert",
                    "Your recent assessment indicates high risk. Please contact your counselor immediately.",
                    "HIGH"
                );
            }
            
            log.info("Risk assessment alert job completed. Notified {} high-risk clients", highRiskClients.size());
        } catch (Exception e) {
            log.error("Error in risk assessment alert job", e);
        }
    }
}