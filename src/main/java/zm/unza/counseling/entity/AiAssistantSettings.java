package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Singleton (id = 1) super-admin settings for what the wellness assistant knows: real crisis
 * contacts and campus services (so the AI never has to invent a phone number), extra
 * instructions, and how many counsellor scenarios to retrieve per reply.
 */
@Entity
@Table(name = "ai_assistant_settings")
@EntityListeners(AuditingEntityListener.class)
@Data
public class AiAssistantSettings {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "knowledge_enabled", nullable = false)
    private Boolean knowledgeEnabled = true;

    @Column(name = "examples_per_reply", nullable = false)
    private Integer examplesPerReply = 3;

    /** Long-term memory: summarise older conversations so the assistant remembers them. Null means true. */
    @Column(name = "memory_enabled")
    private Boolean memoryEnabled = true;

    /**
     * Let the assistant see the user's own records (self-assessment scores, upcoming appointments,
     * latest wellness analysis status). Counsellor session notes are never included. Null means true.
     */
    @Column(name = "records_enabled")
    private Boolean recordsEnabled = true;

    /** Whether AI-drafted responses not yet reviewed by a counsellor are used as examples. Null means true. */
    @Column(name = "use_ai_drafts")
    private Boolean useAiDrafts = true;

    /** Real phone numbers / places to go in an emergency. The AI only quotes what is here. */
    @Column(name = "crisis_contacts", columnDefinition = "TEXT")
    private String crisisContacts;

    /** UNZA offices and services the assistant may refer students to. */
    @Column(name = "campus_services", columnDefinition = "TEXT")
    private String campusServices;

    @Column(name = "additional_instructions", columnDefinition = "TEXT")
    private String additionalInstructions;

    @Column(name = "last_import_at")
    private LocalDateTime lastImportAt;

    @Column(name = "last_import_file", length = 255)
    private String lastImportFile;

    @Column(name = "last_import_summary", length = 1000)
    private String lastImportSummary;

    @Column(name = "updated_by", length = 150)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
