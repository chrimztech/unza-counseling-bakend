package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.AiUserMemory;

@Repository
public interface AiUserMemoryRepository extends JpaRepository<AiUserMemory, Long> {
}
