package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.SelfAssessment;

import java.util.List;
import java.util.Optional;

@Repository
public interface SelfAssessmentRepository extends JpaRepository<SelfAssessment, Long> {
    @Query("SELECT s FROM SelfAssessment s WHERE s.submittedByUserId = :clientId")
    List<SelfAssessment> findByClientId(@Param("clientId") Long clientId);

    @Query("SELECT s FROM SelfAssessment s WHERE s.submittedByUserId = :clientId ORDER BY s.createdAt DESC")
    Optional<SelfAssessment> findTopByClientIdOrderByCreatedAtDesc(@Param("clientId") Long clientId);

    @Query("SELECT s FROM SelfAssessment s WHERE s.submittedByUserId = :clientId ORDER BY s.createdAt DESC")
    List<SelfAssessment> findByClientIdOrderByCreatedAtDesc(@Param("clientId") Long clientId);

    List<SelfAssessment> findBySubmittedByUserIdOrderByAssessmentDateDesc(Long submittedByUserId);
    Optional<SelfAssessment> findTopBySubmittedByUserIdOrderByAssessmentDateDesc(Long submittedByUserId);
}
