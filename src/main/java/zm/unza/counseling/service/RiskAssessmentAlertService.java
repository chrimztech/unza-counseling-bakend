package zm.unza.counseling.service;

/**
 * Service interface for risk assessment alert operations
 */
public interface RiskAssessmentAlertService {
    
    /**
     * Check for high-risk assessments
     */
    void checkHighRiskAssessments();
    
    /**
     * Send daily risk summary
     */
    void sendDailyRiskSummary();
}