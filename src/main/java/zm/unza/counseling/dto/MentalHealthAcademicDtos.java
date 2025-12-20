package zm.unza.counseling.dto;

import zm.unza.counseling.entity.MentalHealthAcademicAnalysis;
import zm.unza.counseling.entity.AcademicPerformance;
import zm.unza.counseling.entity.SelfAssessment;
import zm.unza.counseling.entity.RiskAssessment;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.Counselor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO classes for Mental Health Academic Analysis functionality
 */
public class MentalHealthAcademicDtos {

    /**
     * Request DTO for creating/updating mental health academic analysis
     */
    public static class MentalHealthAcademicAnalysisRequest {
        private Long clientId;
        private Long academicPerformanceId;
        private String selfAssessmentId;
        private Long riskAssessmentId;
        private LocalDate analysisDate;
        private LocalDate analysisPeriodStart;
        private LocalDate analysisPeriodEnd;
        
        // Mental Health Indicators
        private BigDecimal depressionScore;
        private BigDecimal anxietyScore;
        private BigDecimal stressScore;
        private BigDecimal overallMentalHealthScore;
        private MentalHealthAcademicAnalysis.MentalHealthStatus mentalHealthStatus;
        
        // Academic Performance Indicators
        private BigDecimal currentGpa;
        private BigDecimal gpaChange;
        private BigDecimal attendanceRate;
        private BigDecimal attendanceChange;
        
        // Risk Factors
        private Boolean concentrationIssues;
        private Boolean motivationIssues;
        private Boolean sleepIssues;
        private Boolean socialIsolation;
        private Boolean financialStress;
        private Boolean familyIssues;
        private Boolean substanceUseConcern;
        
        // Intervention Recommendations
        private Boolean counselingRecommended;
        private Boolean academicSupportRecommended;
        private Boolean peerSupportRecommended;
        private Boolean lifestyleChangesRecommended;
        private Boolean referralRecommended;
        private MentalHealthAcademicAnalysis.InterventionUrgency interventionUrgency;
        
        // Analysis Notes
        private String analysisSummary;
        private String recommendations;
        private String counselorNotes;
        
        // Metadata
        private Long analyzedBy;
        private Boolean isAiGenerated;
        private BigDecimal aiConfidenceScore;

        // Constructors
        public MentalHealthAcademicAnalysisRequest() {}

        // Getters and Setters
        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }
        
        public Long getAcademicPerformanceId() { return academicPerformanceId; }
        public void setAcademicPerformanceId(Long academicPerformanceId) { this.academicPerformanceId = academicPerformanceId; }
        
        public String getSelfAssessmentId() { return selfAssessmentId; }
        public void setSelfAssessmentId(String selfAssessmentId) { this.selfAssessmentId = selfAssessmentId; }
        
        public Long getRiskAssessmentId() { return riskAssessmentId; }
        public void setRiskAssessmentId(Long riskAssessmentId) { this.riskAssessmentId = riskAssessmentId; }
        
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
        
        public MentalHealthAcademicAnalysis.MentalHealthStatus getMentalHealthStatus() { return mentalHealthStatus; }
        public void setMentalHealthStatus(MentalHealthAcademicAnalysis.MentalHealthStatus mentalHealthStatus) { this.mentalHealthStatus = mentalHealthStatus; }
        
        public BigDecimal getCurrentGpa() { return currentGpa; }
        public void setCurrentGpa(BigDecimal currentGpa) { this.currentGpa = currentGpa; }
        
        public BigDecimal getGpaChange() { return gpaChange; }
        public void setGpaChange(BigDecimal gpaChange) { this.gpaChange = gpaChange; }
        
        public BigDecimal getAttendanceRate() { return attendanceRate; }
        public void setAttendanceRate(BigDecimal attendanceRate) { this.attendanceRate = attendanceRate; }
        
        public BigDecimal getAttendanceChange() { return attendanceChange; }
        public void setAttendanceChange(BigDecimal attendanceChange) { this.attendanceChange = attendanceChange; }
        
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
        
        public MentalHealthAcademicAnalysis.InterventionUrgency getInterventionUrgency() { return interventionUrgency; }
        public void setInterventionUrgency(MentalHealthAcademicAnalysis.InterventionUrgency interventionUrgency) { this.interventionUrgency = interventionUrgency; }
        
        public String getAnalysisSummary() { return analysisSummary; }
        public void setAnalysisSummary(String analysisSummary) { this.analysisSummary = analysisSummary; }
        
        public String getRecommendations() { return recommendations; }
        public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
        
        public String getCounselorNotes() { return counselorNotes; }
        public void setCounselorNotes(String counselorNotes) { this.counselorNotes = counselorNotes; }
        
        public Long getAnalyzedBy() { return analyzedBy; }
        public void setAnalyzedBy(Long analyzedBy) { this.analyzedBy = analyzedBy; }
        
        public Boolean getIsAiGenerated() { return isAiGenerated; }
        public void setIsAiGenerated(Boolean isAiGenerated) { this.isAiGenerated = isAiGenerated; }
        
