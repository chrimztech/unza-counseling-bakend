package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.RiskAssessmentRequest;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.RiskAssessment;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.RiskAssessmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service interface for risk assessment management
 */
public interface RiskAssessmentService {
    
    /**
     * Get all risk assessments with pagination
     * @param pageable pagination information
     * @return paginated list of risk assessments
     */
    Page<RiskAssessment> getAllRiskAssessments(Pageable pageable);

    /**
     * Get risk assessment by ID
     * @param id the risk assessment ID
     * @return the risk assessment
     */
    RiskAssessment getRiskAssessmentById(Long id);

    /**
     * Create risk assessment
     * @param request the risk assessment request
     * @return the created risk assessment
     */
    RiskAssessment createRiskAssessment(RiskAssessmentRequest request);

    /**
     * Update risk assessment
     * @param id the risk assessment ID
     * @param request the risk assessment request
     * @return the updated risk assessment
     */
    RiskAssessment updateRiskAssessment(Long id, RiskAssessmentRequest request);

    /**
     * Delete risk assessment
     * @param id the risk assessment ID
     */
    void deleteRiskAssessment(Long id);

    /**
     * Get risk assessments by client
     * @param clientId the client ID
     * @return list of risk assessments for the client
     */
    List<RiskAssessment> getRiskAssessmentsByClient(Long clientId);

    /**
     * Get assessments for a specific client
     * @param clientId the client ID
     * @return list of risk assessments for the client
     */
    List<RiskAssessment> getAssessmentsForClient(Long clientId);

    /**
     * Get high risk assessments
     * @return list of high risk assessments
     */
    List<RiskAssessment> getHighRiskAssessments();

    /**
     * Get risk assessment stats
     * @return risk assessment statistics
     */
    Object getRiskAssessmentStats();

    /**
     * Escalate risk assessment
     * @param id the risk assessment ID
     * @return the escalated risk assessment
     */
    RiskAssessment escalateRiskAssessment(Long id);

    /**
     * Export risk assessment data
     * @param format the export format
     * @return the exported risk assessment data
     */
    byte[] exportRiskAssessmentData(String format);
}
