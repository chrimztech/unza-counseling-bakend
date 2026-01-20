package zm.unza.counseling.service.impl;

import zm.unza.counseling.dto.request.RiskAssessmentRequest;
import zm.unza.counseling.entity.RiskAssessment;
import zm.unza.counseling.repository.RiskAssessmentRepository;
import zm.unza.counseling.service.RiskAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of RiskAssessmentService
 */
@Service
@Transactional
public class RiskAssessmentServiceImpl implements RiskAssessmentService {

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Override
    public Page<RiskAssessment> getAllRiskAssessments(Pageable pageable) {
        return riskAssessmentRepository.findAll(pageable);
    }

    @Override
    public RiskAssessment getRiskAssessmentById(Long id) {
        return riskAssessmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Risk assessment not found with id: " + id));
    }

    @Override
    public RiskAssessment createRiskAssessment(RiskAssessmentRequest request) {
        RiskAssessment riskAssessment = new RiskAssessment();
        riskAssessment.setRiskScore(request.getRiskScore());
        riskAssessment.setRiskLevel(request.getRiskLevel());
        riskAssessment.setNotes(request.getNotes());
        return riskAssessmentRepository.save(riskAssessment);
    }

    @Override
    public RiskAssessment updateRiskAssessment(Long id, RiskAssessmentRequest request) {
        RiskAssessment existing = getRiskAssessmentById(id);
        existing.setRiskScore(request.getRiskScore());
        existing.setRiskLevel(request.getRiskLevel());
        existing.setNotes(request.getNotes());
        return riskAssessmentRepository.save(existing);
    }

    @Override
    public void deleteRiskAssessment(Long id) {
        RiskAssessment riskAssessment = getRiskAssessmentById(id);
        riskAssessmentRepository.delete(riskAssessment);
    }

    @Override
    public List<RiskAssessment> getRiskAssessmentsByClient(Long clientId) {
        return riskAssessmentRepository.findByClientIdOrderByAssessmentDateDesc(clientId);
    }

    @Override
    public List<RiskAssessment> getAssessmentsForClient(Long clientId) {
        return riskAssessmentRepository.findByClientIdOrderByAssessmentDateDesc(clientId);
    }

    @Override
    public List<RiskAssessment> getHighRiskAssessments() {
        return riskAssessmentRepository.findByRiskLevel("HIGH");
    }

    @Override
    public Object getRiskAssessmentStats() {
        // Implementation would return statistics about risk assessments
        return null;
    }

    @Override
    public RiskAssessment escalateRiskAssessment(Long id) {
        RiskAssessment riskAssessment = getRiskAssessmentById(id);
        // Implementation would escalate the risk assessment
        return riskAssessment;
    }

    @Override
    public byte[] exportRiskAssessmentData(String format) {
        // Implementation would export risk assessment data
        return new byte[0];
    }
}