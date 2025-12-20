package zm.unza.counseling.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mental_health_academic_analysis")
public class MentalHealthAcademicAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_performance_id")
    private AcademicPerformance academicPerformance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "self_assessment_id")
    private SelfAssessment selfAssessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_assessment_id")
    private RiskAssessment riskAssessment;

    @Column(name = "analysis_date", nullable = false)
    private LocalDate analysisDate;

    @Column(name = "analysis_period_start")
    private LocalDate analysisPeriodStart;

    @Column(name = "analysis_period_end")
    private LocalDate analysisPeriodEnd;

    // Mental Health Indicators
    @Column(name = "depression_score", precision = 5, scale = 2)
    private BigDecimal depressionScore;

    @Column(name = "anxiety_score", precision = 5, scale = 2)
    private BigDecimal anxietyScore;

    @Column(name = "stress_score", precision = 5, scale = 2)
    private BigDecimal stressScore;

    @Column(name = "overall_mental_health_score", precision = 5, scale = 2)
    private BigDecimal overallMentalHealthScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "mental_health_status")
    private MentalHealthStatus mentalHealthStatus;

    // Academic Performance Indicators at time of analysis
    @Column(name = "current_gpa", precision = 3, scale = 2)
    private BigDecimal currentGpa;

    @Column(name = "gpa_change", precision = 4, scale = 3)
    private BigDecimal gpaChange;

    @Column(name = "attendance_rate", precision = 5, scale = 2)
    private BigDecimal attendanceRate;

    @Column(name = "attendance_change", precision = 5, scale = 2)
    private BigDecimal attendanceChange;

    // Correlation Analysis
    @Column(name = "correlation_score", precision = 4, scale = 3)
    private BigDecimal correlationScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "correlation_strength")
    private CorrelationStrength correlationStrength;

    @Enumerated(EnumType.STRING)
    @Column(name = "impact_level")
    private ImpactLevel impactLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "trend_direction")
    private TrendDirection trendDirection;

    // Risk Factors Identified
    @Column(name = "concentration_issues")
    private Boolean concentrationIssues;

    @Column(name = "motivation_issues")
    private Boolean motivationIssues;

    @Column(name = "sleep_issues")
    private Boolean sleepIssues;

    @Column(name = "social_isolation")
    private Boolean socialIsolation;

    @Column(name = "financial_stress")
    private Boolean financialStress;

    @Column(name = "family_issues")
    private Boolean familyIssues;

    @Column(name = "substance_use_concern")
    private Boolean substanceUseConcern;

    // Intervention Recommendations
    @Column(name = "counseling_recommended")
    private Boolean counselingRecommended;

    @Column(name = "academic_support_recommended")
    private Boolean academicSupportRecommended;

    @Column(name = "peer_support_recommended")
    private Boolean peerSupportRecommended;

    @Column(name = "lifestyle_changes_recommended")
    private Boolean lifestyleChangesRecommended;

    @Column(name = "referral_recommended")
    private Boolean referralRecommended;

    @Enumerated(EnumType.STRING)
    @Column(name = "intervention_urgency")
    private InterventionUrgency interventionUrgency;

    // Analysis Notes
    @Column(name = "analysis_summary", columnDefinition = "TEXT")
    private String analysisSummary;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "counselor_notes", columnDefinition = "TEXT")
    private String counselorNotes;

    // Metadata
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analyzed_by")
    private Counselor analyzedBy;

    @Column(name = "is_ai_generated")
    private Boolean isAiGenerated;

    @Column(name = "ai_confidence_score", precision = 4, scale = 3)
    private BigDecimal aiConfidenceScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public MentalHealthAcademicAnalysis() {}

    // All args constructor
    public MentalHealthAcademicAnalysis(Long id, Client client, AcademicPerformance academicPerformance, 
                                       SelfAssessment selfAssessment, RiskAssessment riskAssessment,
                                       LocalDate analysisDate, LocalDate analysisPeriodStart, LocalDate analysisPeriodEnd,
                                       BigDecimal depressionScore, BigDecimal anxietyScore, BigDecimal stressScore,
                                       BigDecimal overallMentalHealthScore, MentalHealthStatus mentalHealthStatus,
                                       BigDecimal currentGpa, BigDecimal gpaChange, BigDecimal attendanceRate,
                                       BigDecimal attendanceChange, BigDecimal correlationScore,
                                       CorrelationStrength correlationStrength, ImpactLevel impactLevel,
                                       TrendDirection trendDirection, Boolean concentrationIssues,
                                       Boolean motivationIssues, Boolean sleepIssues, Boolean socialIsolation,
                                       Boolean financialStress, Boolean familyIssues, Boolean substanceUseConcern,
                                       Boolean counselingRecommended, Boolean academicSupportRecommended,
                                       Boolean peerSupportRecommended, Boolean lifestyleChangesRecommended,
                                       Boolean referralRecommended, InterventionUrgency interventionUrgency,
                                       String analysisSummary, String recommendations, String counselorNotes,
                                       Counselor analyzedBy, Boolean isAiGenerated, BigDecimal aiConfidenceScore,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.client = client;
        this.academicPerformance = academicPerformance;
        this.selfAssessment = selfAssessment;
        this.riskAssessment = riskAssessment;
        this.analysisDate = analysisDate;
        this.analysisPeriodStart = analysisPeriodStart;
        this.analysisPeriodEnd = analysisPeriodEnd;
        this.depressionScore = depressionScore;
        this.anxietyScore = anxietyScore;
        this.stressScore = stressScore;
        this.overallMentalHealthScore = overallMentalHealthScore;
        this.mentalHealthStatus = mentalHealthStatus;
        this.currentGpa = currentGpa;
        this.gpaChange = gpaChange;
        this.attendanceRate = attendanceRate;
        this.attendanceChange = attendanceChange;
        this.correlationScore = correlationScore;
        this.correlationStrength = correlationStrength;
        this.impactLevel = impactLevel;
        this.trendDirection = trendDirection;
        this.concentrationIssues = concentrationIssues;
        this.motivationIssues = motivationIssues;
        this.sleepIssues = sleepIssues;
        this.socialIsolation = socialIsolation;
        this.financialStress = financialStress;
        this.familyIssues = familyIssues;
        this.substanceUseConcern = substanceUseConcern;
        this.counselingRecommended = counselingRecommended;
        this.academicSupportRecommended = academicSupportRecommended;
        this.peerSupportRecommended = peerSupportRecommended;
        this.lifestyleChangesRecommended = lifestyleChangesRecommended;
        this.referralRecommended = referralRecommended;
        this.interventionUrgency = interventionUrgency;
        this.analysisSummary = analysisSummary;
        this.recommendations = recommendations;
        this.counselorNotes = counselorNotes;
        this.analyzedBy = analyzedBy;
        this.isAiGenerated = isAiGenerated;
        this.aiConfidenceScore = aiConfidenceScore;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public AcademicPerformance getAcademicPerformance() { return academicPerformance; }
    public void setAcademicPerformance(AcademicPerformance academicPerformance) { this.academicPerformance = academicPerformance; }

    public SelfAssessment getSelfAssessment() { return selfAssessment; }
    public void setSelfAssessment(SelfAssessment selfAssessment) { this.selfAssessment = selfAssessment; }

    public RiskAssessment getRiskAssessment() { return riskAssessment; }
    public void setRiskAssessment(RiskAssessment riskAssessment) { this.riskAssessment = riskAssessment; }

    public LocalDate getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(LocalDate analysisDate) { this.analysisDate = analysisDate; }

    public LocalDate getAnalysisPeriodStart() { return analysisPeriodStart; }
    public void setAnalysisPeriodStart(LocalDate analysisPeriodStart) { this.analysisPeriodStart = analysisPeriodStart; }

    public LocalDate getAnalysisPeriodEnd() { return analysisPeriodEnd; }
    public void setAnalysisPeriodEnd(LocalDate analysisPeriodEnd) { this.analysisPeriodEnd = analysisPeriodEnd; }

    public BigDecimal getDepressionScore() { return depressionScore; }
    public void setDepressionScore(BigDecimal depressionScore) { this.depressionScore = depressionScore; }

    public BigDecimal getAnxietyScore() { return anxietyScore; }
    public void setAnxietyScore(BigDecimal anxietyScore) { this.anxietyScore = anxietyScore; }

    public BigDecimal getStressScore() { return stressScore; }
    public void setStressScore(BigDecimal stressScore) { this.stressScore = stressScore; }

    public BigDecimal getOverallMentalHealthScore() { return overallMentalHealthScore; }
    public void setOverallMentalHealthScore(BigDecimal overallMentalHealthScore) { this.overallMentalHealthScore = overallMentalHealthScore; }

    public MentalHealthStatus getMentalHealthStatus() { return mentalHealthStatus; }
    public void setMentalHealthStatus(MentalHealthStatus mentalHealthStatus) { this.mentalHealthStatus = mentalHealthStatus; }

    public BigDecimal getCurrentGpa() { return currentGpa; }
    public void setCurrentGpa(BigDecimal currentGpa) { this.currentGpa = currentGpa; }

    public BigDecimal getGpaChange() { return gpaChange; }
    public void setGpaChange(BigDecimal gpaChange) { this.gpaChange = gpaChange; }

    public BigDecimal getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(BigDecimal attendanceRate) { this.attendanceRate = attendanceRate; }

    public BigDecimal getAttendanceChange() { return attendanceChange; }
    public void setAttendanceChange(BigDecimal attendanceChange) { this.attendanceChange = attendanceChange; }

    public BigDecimal getCorrelationScore() { return correlationScore; }
    public void setCorrelationScore(BigDecimal correlationScore) { this.correlationScore = correlationScore; }

    public CorrelationStrength getCorrelationStrength() { return correlationStrength; }
    public void setCorrelationStrength(CorrelationStrength correlationStrength) { this.correlationStrength = correlationStrength; }

    public ImpactLevel getImpactLevel() { return impactLevel; }
    public void setImpactLevel(ImpactLevel impactLevel) { this.impactLevel = impactLevel; }

    public TrendDirection getTrendDirection() { return trendDirection; }
    public void setTrendDirection(TrendDirection trendDirection) { this.trendDirection = trendDirection; }

    public Boolean getConcentrationIssues() { return concentrationIssues; }
    public void setConcentrationIssues(Boolean concentrationIssues) { this.concentrationIssues = concentrationIssues; }

    public Boolean getMotivationIssues() { return motivationIssues; }
    public void setMotivationIssues(Boolean motivationIssues) { this.motivationIssues = motivationIssues; }

    public Boolean getSleepIssues() { return sleepIssues; }
    public void setSleepIssues(Boolean sleepIssues) { this.sleepIssues = sleepIssues; }

    public Boolean getSocialIsolation() { return socialIsolation; }
    public void setSocialIsolation(Boolean socialIsolation) { this.socialIsolation = socialIsolation; }

    public Boolean getFinancialStress() { return financialStress; }
    public void setFinancialStress(Boolean financialStress) { this.financialStress = financialStress; }

    public Boolean getFamilyIssues() { return familyIssues; }
    public void setFamilyIssues(Boolean familyIssues) { this.familyIssues = familyIssues; }

    public Boolean getSubstanceUseConcern() { return substanceUseConcern; }
    public void setSubstanceUseConcern(Boolean substanceUseConcern) { this.substanceUseConcern = substanceUseConcern; }

    public Boolean getCounselingRecommended() { return counselingRecommended; }
    public void setCounselingRecommended(Boolean counselingRecommended) { this.counselingRecommended = counselingRecommended; }

    public Boolean getAcademicSupportRecommended() { return academicSupportRecommended; }
    public void setAcademicSupportRecommended(Boolean academicSupportRecommended) { this.academicSupportRecommended = academicSupportRecommended; }

    public Boolean getPeerSupportRecommended() { return peerSupportRecommended; }
    public void setPeerSupportRecommended(Boolean peerSupportRecommended) { this.peerSupportRecommended = peerSupportRecommended; }

    public Boolean getLifestyleChangesRecommended() { return lifestyleChangesRecommended; }
    public void setLifestyleChangesRecommended(Boolean lifestyleChangesRecommended) { this.lifestyleChangesRecommended = lifestyleChangesRecommended; }

    public Boolean getReferralRecommended() { return referralRecommended; }
    public void setReferralRecommended(Boolean referralRecommended) { this.referralRecommended = referralRecommended; }

    public InterventionUrgency getInterventionUrgency() { return interventionUrgency; }
    public void setInterventionUrgency(InterventionUrgency interventionUrgency) { this.interventionUrgency = interventionUrgency; }

    public String getAnalysisSummary() { return analysisSummary; }
    public void setAnalysisSummary(String analysisSummary) { this.analysisSummary = analysisSummary; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public String getCounselorNotes() { return counselorNotes; }
    public void setCounselorNotes(String counselorNotes) { this.counselorNotes = counselorNotes; }

    public Counselor getAnalyzedBy() { return analyzedBy; }
    public void setAnalyzedBy(Counselor analyzedBy) { this.analyzedBy = analyzedBy; }

    public Boolean getIsAiGenerated() { return isAiGenerated; }
    public void setIsAiGenerated(Boolean isAiGenerated) { this.isAiGenerated = isAiGenerated; }

    public BigDecimal getAiConfidenceScore() { return aiConfidenceScore; }
    public void setAiConfidenceScore(BigDecimal aiConfidenceScore) { this.aiConfidenceScore = aiConfidenceScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder pattern methods
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MentalHealthAcademicAnalysis analysis = new MentalHealthAcademicAnalysis();

        public Builder id(Long id) {
            analysis.setId(id);
            return this;
        }

        public Builder client(Client client) {
            analysis.setClient(client);
            return this;
        }

        public Builder academicPerformance(AcademicPerformance academicPerformance) {
            analysis.setAcademicPerformance(academicPerformance);
            return this;
        }

        public Builder selfAssessment(SelfAssessment selfAssessment) {
            analysis.setSelfAssessment(selfAssessment);
            return this;
        }

        public Builder riskAssessment(RiskAssessment riskAssessment) {
            analysis.setRiskAssessment(riskAssessment);
            return this;
        }

        public Builder analysisDate(LocalDate analysisDate) {
            analysis.setAnalysisDate(analysisDate);
            return this;
        }

        public Builder analysisPeriodStart(LocalDate analysisPeriodStart) {
            analysis.setAnalysisPeriodStart(analysisPeriodStart);
            return this;
        }

        public Builder analysisPeriodEnd(LocalDate analysisPeriodEnd) {
            analysis.setAnalysisPeriodEnd(analysisPeriodEnd);
            return this;
        }

        public Builder depressionScore(BigDecimal depressionScore) {
            analysis.setDepressionScore(depressionScore);
            return this;
        }

        public Builder anxietyScore(BigDecimal anxietyScore) {
            analysis.setAnxietyScore(anxietyScore);
            return this;
        }

        public Builder stressScore(BigDecimal stressScore) {
            analysis.setStressScore(stressScore);
            return this;
        }

        public Builder overallMentalHealthScore(BigDecimal overallMentalHealthScore) {
            analysis.setOverallMentalHealthScore(overallMentalHealthScore);
            return this;
        }

        public Builder mentalHealthStatus(MentalHealthStatus mentalHealthStatus) {
            analysis.setMentalHealthStatus(mentalHealthStatus);
            return this;
        }

        public Builder currentGpa(BigDecimal currentGpa) {
            analysis.setCurrentGpa(currentGpa);
            return this;
        }

        public Builder gpaChange(BigDecimal gpaChange) {
            analysis.setGpaChange(gpaChange);
            return this;
        }

        public Builder attendanceRate(BigDecimal attendanceRate) {
            analysis.setAttendanceRate(attendanceRate);
            return this;
        }

        public Builder attendanceChange(BigDecimal attendanceChange) {
            analysis.setAttendanceChange(attendanceChange);
            return this;
        }

        public Builder correlationScore(BigDecimal correlationScore) {
            analysis.setCorrelationScore(correlationScore);
            return this;
        }

        public Builder correlationStrength(CorrelationStrength correlationStrength) {
            analysis.setCorrelationStrength(correlationStrength);
            return this;
        }

        public Builder impactLevel(ImpactLevel impactLevel) {
            analysis.setImpactLevel(impactLevel);
            return this;
        }

        public Builder trendDirection(TrendDirection trendDirection) {
            analysis.setTrendDirection(trendDirection);
            return this;
        }

        public Builder concentrationIssues(Boolean concentrationIssues) {
            analysis.setConcentrationIssues(concentrationIssues);
            return this;
        }

        public Builder motivationIssues(Boolean motivationIssues) {
            analysis.setMotivationIssues(motivationIssues);
            return this;
        }

        public Builder sleepIssues(Boolean sleepIssues) {
            analysis.setSleepIssues(sleepIssues);
            return this;
        }

        public Builder socialIsolation(Boolean socialIsolation) {
            analysis.setSocialIsolation(socialIsolation);
            return this;
        }

        public Builder financialStress(Boolean financialStress) {
            analysis.setFinancialStress(financialStress);
            return this;
        }

        public Builder familyIssues(Boolean familyIssues) {
            analysis.setFamilyIssues(familyIssues);
            return this;
        }

        public Builder substanceUseConcern(Boolean substanceUseConcern) {
            analysis.setSubstanceUseConcern(substanceUseConcern);
            return this;
        }

        public Builder counselingRecommended(Boolean counselingRecommended) {
            analysis.setCounselingRecommended(counselingRecommended);
            return this;
        }

        public Builder academicSupportRecommended(Boolean academicSupportRecommended) {
            analysis.setAcademicSupportRecommended(academicSupportRecommended);
            return this;
        }

        public Builder peerSupportRecommended(Boolean peerSupportRecommended) {
            analysis.setPeerSupportRecommended(peerSupportRecommended);
            return this;
        }

        public Builder lifestyleChangesRecommended(Boolean lifestyleChangesRecommended) {
            analysis.setLifestyleChangesRecommended(lifestyleChangesRecommended);
            return this;
        }

        public Builder referralRecommended(Boolean referralRecommended) {
            analysis.setReferralRecommended(referralRecommended);
            return this;
        }

        public Builder interventionUrgency(InterventionUrgency interventionUrgency) {
            analysis.setInterventionUrgency(interventionUrgency);
            return this;
        }

        public Builder analysisSummary(String analysisSummary) {
            analysis.setAnalysisSummary(analysisSummary);
            return this;
        }

        public Builder recommendations(String recommendations) {
            analysis.setRecommendations(recommendations);
            return this;
        }

        public Builder counselorNotes(String counselorNotes) {
            analysis.setCounselorNotes(counselorNotes);
            return this;
        }

        public Builder analyzedBy(Counselor analyzedBy) {
            analysis.setAnalyzedBy(analyzedBy);
            return this;
        }

        public Builder isAiGenerated(Boolean isAiGenerated) {
            analysis.setIsAiGenerated(isAiGenerated);
            return this;
        }

        public Builder aiConfidenceScore(BigDecimal aiConfidenceScore) {
            analysis.setAiConfidenceScore(aiConfidenceScore);
            return this;
        }

        public MentalHealthAcademicAnalysis build() {
            return analysis;
        }
    }

    // Enums
    public enum MentalHealthStatus {
        EXCELLENT,
        GOOD,
        MODERATE,
        AT_RISK,
        CRISIS,
        NOT_ASSESSED
    }

    public enum CorrelationStrength {
        STRONG_POSITIVE,    // > 0.7
        MODERATE_POSITIVE,  // 0.4 - 0.7
        WEAK_POSITIVE,      // 0.2 - 0.4
        NEGLIGIBLE,         // -0.2 - 0.2
        WEAK_NEGATIVE,      // -0.4 - -0.2
        MODERATE_NEGATIVE,  // -0.7 - -0.4
        STRONG_NEGATIVE     // < -0.7
    }

    public enum ImpactLevel {
        NONE,
        MINIMAL,
        MODERATE,
        SIGNIFICANT,
        SEVERE
    }

    public enum TrendDirection {
        IMPROVING,
        STABLE,
        DECLINING,
        FLUCTUATING,
        UNKNOWN
    }

    public enum InterventionUrgency {
        NONE,
        LOW,
        MODERATE,
        HIGH,
        IMMEDIATE
    }

    // Helper method to determine if intervention is needed
    public boolean isInterventionNeeded() {
        return interventionUrgency != null && 
               interventionUrgency != InterventionUrgency.NONE && 
               interventionUrgency != InterventionUrgency.LOW;
    }

    // Helper method to calculate risk level based on multiple factors
    public int calculateOverallRiskScore() {
        int score = 0;
        
        if (Boolean.TRUE.equals(concentrationIssues)) score += 10;
        if (Boolean.TRUE.equals(motivationIssues)) score += 10;
        if (Boolean.TRUE.equals(sleepIssues)) score += 15;
        if (Boolean.TRUE.equals(socialIsolation)) score += 15;
        if (Boolean.TRUE.equals(financialStress)) score += 10;
        if (Boolean.TRUE.equals(familyIssues)) score += 10;
        if (Boolean.TRUE.equals(substanceUseConcern)) score += 20;
        
        // Add impact from mental health scores
        if (depressionScore != null && depressionScore.doubleValue() > 14) score += 15;
        if (anxietyScore != null && anxietyScore.doubleValue() > 14) score += 10;
        if (stressScore != null && stressScore.doubleValue() > 14) score += 10;
        
        return Math.min(score, 100);
    }
}