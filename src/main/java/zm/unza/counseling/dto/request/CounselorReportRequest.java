package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CounselorReportRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    private Long counselorId;
    private Long caseId;
    private Long appointmentId;
    private Long sessionId;
    private String caseNumber;

    @NotBlank(message = "Background information is required")
    private String descriptionBackground;

    @NotBlank(message = "Presenting problem is required")
    private String presentingProblem;

    private String assessment;

    @NotBlank(message = "Conclusion is required")
    private String conclusion;

    @NotBlank(message = "Recommendations are required")
    private String recommendations;

    private String sessionSummary;
    private String progressNotes;
    private String followUpPlan;
    private String riskAssessment;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getCounselorId() {
        return counselorId;
    }

    public void setCounselorId(Long counselorId) {
        this.counselorId = counselorId;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getDescriptionBackground() {
        return descriptionBackground;
    }

    public void setDescriptionBackground(String descriptionBackground) {
        this.descriptionBackground = descriptionBackground;
    }

    public String getPresentingProblem() {
        return presentingProblem;
    }

    public void setPresentingProblem(String presentingProblem) {
        this.presentingProblem = presentingProblem;
    }

    public String getAssessment() {
        return assessment;
    }

    public void setAssessment(String assessment) {
        this.assessment = assessment;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getSessionSummary() {
        return sessionSummary;
    }

    public void setSessionSummary(String sessionSummary) {
        this.sessionSummary = sessionSummary;
    }

    public String getProgressNotes() {
        return progressNotes;
    }

    public void setProgressNotes(String progressNotes) {
        this.progressNotes = progressNotes;
    }

    public String getFollowUpPlan() {
        return followUpPlan;
    }

    public void setFollowUpPlan(String followUpPlan) {
        this.followUpPlan = followUpPlan;
    }

    public String getRiskAssessment() {
        return riskAssessment;
    }

    public void setRiskAssessment(String riskAssessment) {
        this.riskAssessment = riskAssessment;
    }
}
