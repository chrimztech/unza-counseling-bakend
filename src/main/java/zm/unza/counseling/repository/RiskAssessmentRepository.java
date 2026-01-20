package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.RiskAssessment;

import java.util.List;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    List<RiskAssessment> findByClientIdOrderByAssessmentDateDesc(Long clientId);
    
    List<RiskAssessment> findByRiskLevel(String riskLevel);
}