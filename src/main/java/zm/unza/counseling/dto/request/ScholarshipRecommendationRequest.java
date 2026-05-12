package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.NotNull;
import zm.unza.counseling.entity.ScholarshipRecommendation;

public class ScholarshipRecommendationRequest {

    @NotNull(message = "Scholarship ID is required")
    private Long scholarshipId;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    private String justification;

    private ScholarshipRecommendation.FinancialNeedLevel financialNeedLevel;

    private Integer vulnerabilityScore;

    private String academicStanding;

    private String personalStatement;

    private String supportingNotes;

    private ScholarshipRecommendation.RecommendationStatus status;

    private String rejectionReason;

    public Long getScholarshipId() { return scholarshipId; }
    public void setScholarshipId(Long scholarshipId) { this.scholarshipId = scholarshipId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public ScholarshipRecommendation.FinancialNeedLevel getFinancialNeedLevel() { return financialNeedLevel; }
    public void setFinancialNeedLevel(ScholarshipRecommendation.FinancialNeedLevel financialNeedLevel) { this.financialNeedLevel = financialNeedLevel; }

    public Integer getVulnerabilityScore() { return vulnerabilityScore; }
    public void setVulnerabilityScore(Integer vulnerabilityScore) { this.vulnerabilityScore = vulnerabilityScore; }

    public String getAcademicStanding() { return academicStanding; }
    public void setAcademicStanding(String academicStanding) { this.academicStanding = academicStanding; }

    public String getPersonalStatement() { return personalStatement; }
    public void setPersonalStatement(String personalStatement) { this.personalStatement = personalStatement; }

    public String getSupportingNotes() { return supportingNotes; }
    public void setSupportingNotes(String supportingNotes) { this.supportingNotes = supportingNotes; }

    public ScholarshipRecommendation.RecommendationStatus getStatus() { return status; }
    public void setStatus(ScholarshipRecommendation.RecommendationStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