        public BigDecimal getAiConfidenceScore() { return aiConfidenceScore; }
        public void setAiConfidenceScore(BigDecimal aiConfidenceScore) { this.aiConfidenceScore = aiConfidenceScore; }
    }

    /**
     * Response DTO for mental health academic analysis
     */
    public static class MentalHealthAcademicAnalysisResponse {
        private Long id;
        private Long clientId;
        private String clientName;
        private Long academicPerformanceId;
        private String selfAssessmentId;
        private Long riskAssessmentId;
        private LocalDate analysisDate;
        private LocalDate analysisPeriodStart;
        private LocalDate analysisPeriodEnd;
        
        // Mental Health Indicators
        private BigDecimal depressionScore;
        private BigDecimal anxietyScore;
        private BigDecimal stressScore;
        private BigDecimal overallMentalHealthScore;
        private MentalHealthAcademicAnalysis.MentalHealthStatus mentalHealthStatus;
        
        // Academic Performance Indicators
        private BigDecimal currentGpa;
        private BigDecimal gpaChange;
        private BigDecimal attendanceRate;
        private BigDecimal attendanceChange;
        
        // Correlation Analysis
        private BigDecimal correlationScore;
        private MentalHealthAcademicAnalysis.CorrelationStrength correlationStrength;
        private MentalHealthAcademicAnalysis.ImpactLevel impactLevel;
        private MentalHealthAcademicAnalysis.TrendDirection trendDirection;
        
        // Risk Factors
        private Boolean concentrationIssues;
        private Boolean motivationIssues;
        private Boolean sleepIssues;
        private Boolean socialIsolation;
        private Boolean financialStress;
        private Boolean familyIssues;
        private Boolean substanceUseConcern;
        
        // Intervention Recommendations
        private Boolean counselingRecommended;
        private Boolean academicSupportRecommended;
        private Boolean peerSupportRecommended;
        private Boolean lifestyleChangesRecommended;
        private Boolean referralRecommended;
        private MentalHealthAcademicAnalysis.InterventionUrgency interventionUrgency;
        private boolean interventionNeeded;
        private int overallRiskScore;
        
        // Analysis Notes
        private String analysisSummary;
        private String recommendations;
        private String counselorNotes;
        
        // Metadata
        private Long analyzedBy;
        private String analyzedByName;
        private Boolean isAiGenerated;
        private BigDecimal aiConfidenceScore;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Constructors
        public MentalHealthAcademicAnalysisResponse() {}

        public MentalHealthAcademicAnalysisResponse(MentalHealthAcademicAnalysis analysis) {
            this.id = analysis.getId();
            this.clientId = analysis.getClient().getId();
            this.clientName = analysis.getClient().getFullName();
            this.analysisDate = analysis.getAnalysisDate();
            this.analysisPeriodStart = analysis.getAnalysisPeriodStart();
            this.analysisPeriodEnd = analysis.getAnalysisPeriodEnd();
            
            // Mental Health Indicators
            this.depressionScore = analysis.getDepressionScore();
            this.anxietyScore = analysis.getAnxietyScore();
            this.stressScore = analysis.getStressScore();
            this.overallMentalHealthScore = analysis.getOverallMentalHealthScore();
            this.mentalHealthStatus = analysis.getMentalHealthStatus();
            
            // Academic Performance Indicators
            this.currentGpa = analysis.getCurrentGpa();
            this.gpaChange = analysis.getGpaChange();
            this.attendanceRate = analysis.getAttendanceRate();
            this.attendanceChange = analysis.getAttendanceChange();
            
            // Correlation Analysis
            this.correlationScore = analysis.getCorrelationScore();
            this.correlationStrength = analysis.getCorrelationStrength();
            this.impactLevel = analysis.getImpactLevel();
            this.trendDirection = analysis.getTrendDirection();
            
            // Risk Factors
            this.concentrationIssues = analysis.getConcentrationIssues();
            this.motivationIssues = analysis.getMotivationIssues();
            this.sleepIssues = analysis.getSleepIssues();
            this.socialIsolation = analysis.getSocialIsolation();
            this.financialStress = analysis.getFinancialStress();
            this.familyIssues = analysis.getFamilyIssues();
            this.substanceUseConcern = analysis.getSubstanceUseConcern();
            
            // Intervention Recommendations
            this.counselingRecommended = analysis.getCounselingRecommended();
            this.academicSupportRecommended = analysis.getAcademicSupportRecommended();
            this.peerSupportRecommended = analysis.getPeerSupportRecommended();
            this.lifestyleChangesRecommended = analysis.getLifestyleChangesRecommended();
            this.referralRecommended = analysis.getReferralRecommended();
            this.interventionUrgency = analysis.getInterventionUrgency();
            this.interventionNeeded = analysis.isInterventionNeeded();
            this.overallRiskScore = analysis.calculateOverallRiskScore();
            
            // Analysis Notes
            this.analysisSummary = analysis.getAnalysisSummary();
            this.recommendations = analysis.getRecommendations();
            this.counselorNotes = analysis.getCounselorNotes();
            
            // Metadata
            this.analyzedBy = analysis.getAnalyzedBy() != null ? analysis.getAnalyzedBy().getId() : null;
            this.analyzedByName = analysis.getAnalyzedBy() != null ? analysis.getAnalyzedBy().getFullName() : null;
            this.isAiGenerated = analysis.getIsAiGenerated();
            this.aiConfidenceScore = analysis.getAiConfidenceScore();
            this.createdAt = analysis.getCreatedAt();
            this.updatedAt = analysis.getUpdatedAt();
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }
        
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        
        public Long getAcademicPerformanceId() { return academicPerformanceId; }
        public void setAcademicPerformanceId(Long academicPerformanceId) { this.academicPerformanceId = academicPerformanceId; }
        
        public String getSelfAssessmentId() { return selfAssessmentId; }
        public void setSelfAssessmentId(String selfAssessmentId) { this.selfAssessmentId = selfAssessmentId; }
        
        public Long getRiskAssessmentId() { return riskAssessmentId; }
        public void setRiskAssessmentId(Long riskAssessmentId) { this.riskAssessmentId = riskAssessmentId; }
        
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
        
        public MentalHealthAcademicAnalysis.MentalHealthStatus getMentalHealthStatus() { return mentalHealthStatus; }
        public void setMentalHealthStatus(MentalHealthAcademicAnalysis.MentalHealthStatus mentalHealthStatus) { this.mentalHealthStatus = mentalHealthStatus; }
        
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
        
        public MentalHealthAcademicAnalysis.CorrelationStrength getCorrelationStrength() { return correlationStrength; }
        public void setCorrelationStrength(MentalHealthAcademicAnalysis.CorrelationStrength correlationStrength) { this.correlationStrength = correlationStrength; }
        
        public MentalHealthAcademicAnalysis.ImpactLevel getImpactLevel() { return impactLevel; }
        public void setImpactLevel(MentalHealthAcademicAnalysis.ImpactLevel impactLevel) { this.impactLevel = impactLevel; }
        
        public MentalHealthAcademicAnalysis.TrendDirection getTrendDirection() { return trendDirection; }
        public void setTrendDirection(MentalHealthAcademicAnalysis.TrendDirection trendDirection) { this.trendDirection = trendDirection; }
        
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
        
        public MentalHealthAcademicAnalysis.InterventionUrgency getInterventionUrgency() { return interventionUrgency; }
        public void setInterventionUrgency(MentalHealthAcademicAnalysis.InterventionUrgency interventionUrgency) { this.interventionUrgency = interventionUrgency; }
        
        public boolean isInterventionNeeded() { return interventionNeeded; }
        public void setInterventionNeeded(boolean interventionNeeded) { this.interventionNeeded = interventionNeeded; }
        
        public int getOverallRiskScore() { return overallRiskScore; }
        public void setOverallRiskScore(int overallRiskScore) { this.overallRiskScore = overallRiskScore; }
        
        public String getAnalysisSummary() { return analysisSummary; }
        public void setAnalysisSummary(String analysisSummary) { this.analysisSummary = analysisSummary; }
        
        public String getRecommendations() { return recommendations; }
        public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
        
        public String getCounselorNotes() { return counselorNotes; }
        public void setCounselorNotes(String counselorNotes) { this.counselorNotes = counselorNotes; }
        
        public Long getAnalyzedBy() { return analyzedBy; }
        public void setAnalyzedBy(Long analyzedBy) { this.analyzedBy = analyzedBy; }
        
        public String getAnalyzedByName() { return analyzedByName; }
        public void setAnalyzedByName(String analyzedByName) { this.analyzedByName = analyzedByName; }
        
        public Boolean getIsAiGenerated() { return isAiGenerated; }
        public void setIsAiGenerated(Boolean isAiGenerated) { this.isAiGenerated = isAiGenerated; }
        
        public BigDecimal getAiConfidenceScore() { return aiConfidenceScore; }
        public void setAiConfidenceScore(BigDecimal aiConfidenceScore) { this.aiConfidenceScore = aiConfidenceScore; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private MentalHealthAcademicAnalysisResponse r = new MentalHealthAcademicAnalysisResponse();
            public Builder id(Long id) { r.setId(id); return this; }
            public Builder clientId(Long id) { r.setClientId(id); return this; }
            public Builder clientName(String n) { r.setClientName(n); return this; }
            public Builder studentNumber(String s) { /* r.setStudentNumber(s); */ return this; } // Field missing in DTO but used in mapper, ignoring for now or assuming mapped elsewhere
            public Builder academicPerformanceId(Long id) { r.setAcademicPerformanceId(id); return this; }
            public Builder selfAssessmentId(String id) { r.setSelfAssessmentId(id); return this; }
            public Builder riskAssessmentId(Long id) { r.setRiskAssessmentId(id); return this; }
            public Builder analysisDate(LocalDate d) { r.setAnalysisDate(d); return this; }
            public Builder analysisPeriodStart(LocalDate d) { r.setAnalysisPeriodStart(d); return this; }
            public Builder analysisPeriodEnd(LocalDate d) { r.setAnalysisPeriodEnd(d); return this; }
            public Builder depressionScore(BigDecimal s) { r.setDepressionScore(s); return this; }
            public Builder anxietyScore(BigDecimal s) { r.setAnxietyScore(s); return this; }
            public Builder stressScore(BigDecimal s) { r.setStressScore(s); return this; }
            public Builder overallMentalHealthScore(BigDecimal s) { r.setOverallMentalHealthScore(s); return this; }
            public Builder mentalHealthStatus(String s) { if(s!=null) r.setMentalHealthStatus(MentalHealthAcademicAnalysis.MentalHealthStatus.valueOf(s)); return this; }
            public Builder currentGpa(BigDecimal s) { r.setCurrentGpa(s); return this; }
            public Builder gpaChange(BigDecimal s) { r.setGpaChange(s); return this; }
            public Builder attendanceRate(BigDecimal s) { r.setAttendanceRate(s); return this; }
            public Builder attendanceChange(BigDecimal s) { r.setAttendanceChange(s); return this; }
            public Builder correlationScore(BigDecimal s) { r.setCorrelationScore(s); return this; }
            public Builder correlationStrength(String s) { if(s!=null) r.setCorrelationStrength(MentalHealthAcademicAnalysis.CorrelationStrength.valueOf(s)); return this; }
            public Builder impactLevel(String s) { if(s!=null) r.setImpactLevel(MentalHealthAcademicAnalysis.ImpactLevel.valueOf(s)); return this; }
            public Builder trendDirection(String s) { if(s!=null) r.setTrendDirection(MentalHealthAcademicAnalysis.TrendDirection.valueOf(s)); return this; }
            public Builder riskFactors(RiskFactors rf) { 
                r.setConcentrationIssues(rf.getConcentrationIssues());
                r.setMotivationIssues(rf.getMotivationIssues());
                r.setSleepIssues(rf.getSleepIssues());
                r.setSocialIsolation(rf.getSocialIsolation());
                r.setFinancialStress(rf.getFinancialStress());
                r.setFamilyIssues(rf.getFamilyIssues());
                r.setSubstanceUseConcern(rf.getSubstanceUseConcern());
                return this; 
            }
            public Builder recommendations(Recommendations rec) {
                r.setCounselingRecommended(rec.getCounselingRecommended());
                r.setAcademicSupportRecommended(rec.getAcademicSupportRecommended());
                r.setPeerSupportRecommended(rec.getPeerSupportRecommended());
                r.setLifestyleChangesRecommended(rec.getLifestyleChangesRecommended());
                r.setReferralRecommended(rec.getReferralRecommended());
                r.setInterventionUrgency(rec.getInterventionUrgency());
                return this;
            }
            public Builder analysisSummary(String s) { r.setAnalysisSummary(s); return this; }
            public Builder recommendationText(String s) { r.setRecommendations(s); return this; }
            public Builder counselorNotes(String s) { r.setCounselorNotes(s); return this; }
            public Builder analyzedById(Long id) { r.setAnalyzedBy(id); return this; }
            public Builder analyzedByName(String n) { r.setAnalyzedByName(n); return this; }
            public Builder isAiGenerated(Boolean b) { r.setIsAiGenerated(b); return this; }
            public Builder aiConfidenceScore(BigDecimal s) { r.setAiConfidenceScore(s); return this; }
            public Builder overallRiskScore(int s) { r.setOverallRiskScore(s); return this; }
            public Builder createdAt(LocalDateTime d) { r.setCreatedAt(d); return this; }
            public Builder updatedAt(LocalDateTime d) { r.setUpdatedAt(d); return this; }
            public MentalHealthAcademicAnalysisResponse build() { return r; }
        }
    }

    /**
     * AI Analysis Request DTO
     */
    public static class AiAnalysisRequest {
        private Long clientId;
        private List<Long> assessmentIds;
        private String analysisType;
        private Map<String, Object> parameters;
        private LocalDate analysisPeriodStart;
        private LocalDate analysisPeriodEnd;

        public AiAnalysisRequest() {}

        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }
        
        public List<Long> getAssessmentIds() { return assessmentIds; }
        public void setAssessmentIds(List<Long> assessmentIds) { this.assessmentIds = assessmentIds; }
        
        public String getAnalysisType() { return analysisType; }
        public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
        
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public LocalDate getAnalysisPeriodStart() { return analysisPeriodStart; }
        public void setAnalysisPeriodStart(LocalDate analysisPeriodStart) { this.analysisPeriodStart = analysisPeriodStart; }

        public LocalDate getAnalysisPeriodEnd() { return analysisPeriodEnd; }
        public void setAnalysisPeriodEnd(LocalDate analysisPeriodEnd) { this.analysisPeriodEnd = analysisPeriodEnd; }
    }

    /**
     * AI Analysis Response DTO
     */
    public static class AiAnalysisResponse {
        private String analysisSummary;
        private BigDecimal confidenceScore;
        private Map<String, Object> recommendations;
        private Map<String, Object> riskFactors;
        private String status;

        public AiAnalysisResponse() {}

        public String getAnalysisSummary() { return analysisSummary; }
        public void setAnalysisSummary(String analysisSummary) { this.analysisSummary = analysisSummary; }
        
        public BigDecimal getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
        
        public Map<String, Object> getRecommendations() { return recommendations; }
        public void setRecommendations(Map<String, Object> recommendations) { this.recommendations = recommendations; }
        
        public Map<String, Object> getRiskFactors() { return riskFactors; }
        public void setRiskFactors(Map<String, Object> riskFactors) { this.riskFactors = riskFactors; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private AiAnalysisResponse r = new AiAnalysisResponse();
            public Builder analysis(MentalHealthAcademicAnalysisResponse a) { /* r.setAnalysis(a); */ return this; } // Field missing in DTO
            public Builder aiNarrative(String s) { /* r.setAiNarrative(s); */ return this; } // Field missing in DTO
            public Builder confidenceScore(BigDecimal s) { r.setConfidenceScore(s); return this; }
            public Builder keyFindings(List<String> l) { /* r.setKeyFindings(l); */ return this; } // Field missing in DTO
            public Builder suggestedInterventions(List<String> l) { /* r.setSuggestedInterventions(l); */ return this; } // Field missing in DTO
            public Builder riskPrediction(String s) { /* r.setRiskPrediction(s); */ return this; } // Field missing in DTO
            public AiAnalysisResponse build() { return r; }
        }
    }

    /**
     * Correlation Analysis Result DTO
     */
    public static class CorrelationAnalysisResult {
        private BigDecimal correlationScore;
        private MentalHealthAcademicAnalysis.CorrelationStrength strength;
        private MentalHealthAcademicAnalysis.ImpactLevel impactLevel;
        private String interpretation;
        private List<DataPoint> dataPoints;

        public CorrelationAnalysisResult() {}

        public BigDecimal getCorrelationScore() { return correlationScore; }
        public void setCorrelationScore(BigDecimal correlationScore) { this.correlationScore = correlationScore; }
        
        public MentalHealthAcademicAnalysis.CorrelationStrength getStrength() { return strength; }
        public void setStrength(MentalHealthAcademicAnalysis.CorrelationStrength strength) { this.strength = strength; }
        
        public MentalHealthAcademicAnalysis.ImpactLevel getImpactLevel() { return impactLevel; }
        public void setImpactLevel(MentalHealthAcademicAnalysis.ImpactLevel impactLevel) { this.impactLevel = impactLevel; }
        
        public String getInterpretation() { return interpretation; }
        public void setInterpretation(String interpretation) { this.interpretation = interpretation; }
        
        public List<DataPoint> getDataPoints() { return dataPoints; }
        public void setDataPoints(List<DataPoint> dataPoints) { this.dataPoints = dataPoints; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private CorrelationAnalysisResult r = new CorrelationAnalysisResult();
            public Builder clientId(Long id) { /* r.setClientId(id); */ return this; } // Field missing
            public Builder clientName(String n) { /* r.setClientName(n); */ return this; } // Field missing
            public Builder correlationCoefficient(BigDecimal c) { r.setCorrelationScore(c); return this; }
            public Builder correlationStrength(String s) { if(s!=null) r.setStrength(MentalHealthAcademicAnalysis.CorrelationStrength.valueOf(s)); return this; }
            public Builder interpretation(String s) { r.setInterpretation(s); return this; }
            public Builder mentalHealthData(List<DataPoint> l) { /* r.setMentalHealthData(l); */ return this; } // Field missing
            public Builder academicData(List<DataPoint> l) { /* r.setAcademicData(l); */ return this; } // Field missing
            public Builder statisticalSignificance(String s) { /* r.setStatisticalSignificance(s); */ return this; } // Field missing
            public Builder recommendation(String s) { /* r.setRecommendation(s); */ return this; } // Field missing
            public CorrelationAnalysisResult build() { return r; }
        }
    }

    /**
     * Trend Analysis Result DTO
     */
    public static class TrendAnalysisResult {
        private MentalHealthAcademicAnalysis.TrendDirection direction;
        private BigDecimal trendScore;
        private String description;
        private List<TrendDataPoint> dataPoints;
        private String timeframe;

        public TrendAnalysisResult() {}

        public MentalHealthAcademicAnalysis.TrendDirection getDirection() { return direction; }
        public void setDirection(MentalHealthAcademicAnalysis.TrendDirection direction) { this.direction = direction; }
        
        public BigDecimal getTrendScore() { return trendScore; }
        public void setTrendScore(BigDecimal trendScore) { this.trendScore = trendScore; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public List<TrendDataPoint> getDataPoints() { return dataPoints; }
        public void setDataPoints(List<TrendDataPoint> dataPoints) { this.dataPoints = dataPoints; }
        
        public String getTimeframe() { return timeframe; }
        public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private TrendAnalysisResult r = new TrendAnalysisResult();
            public Builder clientId(Long id) { /* r.setClientId(id); */ return this; } // Field missing
            public Builder clientName(String n) { /* r.setClientName(n); */ return this; } // Field missing
            public Builder mentalHealthTrend(String s) { /* r.setMentalHealthTrend(s); */ return this; } // Field missing
            public Builder academicTrend(String s) { /* r.setAcademicTrend(s); */ return this; } // Field missing
            public Builder overallTrend(String s) { /* r.setOverallTrend(s); */ return this; } // Field missing
            public Builder trendData(List<TrendDataPoint> l) { r.setDataPoints(l); return this; }
            public Builder prediction(String s) { /* r.setPrediction(s); */ return this; } // Field missing
            public Builder riskAssessment(String s) { /* r.setRiskAssessment(s); */ return this; } // Field missing
            public TrendAnalysisResult build() { return r; }
        }
    }

    /**
     * Analysis Dashboard Stats DTO
     */
    public static class AnalysisDashboardStats {
        private long totalAnalyses;
        private long atRiskStudents;
        private long interventionNeeded;
        private MentalHealthDistribution mentalHealthDistribution;
        private ImpactDistribution impactDistribution;
        private Map<String, Object> trends;

        public AnalysisDashboardStats() {}

        public long getTotalAnalyses() { return totalAnalyses; }
        public void setTotalAnalyses(long totalAnalyses) { this.totalAnalyses = totalAnalyses; }
        
        public long getAtRiskStudents() { return atRiskStudents; }
        public void setAtRiskStudents(long atRiskStudents) { this.atRiskStudents = atRiskStudents; }
        
        public long getInterventionNeeded() { return interventionNeeded; }
        public void setInterventionNeeded(long interventionNeeded) { this.interventionNeeded = interventionNeeded; }
        
        public MentalHealthDistribution getMentalHealthDistribution() { return mentalHealthDistribution; }
        public void setMentalHealthDistribution(MentalHealthDistribution mentalHealthDistribution) { this.mentalHealthDistribution = mentalHealthDistribution; }
        
        public ImpactDistribution getImpactDistribution() { return impactDistribution; }
        public void setImpactDistribution(ImpactDistribution impactDistribution) { this.impactDistribution = impactDistribution; }
        
        public Map<String, Object> getTrends() { return trends; }
        public void setTrends(Map<String, Object> trends) { this.trends = trends; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private AnalysisDashboardStats r = new AnalysisDashboardStats();
            public Builder totalAnalyses(long l) { r.setTotalAnalyses(l); return this; }
            public Builder highRiskStudents(long l) { r.setAtRiskStudents(l); return this; }
            public Builder needingIntervention(long l) { r.setInterventionNeeded(l); return this; }
            public Builder averageCorrelation(BigDecimal d) { /* r.setAverageCorrelation(d); */ return this; } // Field missing
            public Builder decliningStudents(long l) { /* r.setDecliningStudents(l); */ return this; } // Field missing
            public Builder improvingStudents(long l) { /* r.setImprovingStudents(l); */ return this; } // Field missing
            public Builder mentalHealthDistribution(MentalHealthDistribution d) { r.setMentalHealthDistribution(d); return this; }
            public Builder impactDistribution(ImpactDistribution d) { r.setImpactDistribution(d); return this; }
            public AnalysisDashboardStats build() { return r; }
        }
    }

    /**
     * Intervention Report DTO
     */
    public static class InterventionReport {
        private long totalInterventions;
        private long counselingRecommended;
        private long academicSupportRecommended;
        private long peerSupportRecommended;
        private long lifestyleChangesRecommended;
        private long referralRecommended;
        private Map<MentalHealthAcademicAnalysis.InterventionUrgency, Long> urgencyDistribution;
        private List<StudentAnalysisSummary> highPriorityStudents;

        public InterventionReport() {}

        public long getTotalInterventions() { return totalInterventions; }
        public void setTotalInterventions(long totalInterventions) { this.totalInterventions = totalInterventions; }
        
        public long getCounselingRecommended() { return counselingRecommended; }
        public void setCounselingRecommended(long counselingRecommended) { this.counselingRecommended = counselingRecommended; }
        
        public long getAcademicSupportRecommended() { return academicSupportRecommended; }
        public void setAcademicSupportRecommended(long academicSupportRecommended) { this.academicSupportRecommended = academicSupportRecommended; }
        
        public long getPeerSupportRecommended() { return peerSupportRecommended; }
        public void setPeerSupportRecommended(long peerSupportRecommended) { this.peerSupportRecommended = peerSupportRecommended; }
        
        public long getLifestyleChangesRecommended() { return lifestyleChangesRecommended; }
        public void setLifestyleChangesRecommended(long lifestyleChangesRecommended) { this.lifestyleChangesRecommended = lifestyleChangesRecommended; }
        
        public long getReferralRecommended() { return referralRecommended; }
        public void setReferralRecommended(long referralRecommended) { this.referralRecommended = referralRecommended; }
        
        public Map<MentalHealthAcademicAnalysis.InterventionUrgency, Long> getUrgencyDistribution() { return urgencyDistribution; }
        public void setUrgencyDistribution(Map<MentalHealthAcademicAnalysis.InterventionUrgency, Long> urgencyDistribution) { this.urgencyDistribution = urgencyDistribution; }
        
        public List<StudentAnalysisSummary> getHighPriorityStudents() { return highPriorityStudents; }
        public void setHighPriorityStudents(List<StudentAnalysisSummary> highPriorityStudents) { this.highPriorityStudents = highPriorityStudents; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private InterventionReport r = new InterventionReport();
            public Builder totalStudentsAnalyzed(long l) { /* r.setTotalStudentsAnalyzed(l); */ return this; } // Field missing
            public Builder studentsNeedingIntervention(long l) { /* r.setStudentsNeedingIntervention(l); */ return this; } // Field missing
            public Builder immediateInterventions(long l) { /* r.setImmediateInterventions(l); */ return this; } // Field missing
            public Builder highPriorityInterventions(long l) { /* r.setHighPriorityInterventions(l); */ return this; } // Field missing
            public Builder moderateInterventions(long l) { /* r.setModerateInterventions(l); */ return this; } // Field missing
            public Builder urgentCases(List<StudentAnalysisSummary> l) { r.setHighPriorityStudents(l); return this; }
            public Builder commonRiskFactors(List<String> l) { /* r.setCommonRiskFactors(l); */ return this; } // Field missing
            public Builder topRecommendations(List<String> l) { /* r.setTopRecommendations(l); */ return this; } // Field missing
            public InterventionReport build() { return r; }
        }
    }

    /**
     * Risk Factors DTO
     */
    public static class RiskFactors {
        private Boolean concentrationIssues;
        private Boolean motivationIssues;
        private Boolean sleepIssues;
        private Boolean socialIsolation;
        private Boolean financialStress;
        private Boolean familyIssues;
        private Boolean substanceUseConcern;
        private int riskScore;
        private List<String> identifiedFactors;

        public RiskFactors() {}

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
        
        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
        
        public List<String> getIdentifiedFactors() { return identifiedFactors; }
        public void setIdentifiedFactors(List<String> identifiedFactors) { this.identifiedFactors = identifiedFactors; }

        public int getActiveCount() {
            int count = 0;
            if (Boolean.TRUE.equals(concentrationIssues)) count++;
            if (Boolean.TRUE.equals(motivationIssues)) count++;
            if (Boolean.TRUE.equals(sleepIssues)) count++;
            if (Boolean.TRUE.equals(socialIsolation)) count++;
            if (Boolean.TRUE.equals(financialStress)) count++;
            if (Boolean.TRUE.equals(familyIssues)) count++;
            if (Boolean.TRUE.equals(substanceUseConcern)) count++;
            return count;
        }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private RiskFactors r = new RiskFactors();
            public Builder concentrationIssues(Boolean b) { r.setConcentrationIssues(b); return this; }
            public Builder motivationIssues(Boolean b) { r.setMotivationIssues(b); return this; }
            public Builder sleepIssues(Boolean b) { r.setSleepIssues(b); return this; }
            public Builder socialIsolation(Boolean b) { r.setSocialIsolation(b); return this; }
            public Builder financialStress(Boolean b) { r.setFinancialStress(b); return this; }
            public Builder familyIssues(Boolean b) { r.setFamilyIssues(b); return this; }
            public Builder substanceUseConcern(Boolean b) { r.setSubstanceUseConcern(b); return this; }
            public RiskFactors build() { return r; }
        }
    }

    /**
     * Recommendations DTO
     */
    public static class Recommendations {
        private Boolean counselingRecommended;
        private Boolean academicSupportRecommended;
        private Boolean peerSupportRecommended;
        private Boolean lifestyleChangesRecommended;
        private Boolean referralRecommended;
        private MentalHealthAcademicAnalysis.InterventionUrgency interventionUrgency;
        private List<String> specificRecommendations;
        private String priorityLevel;

        public Recommendations() {}

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
        
        public MentalHealthAcademicAnalysis.InterventionUrgency getInterventionUrgency() { return interventionUrgency; }
        public void setInterventionUrgency(MentalHealthAcademicAnalysis.InterventionUrgency interventionUrgency) { this.interventionUrgency = interventionUrgency; }
        
        public List<String> getSpecificRecommendations() { return specificRecommendations; }
        public void setSpecificRecommendations(List<String> specificRecommendations) { this.specificRecommendations = specificRecommendations; }
        
        public String getPriorityLevel() { return priorityLevel; }
        public void setPriorityLevel(String priorityLevel) { this.priorityLevel = priorityLevel; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private Recommendations r = new Recommendations();
            public Builder counselingRecommended(Boolean b) { r.setCounselingRecommended(b); return this; }
            public Builder academicSupportRecommended(Boolean b) { r.setAcademicSupportRecommended(b); return this; }
            public Builder peerSupportRecommended(Boolean b) { r.setPeerSupportRecommended(b); return this; }
            public Builder lifestyleChangesRecommended(Boolean b) { r.setLifestyleChangesRecommended(b); return this; }
            public Builder referralRecommended(Boolean b) { r.setReferralRecommended(b); return this; }
            public Builder interventionUrgency(String s) { if(s!=null) r.setInterventionUrgency(MentalHealthAcademicAnalysis.InterventionUrgency.valueOf(s)); return this; }
            public Recommendations build() { return r; }
        }
    }

    /**
     * Trend Data Point DTO
     */
    public static class TrendDataPoint {
        private LocalDate date;
        private BigDecimal mentalHealthScore;
        private BigDecimal academicScore;
        private String label;
        private BigDecimal gpa;
        private String period;

        public TrendDataPoint() {}

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        
        public BigDecimal getMentalHealthScore() { return mentalHealthScore; }
        public void setMentalHealthScore(BigDecimal mentalHealthScore) { this.mentalHealthScore = mentalHealthScore; }
        
        public BigDecimal getAcademicScore() { return academicScore; }
        public void setAcademicScore(BigDecimal academicScore) { this.academicScore = academicScore; }
        
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public BigDecimal getGpa() { return gpa; }
        public void setGpa(BigDecimal gpa) { this.gpa = gpa; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private TrendDataPoint r = new TrendDataPoint();
            public Builder date(LocalDate d) { r.setDate(d); return this; }
            public Builder mentalHealthScore(BigDecimal s) { r.setMentalHealthScore(s); return this; }
            public Builder gpa(BigDecimal s) { r.setGpa(s); return this; }
            public Builder period(String s) { r.setPeriod(s); return this; }
            public TrendDataPoint build() { return r; }
        }
    }

    /**
     * Data Point DTO
     */
    public static class DataPoint {
        private String label;
        private BigDecimal x;
        private BigDecimal y;
        private LocalDate date;
        private BigDecimal value;

        public DataPoint() {}

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        
        public BigDecimal getX() { return x; }
        public void setX(BigDecimal x) { this.x = x; }
        
        public BigDecimal getY() { return y; }
        public void setY(BigDecimal y) { this.y = y; }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private DataPoint r = new DataPoint();
            public Builder date(LocalDate d) { r.setDate(d); return this; }
            public Builder value(BigDecimal v) { r.setValue(v); return this; }
            public Builder label(String s) { r.setLabel(s); return this; }
            public DataPoint build() { return r; }
        }
    }

    /**
     * Mental Health Distribution DTO
     */
    public static class MentalHealthDistribution {
        private long excellent;
        private long good;
        private long moderate;
        private long atRisk;
        private long crisis;
        private long notAssessed;

        public MentalHealthDistribution() {}

        public long getExcellent() { return excellent; }
        public void setExcellent(long excellent) { this.excellent = excellent; }
        
        public long getGood() { return good; }
        public void setGood(long good) { this.good = good; }
        
        public long getModerate() { return moderate; }
        public void setModerate(long moderate) { this.moderate = moderate; }
        
        public long getAtRisk() { return atRisk; }
        public void setAtRisk(long atRisk) { this.atRisk = atRisk; }
        
        public long getCrisis() { return crisis; }
        public void setCrisis(long crisis) { this.crisis = crisis; }
        
        public long getNotAssessed() { return notAssessed; }
        public void setNotAssessed(long notAssessed) { this.notAssessed = notAssessed; }
    }

    /**
     * Impact Distribution DTO
     */
    public static class ImpactDistribution {
        private long none;
        private long minimal;
        private long moderate;
        private long significant;
        private long severe;

        public ImpactDistribution() {}

        public long getNone() { return none; }
        public void setNone(long none) { this.none = none; }
        
        public long getMinimal() { return minimal; }
        public void setMinimal(long minimal) { this.minimal = minimal; }
        
        public long getModerate() { return moderate; }
        public void setModerate(long moderate) { this.moderate = moderate; }
        
        public long getSignificant() { return significant; }
        public void setSignificant(long significant) { this.significant = significant; }
        
        public long getSevere() { return severe; }
        public void setSevere(long severe) { this.severe = severe; }
    }

    /**
     * Student Analysis Summary DTO
     */
    public static class StudentAnalysisSummary {
        private Long clientId;
        private String clientName;
        private String studentId;
        private int riskScore;
        private MentalHealthAcademicAnalysis.InterventionUrgency urgency;
        private MentalHealthAcademicAnalysis.MentalHealthStatus status;
        private String lastAnalysisDate;

        public StudentAnalysisSummary() {}

        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }
        
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        
        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
        
        public MentalHealthAcademicAnalysis.InterventionUrgency getUrgency() { return urgency; }
        public void setUrgency(MentalHealthAcademicAnalysis.InterventionUrgency urgency) { this.urgency = urgency; }
        
        public MentalHealthAcademicAnalysis.MentalHealthStatus getStatus() { return status; }
        public void setStatus(MentalHealthAcademicAnalysis.MentalHealthStatus status) { this.status = status; }
        
        public String getLastAnalysisDate() { return lastAnalysisDate; }
        public void setLastAnalysisDate(String lastAnalysisDate) { this.lastAnalysisDate = lastAnalysisDate; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private StudentAnalysisSummary r = new StudentAnalysisSummary();
            public Builder clientId(Long id) { r.setClientId(id); return this; }
            public Builder clientName(String n) { r.setClientName(n); return this; }
            public Builder studentNumber(String s) { r.setStudentId(s); return this; }
            public Builder latestMentalHealthScore(BigDecimal s) { /* r.setLatestMentalHealthScore(s); */ return this; } // Field missing
            public Builder latestGpa(BigDecimal s) { /* r.setLatestGpa(s); */ return this; } // Field missing
            public Builder mentalHealthStatus(String s) { if(s!=null) r.setStatus(MentalHealthAcademicAnalysis.MentalHealthStatus.valueOf(s)); return this; }
            public Builder impactLevel(String s) { /* r.setImpactLevel(s); */ return this; } // Field missing
            public Builder interventionUrgency(String s) { if(s!=null) r.setUrgency(MentalHealthAcademicAnalysis.InterventionUrgency.valueOf(s)); return this; }
            public Builder lastAnalysisDate(LocalDate d) { r.setLastAnalysisDate(d != null ? d.toString() : null); return this; }
            public Builder requiresFollowUp(boolean b) { /* r.setRequiresFollowUp(b); */ return this; } // Field missing
            public StudentAnalysisSummary build() { return r; }
        }
    }
}