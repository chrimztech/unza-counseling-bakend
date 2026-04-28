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
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "widget_id", nullable = false)
    private String widgetId;
    
    @Column(name = "widget_type", nullable = false)
    private String widgetType;
    
    @Column(name = "position_x")
    private Integer positionX;
    
    @Column(name = "position_y")
    private Integer positionY;
    
    private Integer width;
    
    private Integer height;
    
    private Boolean visible;
    
    @Column(name = "config_json")
    private String configJson;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
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
