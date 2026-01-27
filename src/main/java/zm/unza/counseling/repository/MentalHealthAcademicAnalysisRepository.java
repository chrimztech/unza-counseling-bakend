package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentalHealthAcademicAnalysisRepository extends JpaRepository<MentalHealthAcademicAnalysis, Long> {

    @Query("SELECT a FROM MentalHealthAcademicAnalysis a WHERE a.interventionUrgency = 'IMMEDIATE' OR a.impactLevel = 'CRITICAL'")
    List<MentalHealthAcademicAnalysis> findUrgentInterventions();

    @Query("SELECT a FROM MentalHealthAcademicAnalysis a WHERE a.mentalHealthStatus = 'CRISIS' OR a.mentalHealthStatus = 'AT_RISK'")
    List<MentalHealthAcademicAnalysis> findHighRiskAnalyses();

    long countByInterventionNeeded(boolean needed);
    long countByCounselingRecommended(boolean recommended);
    long countByAcademicSupportRecommended(boolean recommended);
    long countByPeerSupportRecommended(boolean recommended);
    long countByLifestyleChangesRecommended(boolean recommended);
    long countByReferralRecommended(boolean recommended);

    List<MentalHealthAcademicAnalysis> findByClientId(Long clientId);

    Optional<MentalHealthAcademicAnalysis> findTopByClientIdOrderByCreatedAtDesc(Long clientId);

    List<MentalHealthAcademicAnalysis> findByClientIdOrderByCreatedAtDesc(Long clientId);
}