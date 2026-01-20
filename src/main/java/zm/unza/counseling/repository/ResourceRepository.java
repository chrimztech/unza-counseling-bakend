package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Resource;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
}