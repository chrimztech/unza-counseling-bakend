package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zm.unza.counseling.dto.request.FileUploadRequest;
import zm.unza.counseling.entity.Resource;
import zm.unza.counseling.repository.ResourceRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResourceService {
    
    private final ResourceRepository resourceRepository;
    private static final String UPLOAD_DIR = "uploads/resources/";

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    public Resource createResource(Resource resource) {
        return resourceRepository.save(resource);
    }

    public void deleteResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
        
        // Delete file from disk if it exists
        if (resource.getFileKey() != null) {
            Path filePath = Paths.get(UPLOAD_DIR, resource.getFileKey());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log the error but don't fail the deletion
                System.err.println("Could not delete file: " + e.getMessage());
            }
        }
        
        resourceRepository.delete(resource);
    }

    public List<Resource> searchResources(String query) {
        return resourceRepository.findByTitleContainingOrDescriptionContaining(query, query);
    }

    public List<String> getResourceCategories() {
        return resourceRepository.findDistinctCategories();
    }

    public Map<String, Object> getResourceStatistics() {
        return null;
    }

    public List<Resource> getResourcesByType(String type) {
        return resourceRepository.findByType(type);
    }

    public List<Resource> getResourcesByCategory(String category) {
        return resourceRepository.findByCategory(category);
    }

    public List<Resource> getFeaturedResources() {
        return resourceRepository.findByFeaturedTrue();
    }

    public byte[] exportResources(String format) {
        return new byte[0];
    }

    public Resource uploadResource(FileUploadRequest request) {
        MultipartFile file = request.getFile();
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Validate file type
        String fileType = file.getContentType();
        if (!isValidFileType(fileType)) {
            throw new IllegalArgumentException("Invalid file type: " + fileType);
        }

        // Validate file size (10MB max)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("Could not create upload directory", e);
            }
        }

        // Generate unique file name
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = System.currentTimeMillis() + fileExtension;
        
        try {
            // Save file to disk
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.write(filePath, file.getBytes());

            // Create Resource entity
            Resource resource = new Resource();
            resource.setTitle(request.getTitle());
            resource.setDescription(request.getDescription());
            resource.setType(request.getType());
            resource.setCategory(request.getCategory());
            resource.setFeatured(request.isFeatured());
            resource.setFileName(originalFilename);
            resource.setFileType(fileType);
            resource.setFileSize(file.getSize());
            resource.setFileUrl("/api/v1/resources/download/" + uniqueFilename);
            resource.setFileKey(uniqueFilename);

            return resourceRepository.save(resource);
        } catch (IOException e) {
            throw new RuntimeException("Could not save file: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<byte[]> downloadResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        Path filePath = Paths.get(UPLOAD_DIR, resource.getFileKey());
        
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found on server");
        }

        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + resource.getFileName())
                    .header("Content-Type", resource.getFileType())
                    .body(fileBytes);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + e.getMessage(), e);
        }
    }

    private boolean isValidFileType(String fileType) {
        // Allow common file types for mental health resources
        String[] allowedTypes = {
            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain", "text/csv", "text/html",
            "image/jpeg", "image/png", "image/gif", "image/tiff",
            "audio/mpeg", "audio/wav", "audio/ogg", "audio/mp4",
            "video/mp4", "video/quicktime", "video/x-msvideo", "video/x-ms-wmv"
        };
        
        for (String type : allowedTypes) {
            if (type.equals(fileType)) {
                return true;
            }
        }
        return false;
    }
}
