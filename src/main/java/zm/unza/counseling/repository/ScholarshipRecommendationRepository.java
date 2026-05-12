package zm.unza.counseling.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.ScholarshipRecommendation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScholarshipRecommendationRepository extends JpaRepository<ScholarshipRecommendation, Long> {

    List<ScholarshipRecommendation> findByScholarshipId(Long scholarshipId);

    Page<ScholarshipRecommendation> findByScholarshipId(Long scholarshipId, Pageable pageable);

    List<ScholarshipRecommendation> findByClientId(Long clientId);

    List<ScholarshipRecommendation> findByRecommendedBy(Long counselorId);

    List<ScholarshipRecommendation> findByStatus(ScholarshipRecommendation.RecommendationStatus status);

    Optional<ScholarshipRecommendation> findByScholarshipIdAndClientId(Long scholarshipId, Long clientId);

    boolean existsByScholarshipIdAndClientId(Long scholarshipId, Long clientId);

    long countByStatus(ScholarshipRecommendation.RecommendationStatus status);

    long countByScholarshipId(Long scholarshipId);

    @Query("SELECT r FROM ScholarshipRecommendation r WHERE r.scholarship.id = :scholarshipId ORDER BY r.vulnerabilityScore DESC, r.createdAt ASC")
    List<ScholarshipRecommendation> findByScholarshipIdOrderByVulnerabilityDesc(@Param("scholarshipId") Long scholarshipId);

    @Query("SELECT r FROM ScholarshipRecommendation r JOIN FETCH r.client JOIN FETCH r.scholarship WHERE r.status = :status")
    List<ScholarshipRecommendation> findByStatusWithDetails(@Param("status") ScholarshipRecommendation.RecommendationStatus status);
}
