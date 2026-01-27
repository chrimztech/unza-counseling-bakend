package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.entity.Resource;
import zm.unza.counseling.service.ResourceService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {
    
    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<List<Resource>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    @PostMapping
    public ResponseEntity<Resource> createResource(@RequestBody Resource resource) {
        return ResponseEntity.ok(resourceService.createResource(resource));
    }

    @DeleteMapping("/{id}")
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
}
