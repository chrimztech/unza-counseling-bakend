package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "resources")
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String type; // ARTICLE, VIDEO, DOCUMENT
    private String category;
    private boolean featured;
    private String url;
    private LocalDateTime createdAt;

    // File upload fields
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String fileKey;

    // Additional fields
    @ElementCollection
    private List<String> tags;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}