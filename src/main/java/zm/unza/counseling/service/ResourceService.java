package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zm.unza.counseling.dto.request.FileUploadRequest;
import zm.unza.counseling.dto.request.ResourceUpdateRequest;
import zm.unza.counseling.entity.Resource;
import zm.unza.counseling.repository.ResourceRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private static final String UPLOAD_DIR = "uploads/resources/";

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    public Resource getResourceById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    }

    public Resource createResource(Resource resource) {
        prepareNewResource(resource);
        return resourceRepository.save(resource);
    }

    public Resource createResource(ResourceUpdateRequest request) {
        Resource resource = new Resource();

        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType());
        resource.setContentType(request.getContentType() != null ? request.getContentType() : request.getType());
        resource.setCategory(request.getCategory());
        resource.setUrl(request.getUrl());
        resource.setTags(request.getTags());
        resource.setFileName(request.getFileName());
        resource.setFileType(request.getFileType());
        resource.setFileSize(request.getFileSize());
        resource.setFileUrl(request.getFileUrl());
        resource.setFileKey(request.getFileKey());
        resource.setFilePath(request.getFilePath());
        resource.setUploadedBy(request.getUploadedBy());

        if (request.getFeatured() != null) {
            resource.setFeatured(request.getFeatured());
        }
        if (request.getIsActive() != null) {
            resource.setIsActive(request.getIsActive());
        }

        prepareNewResource(resource);
        return resourceRepository.save(resource);
    }

    public Resource updateResource(Long id, ResourceUpdateRequest updates) {
        Resource existing = getResourceById(id);

        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getType() != null) existing.setType(updates.getType());
        if (updates.getContentType() != null) existing.setContentType(updates.getContentType());
        if (updates.getCategory() != null) existing.setCategory(updates.getCategory());
        if (updates.getUrl() != null) existing.setUrl(updates.getUrl());
        if (updates.getTags() != null) existing.setTags(updates.getTags());
        if (updates.getFileName() != null) existing.setFileName(updates.getFileName());
        if (updates.getFileType() != null) existing.setFileType(updates.getFileType());
        if (updates.getFileSize() != null) existing.setFileSize(updates.getFileSize());
        if (updates.getFileUrl() != null) existing.setFileUrl(updates.getFileUrl());
        if (updates.getFileKey() != null) existing.setFileKey(updates.getFileKey());
        if (updates.getFilePath() != null) existing.setFilePath(updates.getFilePath());
        if (updates.getUploadedBy() != null) existing.setUploadedBy(updates.getUploadedBy());

        if (updates.getFeatured() != null) existing.setFeatured(updates.getFeatured());
        if (updates.getIsActive() != null) existing.setIsActive(updates.getIsActive());
        existing.setUpdatedAt(LocalDateTime.now());

        return resourceRepository.save(existing);
    }

    public void deleteResource(Long id) {
        Resource resource = getResourceById(id);

        if (resource.getFileKey() != null) {
            Path filePath = Paths.get(UPLOAD_DIR, resource.getFileKey());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
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
        List<Resource> resources = resourceRepository.findAll();
        long activeResources = resources.stream().filter(Resource::isPublic).count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalResources", resources.size());
        stats.put("activeResources", activeResources);
        stats.put("inactiveResources", Math.max(resources.size() - activeResources, 0));
        stats.put("featuredResources", resources.stream().filter(Resource::isFeatured).count());
        stats.put("totalDownloads", resources.stream().mapToInt(Resource::getDownloadCount).sum());
        stats.put("byType", resources.stream()
                .filter(resource -> resource.getType() != null && !resource.getType().isBlank())
                .collect(Collectors.groupingBy(Resource::getType, LinkedHashMap::new, Collectors.counting())));
        stats.put("byCategory", resources.stream()
                .filter(resource -> resource.getCategory() != null && !resource.getCategory().isBlank())
                .collect(Collectors.groupingBy(Resource::getCategory, LinkedHashMap::new, Collectors.counting())));
        return stats;
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
        List<Resource> resources = resourceRepository.findAll();
        String normalizedFormat = format == null ? "csv" : format.toLowerCase(Locale.ROOT);

        if ("json".equals(normalizedFormat)) {
            String json = resources.stream()
                    .map(resource -> String.format(
                            Locale.ROOT,
                            "{\"id\":%d,\"title\":\"%s\",\"type\":\"%s\",\"category\":\"%s\",\"active\":%s}",
                            resource.getId(),
                            escapeJson(resource.getTitle()),
                            escapeJson(resource.getType()),
                            escapeJson(resource.getCategory()),
                            resource.isPublic()
                    ))
                    .collect(Collectors.joining(",", "[", "]"));
            return json.getBytes(StandardCharsets.UTF_8);
        }

        StringBuilder csv = new StringBuilder();
        csv.append("id,title,type,category,isActive,featured,downloads,fileName,url\n");
        for (Resource resource : resources) {
            csv.append(resource.getId()).append(",")
                    .append(escapeCsv(resource.getTitle())).append(",")
                    .append(escapeCsv(resource.getType())).append(",")
                    .append(escapeCsv(resource.getCategory())).append(",")
                    .append(resource.isPublic()).append(",")
                    .append(resource.isFeatured()).append(",")
                    .append(resource.getDownloadCount()).append(",")
                    .append(escapeCsv(resource.getFileName())).append(",")
                    .append(escapeCsv(resource.getUrl())).append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public Resource uploadResource(FileUploadRequest request) {
        return uploadResource(request, null);
    }

    public Resource uploadResource(FileUploadRequest request, Long uploadedBy) {
        MultipartFile file = request.getFile();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String fileType = file.getContentType();
        if (!isValidFileType(fileType)) {
            throw new IllegalArgumentException("Invalid file type: " + fileType);
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("Could not create upload directory", e);
            }
        }

        String originalFilename = file.getOriginalFilename();
        String safeName = originalFilename == null ? "resource" : originalFilename;
        String extension = safeName.contains(".") ? safeName.substring(safeName.lastIndexOf(".")) : "";
        String uniqueFilename = System.currentTimeMillis() + extension;

        try {
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.write(filePath, file.getBytes());

            Resource resource = new Resource();
            resource.setTitle(request.getTitle());
            resource.setDescription(request.getDescription());
            resource.setType(request.getType());
            resource.setContentType(request.getType());
            resource.setCategory(request.getCategory());
            resource.setFeatured(request.isFeatured());
            resource.setFileName(safeName);
            resource.setFileType(fileType);
            resource.setFileSize(file.getSize());
            resource.setFileKey(uniqueFilename);
            resource.setFilePath(filePath.toString());
            resource.setUploadedBy(uploadedBy);
            resource.setIsActive(true);
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                resource.setTags(request.getTags());
            }

            prepareNewResource(resource);
            Resource saved = resourceRepository.save(resource);
            saved.setFileUrl("/api/v1/resources/download/" + saved.getId());
            return resourceRepository.save(saved);
        } catch (IOException e) {
            throw new RuntimeException("Could not save file: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<byte[]> downloadResource(Long id) {
        Resource resource = getResourceById(id);
        Path filePath = resource.getFileKey() != null
                ? Paths.get(UPLOAD_DIR, resource.getFileKey())
                : resource.getFilePath() != null ? Paths.get(resource.getFilePath()) : null;

        if (filePath == null || !Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found on server");
        }

        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            resource.setDownloadCount(resource.getDownloadCount() + 1);
            resourceRepository.save(resource);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + resource.getFileName())
                    .header("Content-Type", resource.getFileType())
                    .body(fileBytes);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + e.getMessage(), e);
        }
    }

    private void prepareNewResource(Resource resource) {
        if (resource.getCreatedAt() == null) {
            resource.setCreatedAt(LocalDateTime.now());
        }
        resource.setUpdatedAt(LocalDateTime.now());
        if (resource.getFileUrl() == null && resource.getUrl() == null) {
            resource.setFileUrl(null);
        }
        if (resource.getType() != null && resource.getContentType() == null) {
            resource.setContentType(resource.getType());
        }
        resource.setTags(normalizeTags(resource.getTags()));
        if (resource.getDownloadCount() < 0) {
            resource.setDownloadCount(0);
        }
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }

        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .toList();
    }

    private boolean isValidFileType(String fileType) {
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
            if (Objects.equals(type, fileType)) {
                return true;
            }
        }
        return false;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
