package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "clinic_referrals")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referral_number", nullable = false, unique = true, length = 50)
    private String referralNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private User counselor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case linkedCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Urgency urgency = Urgency.ROUTINE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReferralStatus status = ReferralStatus.PENDING;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "clinical_notes", columnDefinition = "TEXT")
    private String clinicalNotes;

    @Column(name = "clinic_notes", columnDefinition = "TEXT")
    private String clinicNotes;

    @Column(name = "clinic_appointment_date")
    private LocalDateTime clinicAppointmentDate;

    // ID assigned by the clinic system once they accept — used for cross-system linking
    @Column(name = "external_reference_id", length = 200)
    private String externalReferenceId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum Urgency {
        ROUTINE, URGENT, EMERGENCY
    }

    public enum ReferralStatus {
        PENDING,   // created locally, not yet sent
        SENT,      // transmitted to clinic
        ACCEPTED,  // clinic acknowledged
        COMPLETED, // clinic visit happened
        DECLINED   // clinic declined or client did not attend
    }
}
