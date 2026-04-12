package zm.unza.counseling.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "resources")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 500)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "content_type", length = 50)
    private String contentType;
    
    @Column(name = "type", length = 50)
    private String type;
    
    @Column(name = "category", length = 100)
    private String category;
    
    @Column(name = "featured")
    private boolean featured;
    
    @Column(name = "url", length = 2000)
    private String url;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "file_name", length = 255)
    private String fileName;
    
    @Column(name = "file_type", length = 100)
    private String fileType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "file_url", length = 2000)
    private String fileUrl;
    
    @Column(name = "file_key", length = 255)
    private String fileKey;
    
    @Column(name = "file_path", length = 2000)
    private String filePath;
    
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;
    
    @Column(name = "is_public")
    private boolean isPublic;
    
    @Column(name = "download_count")
    private int downloadCount;
    
    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (!isPublic) {
            isPublic = false;
        }
        if (downloadCount == 0) {
            downloadCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}