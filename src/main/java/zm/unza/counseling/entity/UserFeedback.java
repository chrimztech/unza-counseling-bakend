package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for user feedback/in-app surveys
 */
@Entity
@Table(name = "user_feedback")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "feedback_type", nullable = false)
    private String feedbackType;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    private Integer rating;
    
    private String status;
    
    @Column(name = "admin_response")
    private String adminResponse;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
