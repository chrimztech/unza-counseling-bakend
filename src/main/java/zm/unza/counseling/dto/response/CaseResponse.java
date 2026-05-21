package zm.unza.counseling.dto.response;

import zm.unza.counseling.entity.Case;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CaseResponse {

    private Long id;

    private String caseNumber;

    private Long clientId;

    private String clientName;

    private String clientEmail;

    private Long counselorId;

    private String counselorName;

    private Long assignedBy;

    private String assignedByName;

    private Case.CaseStatus status;

    private Case.CasePriority priority;

    private String subject;

    private String description;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime assignedAt;

    private LocalDateTime lastActivityAt;

    private LocalDateTime expectedResolutionDate;

    private LocalDateTime actualResolutionDate;

    private LocalDateTime closedAt;

    private Integer escalationLevel;

    private String tags;

    private String customFields;

    private Integer appointmentCount;

    // Clinical fields
    private String presentingProblem;
    private String clinicalImpression;
    private Case.TreatmentModality treatmentModality;
    private String treatmentGoals;
    private String treatmentPlan;
    private Case.RiskAssessmentLevel riskLevel;
    private String riskNotes;
    private String crisisPlan;
    private String referralSource;
    private String referralNotes;
    private String previousCounselingHistory;
    private String medicationNotes;
    private LocalDate intakeDate;
    private Boolean consentObtained;
    private LocalDate consentDate;
    private Boolean confidential;
    private LocalDateTime reviewDate;
    private String outcomeAtClosure;
    private String dischargeReason;
    private String dischargeSummary;
    private LocalDateTime followUpDate;
    private String followUpNotes;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public Long getCounselorId() {
        return counselorId;
    }

    public void setCounselorId(Long counselorId) {
        this.counselorId = counselorId;
    }

    public String getCounselorName() {
        return counselorName;
    }

    public void setCounselorName(String counselorName) {
        this.counselorName = counselorName;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getAssignedByName() {
        return assignedByName;
    }

    public void setAssignedByName(String assignedByName) {
        this.assignedByName = assignedByName;
    }

    public Case.CaseStatus getStatus() {
        return status;
    }

    public void setStatus(Case.CaseStatus status) {
        this.status = status;
    }

    public Case.CasePriority getPriority() {
        return priority;
    }

    public void setPriority(Case.CasePriority priority) {
        this.priority = priority;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public LocalDateTime getExpectedResolutionDate() {
        return expectedResolutionDate;
    }

    public void setExpectedResolutionDate(LocalDateTime expectedResolutionDate) {
        this.expectedResolutionDate = expectedResolutionDate;
    }

    public LocalDateTime getActualResolutionDate() {
        return actualResolutionDate;
    }

    public void setActualResolutionDate(LocalDateTime actualResolutionDate) {
        this.actualResolutionDate = actualResolutionDate;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public Integer getEscalationLevel() {
        return escalationLevel;
    }

    public void setEscalationLevel(Integer escalationLevel) {
        this.escalationLevel = escalationLevel;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCustomFields() {
        return customFields;
    }

    public void setCustomFields(String customFields) {
        this.customFields = customFields;
    }

    public Integer getAppointmentCount() {
        return appointmentCount;
    }

    public void setAppointmentCount(Integer appointmentCount) {
        this.appointmentCount = appointmentCount;
    }

    public String getPresentingProblem() { return presentingProblem; }
    public void setPresentingProblem(String presentingProblem) { this.presentingProblem = presentingProblem; }

    public String getClinicalImpression() { return clinicalImpression; }
    public void setClinicalImpression(String clinicalImpression) { this.clinicalImpression = clinicalImpression; }

    public Case.TreatmentModality getTreatmentModality() { return treatmentModality; }
    public void setTreatmentModality(Case.TreatmentModality treatmentModality) { this.treatmentModality = treatmentModality; }

    public String getTreatmentGoals() { return treatmentGoals; }
    public void setTreatmentGoals(String treatmentGoals) { this.treatmentGoals = treatmentGoals; }

    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }

    public Case.RiskAssessmentLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(Case.RiskAssessmentLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getRiskNotes() { return riskNotes; }
    public void setRiskNotes(String riskNotes) { this.riskNotes = riskNotes; }

    public String getCrisisPlan() { return crisisPlan; }
    public void setCrisisPlan(String crisisPlan) { this.crisisPlan = crisisPlan; }

    public String getReferralSource() { return referralSource; }
    public void setReferralSource(String referralSource) { this.referralSource = referralSource; }

    public String getReferralNotes() { return referralNotes; }
    public void setReferralNotes(String referralNotes) { this.referralNotes = referralNotes; }

    public String getPreviousCounselingHistory() { return previousCounselingHistory; }
    public void setPreviousCounselingHistory(String previousCounselingHistory) { this.previousCounselingHistory = previousCounselingHistory; }

    public String getMedicationNotes() { return medicationNotes; }
    public void setMedicationNotes(String medicationNotes) { this.medicationNotes = medicationNotes; }

    public LocalDate getIntakeDate() { return intakeDate; }
    public void setIntakeDate(LocalDate intakeDate) { this.intakeDate = intakeDate; }

    public Boolean getConsentObtained() { return consentObtained; }
    public void setConsentObtained(Boolean consentObtained) { this.consentObtained = consentObtained; }

    public LocalDate getConsentDate() { return consentDate; }
    public void setConsentDate(LocalDate consentDate) { this.consentDate = consentDate; }

    public Boolean getConfidential() { return confidential; }
    public void setConfidential(Boolean confidential) { this.confidential = confidential; }

    public LocalDateTime getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDateTime reviewDate) { this.reviewDate = reviewDate; }

    public String getOutcomeAtClosure() { return outcomeAtClosure; }
    public void setOutcomeAtClosure(String outcomeAtClosure) { this.outcomeAtClosure = outcomeAtClosure; }

    public String getDischargeReason() { return dischargeReason; }
    public void setDischargeReason(String dischargeReason) { this.dischargeReason = dischargeReason; }

    public String getDischargeSummary() { return dischargeSummary; }
    public void setDischargeSummary(String dischargeSummary) { this.dischargeSummary = dischargeSummary; }

    public LocalDateTime getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDateTime followUpDate) { this.followUpDate = followUpDate; }

    public String getFollowUpNotes() { return followUpNotes; }
    public void setFollowUpNotes(String followUpNotes) { this.followUpNotes = followUpNotes; }
}
