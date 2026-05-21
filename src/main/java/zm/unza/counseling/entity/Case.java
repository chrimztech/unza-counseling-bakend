package zm.unza.counseling.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cases")
@EntityListeners(AuditingEntityListener.class)
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", unique = true, nullable = false)
    private String caseNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id")
    private Counselor counselor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CaseStatus status = CaseStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private CasePriority priority = CasePriority.MEDIUM;

    @Column(name = "subject", length = 200)
    private String subject;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "notes", length = 5000)
    private String notes;

    @Column(name = "assigned_by")
    private Long assignedBy;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "expected_resolution_date")
    private LocalDateTime expectedResolutionDate;

    @Column(name = "actual_resolution_date")
    private LocalDateTime actualResolutionDate;

    @Column(name = "escalation_level")
    private Integer escalationLevel = 0;

    @Column(name = "tags", length = 1000)
    private String tags;

    @Column(name = "custom_fields", length = 2000)
    private String customFields;

    // ── Clinical / Record-keeping fields ───────────────────────────────────

    @Column(name = "presenting_problem", columnDefinition = "text")
    private String presentingProblem;

    @Column(name = "clinical_impression", columnDefinition = "text")
    private String clinicalImpression;

    @Enumerated(EnumType.STRING)
    @Column(name = "treatment_modality", length = 50)
    private TreatmentModality treatmentModality;

    @Column(name = "treatment_goals", columnDefinition = "text")
    private String treatmentGoals;

    @Column(name = "treatment_plan", columnDefinition = "text")
    private String treatmentPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    private RiskAssessmentLevel riskLevel;

    @Column(name = "risk_notes", columnDefinition = "text")
    private String riskNotes;

    @Column(name = "crisis_plan", columnDefinition = "text")
    private String crisisPlan;

    @Column(name = "referral_source", length = 200)
    private String referralSource;

    @Column(name = "referral_notes", columnDefinition = "text")
    private String referralNotes;

    @Column(name = "previous_counseling_history", columnDefinition = "text")
    private String previousCounselingHistory;

    @Column(name = "medication_notes", columnDefinition = "text")
    private String medicationNotes;

    @Column(name = "intake_date")
    private LocalDate intakeDate;

    @Column(name = "consent_obtained")
    private Boolean consentObtained;

    @Column(name = "consent_date")
    private LocalDate consentDate;

    @Column(name = "confidential")
    private Boolean confidential = false;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(name = "outcome_at_closure", columnDefinition = "text")
    private String outcomeAtClosure;

    @Column(name = "discharge_reason", columnDefinition = "text")
    private String dischargeReason;

    @Column(name = "discharge_summary", columnDefinition = "text")
    private String dischargeSummary;

    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;

    @Column(name = "follow_up_notes", columnDefinition = "text")
    private String followUpNotes;

    // ── Audit ──────────────────────────────────────────────────────────────

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "caseEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();

    public enum CaseStatus {
        OPEN,
        IN_PROGRESS,
        CLOSED,
        ON_HOLD,
        RESOLVED,
        REFERRED
    }

    public enum CasePriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum TreatmentModality {
        COGNITIVE_BEHAVIOURAL,
        PERSON_CENTRED,
        PSYCHODYNAMIC,
        SOLUTION_FOCUSED,
        DIALECTICAL_BEHAVIOUR,
        MINDFULNESS_BASED,
        CRISIS_INTERVENTION,
        PSYCHOEDUCATION,
        GROUP_THERAPY,
        INTEGRATIVE,
        OTHER
    }

    public enum RiskAssessmentLevel {
        LOW,
        MODERATE,
        HIGH,
        CRISIS
    }

    @PrePersist
    public void generateCaseNumber() {
        if (caseNumber == null) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String random = String.format("%04d", (int) (Math.random() * 10000));
            caseNumber = "CASE-" + timestamp + "-" + random;
        }
    }

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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Counselor getCounselor() {
        return counselor;
    }

    public void setCounselor(Counselor counselor) {
        this.counselor = counselor;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public CasePriority getPriority() {
        return priority;
    }

    public void setPriority(CasePriority priority) {
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

    public Long getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
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

    public String getPresentingProblem() { return presentingProblem; }
    public void setPresentingProblem(String presentingProblem) { this.presentingProblem = presentingProblem; }

    public String getClinicalImpression() { return clinicalImpression; }
    public void setClinicalImpression(String clinicalImpression) { this.clinicalImpression = clinicalImpression; }

    public TreatmentModality getTreatmentModality() { return treatmentModality; }
    public void setTreatmentModality(TreatmentModality treatmentModality) { this.treatmentModality = treatmentModality; }

    public String getTreatmentGoals() { return treatmentGoals; }
    public void setTreatmentGoals(String treatmentGoals) { this.treatmentGoals = treatmentGoals; }

    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }

    public RiskAssessmentLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskAssessmentLevel riskLevel) { this.riskLevel = riskLevel; }

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

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        appointment.setCaseEntity(this);
    }

    public void removeAppointment(Appointment appointment) {
        appointments.remove(appointment);
        appointment.setCaseEntity(null);
    }
}
