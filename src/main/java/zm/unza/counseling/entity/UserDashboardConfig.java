package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing user's dashboard widget configuration
 */
@Entity
@Table(name = "user_dashboard_config")
@Data
@AllArgsConstructor 
@NoArgsConstructor
public class UserDashboardConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String widgetId;
    
    @Column(nullable = false)
    private String widgetType;
    
    private Integer positionX;
    
    private Integer positionY;
    
    private Integer width;
    
    private Integer height;
    
    private Boolean visible;
    
    private String configJson;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
