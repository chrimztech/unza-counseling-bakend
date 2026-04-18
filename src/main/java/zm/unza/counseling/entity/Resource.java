package zm.unza.counseling.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    
    @Column(name = "tags", columnDefinition = "text[]")
    @ColumnTransformer(
            read = "coalesce(array_to_string(tags, ','), '')",
            write = "coalesce(string_to_array(nullif(?, ''), ','), ARRAY[]::text[])"
    )
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String tagsValue = "";
    
    @Column(name = "is_public")
    private boolean isPublic = true;
    
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

    @JsonProperty("isActive")
    public boolean getIsActive() {
        return isPublic;
    }

    @JsonProperty("isActive")
    public void setIsActive(Boolean active) {
        this.isPublic = active == null || active;
    }

    @JsonProperty("tags")
    public List<String> getTags() {
        if (tagsValue == null || tagsValue.isBlank()) {
            return List.of();
        }

        return Arrays.stream(tagsValue.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @JsonProperty("tags")
    public void setTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            this.tagsValue = "";
            return;
        }

        this.tagsValue = tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .collect(Collectors.joining(","));
    }
}
