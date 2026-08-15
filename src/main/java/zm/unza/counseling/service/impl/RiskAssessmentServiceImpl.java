package zm.unza.counseling.service.impl;

import zm.unza.counseling.dto.request.RiskAssessmentRequest;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.RiskAssessment;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.RiskAssessmentRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.RiskAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of RiskAssessmentService
 */
@Service
@Transactional
public class RiskAssessmentServiceImpl implements RiskAssessmentService {

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<RiskAssessment> getAllRiskAssessments(Pageable pageable) {
        return riskAssessmentRepository.findAll(pageable);
    }

    @Override
    public RiskAssessment getRiskAssessmentById(Long id) {
        return riskAssessmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Risk assessment not found with id: " + id));
    }

    private Long resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByUsername(auth.getName()))
                .map(User::getId)
                .orElse(null);
    }

    @Override
    public RiskAssessment createRiskAssessment(RiskAssessmentRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        RiskAssessment riskAssessment = new RiskAssessment();
        riskAssessment.setClient(client);
        riskAssessment.setRiskScore(request.getRiskScore());
        riskAssessment.setRiskLevel(request.getRiskLevel());
        riskAssessment.setNotes(request.getNotes());
        riskAssessment.setAssessmentDate(LocalDateTime.now());
        riskAssessment.setAssessorId(resolveCurrentUserId());
        return riskAssessmentRepository.save(riskAssessment);
    }

    @Override
    public RiskAssessment updateRiskAssessment(Long id, RiskAssessmentRequest request) {
        RiskAssessment existing = getRiskAssessmentById(id);
        if (request.getClientId() != null) {
            Client client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));
            existing.setClient(client);
        }
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

    public RiskAssessment getLatestRiskAssessmentForClient(Long clientId) {
        return riskAssessmentRepository.findTopByClientIdOrderByAssessmentDateDesc(clientId)
                .orElseThrow(() -> new RuntimeException("No risk assessments found for client"));
    }

    public Object getRiskAssessmentTrend(Long clientId) {
        return riskAssessmentRepository.findByClientIdOrderByAssessmentDateDesc(clientId);
    }

    public Object getRiskAssessmentSummary() {
        return null;
    }

    public List<RiskAssessment> getAssessmentsRequiringFollowUp() {
        return riskAssessmentRepository.findByFollowUpRequiredTrue();
    }

    public Object getRiskAssessmentsByAssessor(Long assessorId) {
        return riskAssessmentRepository.findByAssessorIdOrderByAssessmentDateDesc(assessorId);
    }

    public Object getRiskAssessmentAnalytics() {
        return null;
    }
}