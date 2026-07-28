package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * SecurityAlert Entity — sensitive-case alerts (self-harm, suicide, sexual assault,
 * physical attack, panic button, other) surfaced to university Security staff.
 *
 * Alerts can originate locally (this counseling system) or be mirrored in from the
 * clinic system (originSystem/externalAlertId/externalSystem track that linkage).
 */
@Entity
@Table(name = "security_alerts")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_system", nullable = false, length = 20)
    private OriginSystem originSystem;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private SourceType sourceType;

    @Column(name = "subject_student_id", length = 50)
    private String subjectStudentId;

    @Column(name = "subject_name", length = 200)
    private String subjectName;

    @Column(name = "reported_by_user_id", length = 50)
    private String reportedByUserId;

    @Column(name = "reported_by_name", length = 200)
    private String reportedByName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.NEW;

    @Column(name = "acknowledged_by_name", length = 200)
    private String acknowledgedByName;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "resolved_by_name", length = 200)
    private String resolvedByName;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    // The OTHER system's own local id for this same logical alert, once synced
    @Column(name = "external_alert_id", length = 100)
    private String externalAlertId;

    @Enumerated(EnumType.STRING)
    @Column(name = "external_system", length = 20)
    private ExternalSystem externalSystem;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum Category {
        SELF_HARM, SUICIDE, RAPE_OR_SEXUAL_ASSAULT, PHYSICAL_ATTACK, PANIC_BUTTON, OTHER
    }

    public enum Severity {
        HIGH, CRITICAL
    }

    public enum OriginSystem {
        CLINIC, COUNSELLING
    }

    public enum SourceType {
        MANUAL_REPORT, CRISIS_DETECTION, PANIC_BUTTON
    }

    public enum Status {
        NEW, ACKNOWLEDGED, RESOLVED, FALSE_POSITIVE
    }

    public enum ExternalSystem {
        CLINIC, COUNSELLING
    }
}
