package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * The wellness assistant's long-term memory of one user: an AI-maintained summary of every
 * chat exchange older than the ones still sent verbatim. Together with the unsummarised recent
 * messages this gives the assistant the user's whole conversation history without sending an
 * ever-growing transcript to the provider.
 *
 * Deleted when the user clears their chat history.
 */
@Entity
@Table(name = "ai_user_memory")
@Data
public class AiUserMemory {

    /** Same as users.id — one memory per user. */
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String summary;

    /** Highest chat_messages.id folded into {@link #summary}; later messages are sent verbatim. */
    @Column(name = "summarized_through_id")
    private Long summarizedThroughId = 0L;

    /** How many exchanges the summary covers, for admins and diagnostics. */
    @Column(name = "summarized_count")
    private Integer summarizedCount = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
