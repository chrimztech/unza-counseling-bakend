package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "self_assessments")
public class SelfAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String questionsJson;

    @Column(name = "submitted_by_user_id")
    private Long submittedByUserId;

    @Column(name = "assessment_date")
    private LocalDateTime assessmentDate;

    @Column(name = "responses_json", columnDefinition = "TEXT")
    private String responsesJson;

    @Column(name = "phq9_score")
    private Integer phq9Score;

    @Column(name = "gad7_score")
    private Integer gad7Score;

    @Column(name = "pss_score")
    private Integer pssScore;

    @Column(name = "sleep_quality")
    private Integer sleepQuality;

    @Column(name = "overall_wellness")
    private Integer overallWellness;

    @Column(name = "appetite_changes", nullable = false)
    private Boolean appetiteChanges = false;

    @Column(name = "concentration_difficulty", nullable = false)
    private Boolean concentrationDifficulty = false;

    @Column(name = "social_withdrawal", nullable = false)
    private Boolean socialWithdrawal = false;

    @Column(name = "submitted_as_anonymous", nullable = false)
    private Boolean submittedAsAnonymous = false;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Transient
    private Map<String, Object> responses;
}
