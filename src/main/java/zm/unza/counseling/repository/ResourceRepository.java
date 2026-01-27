package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Resource;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByTitleContainingOrDescriptionContaining(String title, String description);

    @Query("SELECT DISTINCT r.category FROM Resource r WHERE r.category IS NOT NULL")
    List<String> findDistinctCategories();

    List<Resource> findByType(String type);

    List<Resource> findByCategory(String category);

    List<Resource> findByFeaturedTrue();
}