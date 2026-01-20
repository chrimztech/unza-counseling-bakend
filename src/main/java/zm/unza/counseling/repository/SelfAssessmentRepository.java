package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.SelfAssessment;

@Repository
public interface SelfAssessmentRepository extends JpaRepository<SelfAssessment, Long> {
}