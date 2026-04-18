package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.FileUploadRequest;
import zm.unza.counseling.dto.request.ResourceUpdateRequest;
import zm.unza.counseling.entity.Resource;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.ResourceService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/resources", "/api/resources", "/v1/resources", "/resources"})
@RequiredArgsConstructor
public class ResourceController {
    
    private final ResourceService resourceService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Resource>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResourceById(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<Resource> createResource(@RequestBody ResourceUpdateRequest request, Principal principal) {
        setUploadedByIfAuthenticated(request, principal);
        return ResponseEntity.ok(resourceService.createResource(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<Resource> updateResource(
            @PathVariable Long id,
            @RequestBody ResourceUpdateRequest request,
            Principal principal
    ) {
        setUploadedByIfAuthenticated(request, principal);
        return ResponseEntity.ok(resourceService.updateResource(id, request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<Resource> createResourceWithFile(@ModelAttribute FileUploadRequest request, Principal principal) {
        Long uploadedBy = getAuthenticatedUserId(principal);
        return ResponseEntity.ok(resourceService.uploadResource(request, uploadedBy));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<Resource> uploadResource(@ModelAttribute FileUploadRequest request, Principal principal) {
        return ResponseEntity.ok(resourceService.uploadResource(request, getAuthenticatedUserId(principal)));
    }

    private void setUploadedByIfAuthenticated(ResourceUpdateRequest request, Principal principal) {
        if (principal != null && request.getUploadedBy() == null) {
            userRepository.findByEmail(principal.getName())
                    .map(User::getId)
                    .ifPresent(request::setUploadedBy);
        }
    }

    private Long getAuthenticatedUserId(Principal principal) {
        return principal == null ? null : userRepository.findByEmail(principal.getName())
                .map(User::getId)
                .orElse(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Resource>> searchResources(@RequestParam String query) {
        return ResponseEntity.ok(resourceService.searchResources(query));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getResourceCategories() {
        return ResponseEntity.ok(resourceService.getResourceCategories());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getResourceStatistics() {
        return ResponseEntity.ok(resourceService.getResourceStatistics());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Resource>> getResourcesByType(@PathVariable String type) {
        return ResponseEntity.ok(resourceService.getResourcesByType(type));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Resource>> getResourcesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(resourceService.getResourcesByCategory(category));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Resource>> getFeaturedResources() {
        return ResponseEntity.ok(resourceService.getFeaturedResources());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportResources(@RequestParam(defaultValue = "csv") String format) {
        byte[] data = resourceService.exportResources(format);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=resources." + format)
                .body(data);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadResource(@PathVariable Long id) {
        return resourceService.downloadResource(id);
    }
}
