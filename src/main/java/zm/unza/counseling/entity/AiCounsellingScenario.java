package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * One counsellor-written dialogue scenario (student message → ideal response) from the
 * "AI Counselling Dialogue Template" workbook, or added by hand in Admin → AI Knowledge Base.
 *
 * At chat time the most similar enabled scenarios are retrieved and shown to the LLM as
 * worked examples (see AiKnowledgeService). Contributor names and student demographics from
 * the sheet are deliberately NOT stored — the dataset instructions require contributor
 * anonymity and the model doesn't need them.
 *
 * {@code source} and {@code riskLevel} are plain strings rather than enums so adding a value
 * never needs a schema change (Hibernate maps enums to native MySQL ENUM columns).
 */
@Entity
@Table(name = "ai_counselling_scenarios", indexes = {
        @Index(name = "idx_ai_scen_category", columnList = "problem_category"),
        @Index(name = "idx_ai_scen_source", columnList = "source")
})
@EntityListeners(AuditingEntityListener.class)
@Data
public class AiCounsellingScenario {

    public static final String SOURCE_IMPORT = "IMPORT";
    public static final String SOURCE_MANUAL = "MANUAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Scenario_ID from the sheet (e.g. "FIN-001", "194"); informational only. */
    @Column(name = "scenario_code", length = 50)
    private String scenarioCode;

    @Column(name = "problem_category", length = 100)
    private String problemCategory;

    /** LOW, MODERATE, HIGH, CRITICAL, or null. */
    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "student_message", columnDefinition = "TEXT", nullable = false)
    private String studentMessage;

    @Column(name = "ideal_response", columnDefinition = "TEXT")
    private String idealResponse;

    /** Escalation / referral guidance, e.g. "Yes (offer human counsellor)", "refer to Gamaid Zambia". */
    @Column(name = "escalation", length = 500)
    private String escalation;

    @Column(name = "key_topics", length = 500)
    private String keyTopics;

    @Column(name = "notes_for_ai", columnDefinition = "TEXT")
    private String notesForAi;

    /**
     * True when the response was drafted by AI (Response_Source = "AI draft ...") rather than
     * written by a counsellor. Cleared when a counsellor reviews it. Nullable so adding the column
     * to an existing table is safe; null means false.
     */
    @Column(name = "ai_drafted")
    private Boolean aiDrafted;

    /** Reviewer guidance from AI_Review_Note, e.g. a risk label that looks wrong. Never sent to the LLM. */
    @Column(name = "review_note", length = 500)
    private String reviewNote;

    /** True for the hand-picked "Example (One Completed Row)" scenarios; preferred in retrieval ties. */
    @Column(name = "gold_example", nullable = false)
    private Boolean goldExample = false;

    @Column(nullable = false)
    private Boolean enabled = true;

    /** Maintained on save: only scenarios with a response are used as examples. */
    @Column(name = "has_response", nullable = false)
    private Boolean hasResponse = false;

    @Column(nullable = false, length = 20)
    private String source = SOURCE_IMPORT;

    @Column(name = "import_file", length = 255)
    private String importFile;

    @Column(name = "updated_by", length = 150)
    private String updatedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void syncHasResponse() {
        hasResponse = idealResponse != null && !idealResponse.isBlank();
    }
}
