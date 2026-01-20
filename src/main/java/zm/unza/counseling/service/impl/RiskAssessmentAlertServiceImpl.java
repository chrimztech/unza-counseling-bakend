package zm.unza.counseling.service.impl;

import zm.unza.counseling.service.RiskAssessmentAlertService;
import org.springframework.stereotype.Service;

/**
 * Implementation of RiskAssessmentAlertService
 */
@Service
public class RiskAssessmentAlertServiceImpl implements RiskAssessmentAlertService {

    @Override
    public void checkHighRiskAssessments() {
        // Implementation for checking high-risk assessments and sending alerts
    }

    @Override
    public void sendDailyRiskSummary() {
        // Implementation for sending daily risk summary to administrators
    }
}