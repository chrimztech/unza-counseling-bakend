package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.AiCategoryGuidance;

import java.util.List;

@Repository
public interface AiCategoryGuidanceRepository extends JpaRepository<AiCategoryGuidance, Long> {
    List<AiCategoryGuidance> findAllByOrderBySortOrderAscIdAsc();
}
