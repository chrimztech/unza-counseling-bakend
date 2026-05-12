package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "scholarship_recommendations")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scholarship_id", nullable = false)
    private Scholarship scholarship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "recommended_by", nullable = false)
    private Long recommendedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationStatus status = RecommendationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "financial_need_level")
    private FinancialNeedLevel financialNeedLevel;

    @Column(name = "vulnerability_score")
    private Integer vulnerabilityScore;

    @Column(name = "academic_standing")
    private String academicStanding;

    @Column(name = "personal_statement", columnDefinition = "TEXT")
    private String personalStatement;

    @Column(name = "supporting_notes", columnDefinition = "TEXT")
    private String supportingNotes;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "awarded_date")
    private LocalDate awardedDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum RecommendationStatus {
        PENDING,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        AWARDED
    }

    public enum FinancialNeedLevel {
        CRITICAL,
        HIGH,
        MODERATE,
        LOW
    }
}
