package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "clinic_visits")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_id")
    private ClinicReferral referral;

    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", nullable = false, length = 30)
    private VisitType visitType = VisitType.GENERAL;

    @Column(name = "visit_purpose", length = 500)
    private String visitPurpose;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // 'MANUAL' when entered by counselor, 'CLINIC_WEBHOOK' when pushed from clinic system
    @Column(name = "recorded_by", nullable = false, length = 50)
    private String recordedBy = "MANUAL";

    @Column(name = "counselor_notified")
    private Boolean counselorNotified = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum VisitType {
        GENERAL,
        MENTAL_HEALTH,
        FOLLOW_UP,
        EMERGENCY,
        REFERRAL_FOLLOW_UP
    }
}
