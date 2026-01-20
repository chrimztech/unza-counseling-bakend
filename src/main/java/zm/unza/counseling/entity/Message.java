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

    private LocalDateTime sentAt;
    private boolean isRead;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}