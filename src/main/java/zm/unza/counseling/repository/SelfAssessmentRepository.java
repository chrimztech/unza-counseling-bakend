package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.SelfAssessment;

import java.util.List;
import java.util.Optional;

@Repository
public interface SelfAssessmentRepository extends JpaRepository<SelfAssessment, Long> {
    List<SelfAssessment> findByClientId(Long clientId);
    Optional<SelfAssessment> findTopByClientIdOrderByCreatedAtDesc(Long clientId);
    List<SelfAssessment> findByClientIdOrderByCreatedAtDesc(Long clientId);
}