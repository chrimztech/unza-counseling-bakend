package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "conversation_id")
    private Long conversationId;

    private LocalDateTime sentAt;
    private boolean isRead;
    private boolean isDelivered;
    private boolean isArchived;
    private boolean isStarred;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}