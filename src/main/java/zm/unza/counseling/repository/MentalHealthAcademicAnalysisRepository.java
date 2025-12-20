package zm.unza.counseling.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis.TrendDirection;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentalHealthAcademicAnalysisRepository extends JpaRepository<MentalHealthAcademicAnalysis, Long> {

    List<MentalHealthAcademicAnalysis> findByClientIdOrderByAnalysisDateDesc(Long clientId);

    Page<MentalHealthAcademicAnalysis> findByClientId(Long clientId, Pageable pageable);

    Optional<MentalHealthAcademicAnalysis> findTopByClientIdOrderByAnalysisDateDesc(Long clientId);

    List<MentalHealthAcademicAnalysis> findByTrendDirection(TrendDirection trendDirection);

    @Query("SELECT m FROM MentalHealthAcademicAnalysis m WHERE m.mentalHealthStatus IN ('AT_RISK', 'CRISIS') OR m.impactLevel = 'SEVERE' ORDER BY m.analysisDate DESC")
    List<MentalHealthAcademicAnalysis> findHighRiskAnalyses();

    @Query("SELECT m FROM MentalHealthAcademicAnalysis m WHERE m.interventionUrgency IN ('IMMEDIATE', 'HIGH') ORDER BY m.analysisDate DESC")
    List<MentalHealthAcademicAnalysis> findUrgentInterventions();

    @Query("SELECT m.analysisDate, m.overallMentalHealthScore, m.currentGpa FROM MentalHealthAcademicAnalysis m WHERE m.client.id = :clientId ORDER BY m.analysisDate ASC")
    List<Object[]> findTrendDataByClient(@Param("clientId") Long clientId);

    @Query("SELECT m.mentalHealthStatus, COUNT(m) FROM MentalHealthAcademicAnalysis m GROUP BY m.mentalHealthStatus")
    List<Object[]> countByMentalHealthStatus();

    @Query("SELECT m.impactLevel, COUNT(m) FROM MentalHealthAcademicAnalysis m GROUP BY m.impactLevel")
    List<Object[]> countByImpactLevel();

    @Query("SELECT m.overallMentalHealthScore, m.currentGpa FROM MentalHealthAcademicAnalysis m WHERE m.overallMentalHealthScore IS NOT NULL AND m.currentGpa IS NOT NULL")
    List<Object[]> findMentalHealthGpaCorrelationData();
}