package zm.unza.counseling.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Session Entity - Records of completed counseling sessions
 */
@Entity
@Table(name = "sessions", indexes = {
    @Index(name = "idx_session_date", columnList = "sessionDate"),
    @Index(name = "idx_session_student", columnList = "student_id"),
    @Index(name = "idx_session_counselor", columnList = "counselor_id")
})
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User counselor;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime sessionDate;

    @NotNull
    @Column(nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.COMPLETED;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(length = 5000)
    private String presentingIssue; // Main concern/reason

    @Column(length = 5000)
    private String sessionNotes; // Detailed session notes

    @Column(length = 2000)
    private String interventions; // Actions taken

    @Column(length = 2000)
    private String recommendations; // Recommendations given

    @Column(length = 2000)
    private String treatmentPlan; // Ongoing treatment plan

    private Boolean followUpRequired = false;

    private LocalDateTime followUpDate;

    @Column(length = 1000)
    private String followUpNotes;

    @Enumerated(EnumType.STRING)
    private Outcome outcome;

    private Integer studentSatisfactionRating; // 1-5

    @Column(length = 1000)
    private String studentFeedback;

    @ElementCollection
    @CollectionTable(name = "session_tags", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "session_attachments", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "attachment_url")
    private List<String> attachments = new ArrayList<>();

    @Column(nullable = false)
    private Boolean confidential = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public User getCounselor() { return counselor; }
    public void setCounselor(User counselor) { this.counselor = counselor; }
    public LocalDateTime getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDateTime sessionDate) { this.sessionDate = sessionDate; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public SessionType getType() { return type; }
    public void setType(SessionType type) { this.type = type; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPresentingIssue() { return presentingIssue; }
    public void setPresentingIssue(String presentingIssue) { this.presentingIssue = presentingIssue; }
    public String getSessionNotes() { return sessionNotes; }
    public void setSessionNotes(String sessionNotes) { this.sessionNotes = sessionNotes; }
    public String getInterventions() { return interventions; }
    public void setInterventions(String interventions) { this.interventions = interventions; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }
    public Boolean getFollowUpRequired() { return followUpRequired; }
    public void setFollowUpRequired(Boolean followUpRequired) { this.followUpRequired = followUpRequired; }
    public LocalDateTime getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDateTime followUpDate) { this.followUpDate = followUpDate; }
    public String getFollowUpNotes() { return followUpNotes; }
    public void setFollowUpNotes(String followUpNotes) { this.followUpNotes = followUpNotes; }
    public Outcome getOutcome() { return outcome; }
    public void setOutcome(Outcome outcome) { this.outcome = outcome; }
    public Integer getStudentSatisfactionRating() { return studentSatisfactionRating; }
    public void setStudentSatisfactionRating(Integer studentSatisfactionRating) { this.studentSatisfactionRating = studentSatisfactionRating; }
    public String getStudentFeedback() { return studentFeedback; }
    public void setStudentFeedback(String studentFeedback) { this.studentFeedback = studentFeedback; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
    public Boolean getConfidential() { return confidential; }
    public void setConfidential(Boolean confidential) { this.confidential = confidential; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum SessionType {
        INDIVIDUAL("Individual Counseling"),
        GROUP("Group Session"),
        CRISIS("Crisis Intervention"),
        FOLLOW_UP("Follow-up Session"),
        ASSESSMENT("Assessment Session"),
        CONSULTATION("Consultation");

        private final String displayName;

        SessionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SessionStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }

    public enum Outcome {
        EXCELLENT("Excellent Progress"),
        GOOD("Good Progress"),
        FAIR("Fair Progress"),
        MINIMAL("Minimal Progress"),
        REFER("Referred to Specialist");

        private final String description;

        Outcome(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}