package zm.unza.counseling.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.ScholarshipRequest;
import zm.unza.counseling.dto.request.ScholarshipRecommendationRequest;
import zm.unza.counseling.dto.response.ScholarshipRecommendationResponse;
import zm.unza.counseling.dto.response.ScholarshipResponse;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.Scholarship;
import zm.unza.counseling.entity.ScholarshipRecommendation;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.ScholarshipRecommendationRepository;
import zm.unza.counseling.repository.ScholarshipRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipRecommendationRepository recommendationRepository;
    private final ClientRepository clientRepository;

    public ScholarshipService(ScholarshipRepository scholarshipRepository,
                              ScholarshipRecommendationRepository recommendationRepository,
                              ClientRepository clientRepository) {
        this.scholarshipRepository = scholarshipRepository;
        this.recommendationRepository = recommendationRepository;
        this.clientRepository = clientRepository;
    }

    // ---- Scholarship CRUD ----

    public ScholarshipResponse createScholarship(ScholarshipRequest request, Long createdBy) {
        Scholarship s = new Scholarship();
        mapRequestToEntity(request, s);
        s.setCreatedBy(createdBy);
        if (s.getStatus() == null) s.setStatus(Scholarship.ScholarshipStatus.OPEN);
        return mapToScholarshipResponse(scholarshipRepository.save(s));
    }

    public ScholarshipResponse getScholarship(Long id) {
        return mapToScholarshipResponse(findScholarship(id));
    }

    public List<ScholarshipResponse> getAllScholarships() {
        return scholarshipRepository.findAll().stream()
                .map(this::mapToScholarshipResponse)
                .collect(Collectors.toList());
    }

    public List<ScholarshipResponse> getActiveScholarships() {
        return scholarshipRepository.findActiveScholarships(LocalDate.now()).stream()
                .map(this::mapToScholarshipResponse)
                .collect(Collectors.toList());
    }

    public ScholarshipResponse updateScholarship(Long id, ScholarshipRequest request) {
        Scholarship s = findScholarship(id);
        mapRequestToEntity(request, s);
        return mapToScholarshipResponse(scholarshipRepository.save(s));
    }

    public void deleteScholarship(Long id) {
        findScholarship(id);
        scholarshipRepository.deleteById(id);
    }

    // ---- Recommendation CRUD ----

    public ScholarshipRecommendationResponse createRecommendation(ScholarshipRecommendationRequest request, Long counselorId) {
        if (recommendationRepository.existsByScholarshipIdAndClientId(request.getScholarshipId(), request.getClientId())) {
            throw new IllegalStateException("This student has already been recommended for this scholarship");
        }

        Scholarship scholarship = findScholarship(request.getScholarshipId());
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        ScholarshipRecommendation rec = new ScholarshipRecommendation();
        rec.setScholarship(scholarship);
        rec.setClient(client);
        rec.setRecommendedBy(counselorId);
        mapRecommendationRequest(request, rec);

        // Auto-compute vulnerability score if not provided
        if (rec.getVulnerabilityScore() == null) {
            rec.setVulnerabilityScore(computeVulnerabilityScore(client));
        }

        return mapToRecommendationResponse(recommendationRepository.save(rec));
    }

    public ScholarshipRecommendationResponse getRecommendation(Long id) {
        return mapToRecommendationResponse(findRecommendation(id));
    }

    public List<ScholarshipRecommendationResponse> getRecommendationsByScholarship(Long scholarshipId) {
        return recommendationRepository.findByScholarshipIdOrderByVulnerabilityDesc(scholarshipId).stream()
                .map(this::mapToRecommendationResponse)
                .collect(Collectors.toList());
    }

    public List<ScholarshipRecommendationResponse> getRecommendationsByClient(Long clientId) {
        return recommendationRepository.findByClientId(clientId).stream()
                .map(this::mapToRecommendationResponse)
                .collect(Collectors.toList());
    }

    public List<ScholarshipRecommendationResponse> getAllRecommendations() {
        return recommendationRepository.findAll().stream()
                .map(this::mapToRecommendationResponse)
                .collect(Collectors.toList());
    }

    public List<ScholarshipRecommendationResponse> getRecommendationsByStatus(ScholarshipRecommendation.RecommendationStatus status) {
        return recommendationRepository.findByStatusWithDetails(status).stream()
                .map(this::mapToRecommendationResponse)
                .collect(Collectors.toList());
    }

    public ScholarshipRecommendationResponse updateRecommendationStatus(Long id, ScholarshipRecommendationRequest request, Long approvedBy) {
        ScholarshipRecommendation rec = findRecommendation(id);
        if (request.getStatus() != null) rec.setStatus(request.getStatus());
        if (request.getRejectionReason() != null) rec.setRejectionReason(request.getRejectionReason());
        if (request.getStatus() == ScholarshipRecommendation.RecommendationStatus.AWARDED) {
            rec.setAwardedDate(LocalDate.now());
            rec.setApprovedBy(approvedBy);
        }
        if (request.getStatus() == ScholarshipRecommendation.RecommendationStatus.APPROVED) {
            rec.setApprovedBy(approvedBy);
        }
        return mapToRecommendationResponse(recommendationRepository.save(rec));
    }

    public ScholarshipRecommendationResponse updateRecommendation(Long id, ScholarshipRecommendationRequest request) {
        ScholarshipRecommendation rec = findRecommendation(id);
        mapRecommendationRequest(request, rec);
        return mapToRecommendationResponse(recommendationRepository.save(rec));
    }

    public void deleteRecommendation(Long id) {
        findRecommendation(id);
        recommendationRepository.deleteById(id);
    }

    // ---- Eligible Students ----

    public List<Client> getEligibleStudents(Long scholarshipId) {
        Scholarship scholarship = findScholarship(scholarshipId);
        List<Long> alreadyRecommended = recommendationRepository.findByScholarshipId(scholarshipId)
                .stream().map(r -> r.getClient().getId()).collect(Collectors.toList());

        List<Client> allClients = clientRepository.findAll();
        return allClients.stream()
                .filter(c -> !alreadyRecommended.contains(c.getId()))
                .filter(c -> scholarship.getRequiredMinGpa() == null
                        || (c.getGpa() != null && c.getGpa() >= scholarship.getRequiredMinGpa()))
                .collect(Collectors.toList());
    }

    // ---- Private helpers ----

    private Scholarship findScholarship(Long id) {
        return scholarshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship not found with id: " + id));
    }

    private ScholarshipRecommendation findRecommendation(Long id) {
        return recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with id: " + id));
    }

    private void mapRequestToEntity(ScholarshipRequest request, Scholarship s) {
        s.setName(request.getName());
        s.setDescription(request.getDescription());
        s.setSponsor(request.getSponsor());
        s.setAmount(request.getAmount());
        s.setType(request.getType());
        s.setDeadline(request.getDeadline());
        s.setAcademicYear(request.getAcademicYear());
        s.setMaxRecipients(request.getMaxRecipients());
        s.setEligibilityCriteria(request.getEligibilityCriteria());
        s.setRequiredMinGpa(request.getRequiredMinGpa());
        if (request.getStatus() != null) s.setStatus(request.getStatus());
    }

    private void mapRecommendationRequest(ScholarshipRecommendationRequest request, ScholarshipRecommendation rec) {
        if (request.getJustification() != null) rec.setJustification(request.getJustification());
        if (request.getFinancialNeedLevel() != null) rec.setFinancialNeedLevel(request.getFinancialNeedLevel());
        if (request.getVulnerabilityScore() != null) rec.setVulnerabilityScore(request.getVulnerabilityScore());
        if (request.getAcademicStanding() != null) rec.setAcademicStanding(request.getAcademicStanding());
        if (request.getPersonalStatement() != null) rec.setPersonalStatement(request.getPersonalStatement());
        if (request.getSupportingNotes() != null) rec.setSupportingNotes(request.getSupportingNotes());
        if (request.getStatus() != null) rec.setStatus(request.getStatus());
        if (request.getRejectionReason() != null) rec.setRejectionReason(request.getRejectionReason());
    }

    private int computeVulnerabilityScore(Client client) {
        int score = 0;
        if (client.getRiskScore() != null) score += Math.min(client.getRiskScore(), 40);
        if (client.getTotalSessions() != null && client.getTotalSessions() > 3) score += 20;
        if (client.getGpa() != null && client.getGpa() < 2.5) score += 20;
        if (client.getRiskLevel() != null) {
            switch (client.getRiskLevel()) {
                case CRITICAL -> score += 20;
                case HIGH -> score += 15;
                case MODERATE -> score += 8;
                default -> {}
            }
        }
        return Math.min(score, 100);
    }

    private ScholarshipResponse mapToScholarshipResponse(Scholarship s) {
        ScholarshipResponse r = new ScholarshipResponse();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setDescription(s.getDescription());
        r.setSponsor(s.getSponsor());
        r.setAmount(s.getAmount());
        r.setType(s.getType());
        r.setDeadline(s.getDeadline());
        r.setAcademicYear(s.getAcademicYear());
        r.setMaxRecipients(s.getMaxRecipients());
        r.setEligibilityCriteria(s.getEligibilityCriteria());
        r.setRequiredMinGpa(s.getRequiredMinGpa());
        r.setStatus(s.getStatus());
        r.setCreatedBy(s.getCreatedBy());
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        r.setRecommendationCount(recommendationRepository.countByScholarshipId(s.getId()));
        r.setAwardedCount(recommendationRepository.findByScholarshipId(s.getId()).stream()
                .filter(rec -> rec.getStatus() == ScholarshipRecommendation.RecommendationStatus.AWARDED)
                .count());
        return r;
    }

    private ScholarshipRecommendationResponse mapToRecommendationResponse(ScholarshipRecommendation rec) {
        ScholarshipRecommendationResponse r = new ScholarshipRecommendationResponse();
        r.setId(rec.getId());

        Scholarship s = rec.getScholarship();
        r.setScholarshipId(s.getId());
        r.setScholarshipName(s.getName());
        r.setScholarshipSponsor(s.getSponsor());

        Client c = rec.getClient();
        r.setClientId(c.getId());
        r.setStudentId(c.getStudentId());
        r.setStudentName(c.getFirstName() + " " + c.getLastName());
        r.setStudentEmail(c.getEmail());
        r.setStudentPhone(c.getPhoneNumber());
        r.setFaculty(c.getFaculty());
        r.setProgramme(c.getProgramme());
        r.setYearOfStudy(c.getYearOfStudy());
        r.setGpa(c.getGpa());
        r.setRiskLevel(c.getRiskLevel() != null ? c.getRiskLevel().name() : null);
        r.setRiskScore(c.getRiskScore());
        r.setTotalSessions(c.getTotalSessions());

        r.setRecommendedBy(rec.getRecommendedBy());
        r.setStatus(rec.getStatus());
        r.setJustification(rec.getJustification());
        r.setFinancialNeedLevel(rec.getFinancialNeedLevel());
        r.setVulnerabilityScore(rec.getVulnerabilityScore());
        r.setAcademicStanding(rec.getAcademicStanding());
        r.setPersonalStatement(rec.getPersonalStatement());
        r.setSupportingNotes(rec.getSupportingNotes());
        r.setApprovedBy(rec.getApprovedBy());
        r.setRejectionReason(rec.getRejectionReason());
        r.setAwardedDate(rec.getAwardedDate());
        r.setCreatedAt(rec.getCreatedAt());
        r.setUpdatedAt(rec.getUpdatedAt());
        return r;
    }
}
