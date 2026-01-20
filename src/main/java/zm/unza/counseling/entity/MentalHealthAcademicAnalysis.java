package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mental_health_academic_analysis")
@EntityListeners(AuditingEntityListener.class)
public class MentalHealthAcademicAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analyzed_by")
    private User analyzedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_performance_id")
    private AcademicPerformance academicPerformance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "self_assessment_id")
    private SelfAssessment selfAssessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_assessment_id")
    private RiskAssessment riskAssessment;

    private LocalDate analysisDate;
    private LocalDate analysisPeriodStart;
    private LocalDate analysisPeriodEnd;

    // Mental Health Indicators
    private BigDecimal depressionScore;
    private BigDecimal anxietyScore;
    private BigDecimal stressScore;
    private BigDecimal overallMentalHealthScore;
    
    @Enumerated(EnumType.STRING)
    private MentalHealthStatus mentalHealthStatus;

    // Academic Performance Indicators
    private BigDecimal currentGpa;
    private BigDecimal gpaChange;
    private BigDecimal attendanceRate;
    private BigDecimal attendanceChange;

    // Correlation Analysis
    private BigDecimal correlationScore;
    
    @Enumerated(EnumType.STRING)
    private CorrelationStrength correlationStrength;

    @Enumerated(EnumType.STRING)
    private ImpactLevel impactLevel;

    @Enumerated(EnumType.STRING)
    private TrendDirection trendDirection;

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
    
    @Enumerated(EnumType.STRING)
    private InterventionUrgency interventionUrgency;

    // Analysis Notes
    @Column(columnDefinition = "TEXT")
    private String analysisSummary;
    
    @Column(columnDefinition = "TEXT")
    private String analysisNotes;

    @Column(columnDefinition = "TEXT")
    private String recommendations;
    
    @Column(columnDefinition = "TEXT")
    private String counselorNotes;

    // Metadata
    private Boolean isAiGenerated;
    private BigDecimal aiConfidenceScore;

    private Boolean interventionNeeded;

    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateInterventionNeeded() {
        this.interventionNeeded = calculateInterventionNeeded();
    }

    // Helper methods
    public boolean isInterventionNeeded() {
        if (interventionNeeded != null) {
            return interventionNeeded;
        }
        return calculateInterventionNeeded();
    }

    private boolean calculateInterventionNeeded() {
        return (interventionUrgency != null && interventionUrgency != InterventionUrgency.LOW) ||
               Boolean.TRUE.equals(counselingRecommended) ||
               Boolean.TRUE.equals(referralRecommended);
    }

    public int calculateOverallRiskScore() {
        int score = 0;
        if (mentalHealthStatus == MentalHealthStatus.CRISIS) score += 30;
        else if (mentalHealthStatus == MentalHealthStatus.AT_RISK) score += 20;
        
        if (impactLevel == ImpactLevel.CRITICAL) score += 30;
        else if (impactLevel == ImpactLevel.SEVERE) score += 20;
        else if (impactLevel == ImpactLevel.MODERATE) score += 10;
        
        if (interventionUrgency == InterventionUrgency.IMMEDIATE) score += 40;
        else if (interventionUrgency == InterventionUrgency.HIGH) score += 20;
        
        return Math.min(score, 100);
    }

    public enum MentalHealthStatus {
        STABLE, AT_RISK, CRISIS, IMPROVING, DECLINING
    }

    public enum ImpactLevel {
        NONE, MILD, MODERATE, SEVERE, CRITICAL
    }

    public enum InterventionUrgency {
        LOW, MEDIUM, HIGH, IMMEDIATE
    }

    public enum TrendDirection {
        IMPROVING, STABLE, DECLINING
    }

    public enum CorrelationStrength {
        STRONG_POSITIVE,
        MODERATE_POSITIVE,
        WEAK_POSITIVE,
        NONE,
        WEAK_NEGATIVE,
        MODERATE_NEGATIVE,
        STRONG_NEGATIVE
    }
}