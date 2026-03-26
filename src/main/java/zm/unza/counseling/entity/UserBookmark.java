package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for user bookmarks/favorites
 */
@Entity
@Table(name = "user_bookmarks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBookmark {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String title;
    
    private String description;
    
    @Column(nullable = false)
    private String url;
    
    private String icon;
    
    private String category;
    
    private Integer usageCount;
    
    private LocalDateTime lastUsedAt;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        usageCount = 0;
    }
}
