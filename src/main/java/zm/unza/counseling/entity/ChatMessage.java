package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@EntityListeners(AuditingEntityListener.class)
@Data
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "is_from_user")
    private Boolean isFromUser = true;

    // --- Oversight metrics (admin "AI Oversight" dashboard). Nullable: older rows predate them. ---

    /** Server-side time to produce the reply, in milliseconds (retrieval + model call). */
    @Column(name = "reply_ms")
    private Long replyMs;

    /** How many counsellor scenarios were matched and used to guide the reply. */
    @Column(name = "matched_scenarios")
    private Integer matchedScenarios;

    /** Crisis rating of the student's message: NONE, HIGH or CRITICAL. */
    @Column(name = "crisis_level", length = 10)
    private String crisisLevel;

    /** False when no provider answered and the canned offline reply was sent. */
    @Column(name = "ai_answered")
    private Boolean aiAnswered;

    /** Position of the provider that answered in the fallback chain (1 = default provider). */
    @Column(name = "provider_attempt")
    private Integer providerAttempt;

    /** Name of the provider configuration that answered (admin-only; never shown to students). */
    @Column(name = "provider_name", length = 100)
    private String providerName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}