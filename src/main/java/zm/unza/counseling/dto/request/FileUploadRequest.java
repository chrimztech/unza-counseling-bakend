package zm.unza.counseling.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadRequest {
    private MultipartFile file;
    private String title;
    private String description;
    private String type; // ARTICLE, VIDEO, DOCUMENT
    private String category;
    private boolean featured;
}