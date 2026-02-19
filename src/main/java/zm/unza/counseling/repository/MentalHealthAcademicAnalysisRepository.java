package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis.ImpactLevel;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis.InterventionUrgency;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis.MentalHealthStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentalHealthAcademicAnalysisRepository extends JpaRepository<MentalHealthAcademicAnalysis, Long> {

    @Query("SELECT a FROM MentalHealthAcademicAnalysis a WHERE a.interventionUrgency = :immediate OR a.impactLevel = :critical")
    List<MentalHealthAcademicAnalysis> findUrgentInterventions(
            @Param("immediate") InterventionUrgency immediate,
            @Param("critical") ImpactLevel critical);

    default List<MentalHealthAcademicAnalysis> findUrgentInterventions() {
        return findUrgentInterventions(InterventionUrgency.IMMEDIATE, ImpactLevel.CRITICAL);
    }

    @Query("SELECT a FROM MentalHealthAcademicAnalysis a WHERE a.mentalHealthStatus = :crisis OR a.mentalHealthStatus = :atRisk")
    List<MentalHealthAcademicAnalysis> findHighRiskAnalyses(
            @Param("crisis") MentalHealthStatus crisis,
            @Param("atRisk") MentalHealthStatus atRisk);

    default List<MentalHealthAcademicAnalysis> findHighRiskAnalyses() {
        return findHighRiskAnalyses(MentalHealthStatus.CRISIS, MentalHealthStatus.AT_RISK);
    }

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