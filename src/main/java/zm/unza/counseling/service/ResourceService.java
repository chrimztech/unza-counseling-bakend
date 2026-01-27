package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zm.unza.counseling.entity.Resource;
import zm.unza.counseling.repository.ResourceRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResourceService {
    
    private final ResourceRepository resourceRepository;

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    public Resource createResource(Resource resource) {
        return resourceRepository.save(resource);
    }

    public void deleteResource(Long id) {
        resourceRepository.deleteById(id);
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
}
