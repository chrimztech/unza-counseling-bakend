package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.AssessmentResponse;
import java.util.List;

@Repository
public interface AssessmentResponseRepository extends JpaRepository<AssessmentResponse, String> {
    List<AssessmentResponse> findByAssessmentId(Long assessmentId);
}
