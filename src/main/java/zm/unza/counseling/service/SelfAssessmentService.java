package zm.unza.counseling.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.AssessmentSubmissionRequest;
import zm.unza.counseling.dto.response.SavedSelfAssessmentDto;
import zm.unza.counseling.entity.AssessmentResponse;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.SelfAssessment;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.AssessmentResponseRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.SelfAssessmentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SelfAssessmentService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final SelfAssessmentRepository selfAssessmentRepository;
    private final AssessmentResponseRepository assessmentResponseRepository;
    private final ClientRepository clientRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public List<SelfAssessment> getAllAssessments() {
        return selfAssessmentRepository.findAll();
    }

    public SelfAssessment createAssessment(SelfAssessment assessment) {
        if (assessment.getCreatedAt() == null) {
            assessment.setCreatedAt(LocalDateTime.now());
        }
        if (assessment.getAssessmentDate() == null) {
            assessment.setAssessmentDate(assessment.getCreatedAt());
        }
        return selfAssessmentRepository.save(assessment);
    }

    public SelfAssessment getAssessmentById(Long id) {
        return selfAssessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));
    }

    @Transactional
    public SavedSelfAssessmentDto submitAssessment(AssessmentSubmissionRequest request, User submittingUser) {
        Long userId = resolveSubmittingUserId(request, submittingUser);
        Map<String, Object> responses = resolveResponses(request);
        LocalDateTime submittedAt = request.getAssessmentDate() != null ? request.getAssessmentDate() : LocalDateTime.now();

        SelfAssessment assessment = new SelfAssessment();
        assessment.setTitle(request.getTitle() != null && !request.getTitle().isBlank()
                ? request.getTitle()
                : "Comprehensive Self-Assessment");
        assessment.setDescription(request.getDescription() != null && !request.getDescription().isBlank()
                ? request.getDescription()
                : "Submitted self-assessment");
        assessment.setQuestionsJson(writeJson(responses));
        assessment.setResponsesJson(writeJson(responses));
        assessment.setSubmittedByUserId(userId);
        assessment.setAssessmentDate(submittedAt);
        assessment.setCreatedAt(LocalDateTime.now());
        assessment.setPhq9Score(request.getPhq9Score());
        assessment.setGad7Score(request.getGad7Score());
        assessment.setPssScore(request.getPssScore());
        assessment.setSleepQuality(request.getSleepQuality());
        assessment.setOverallWellness(request.getOverallWellness());
        assessment.setAppetiteChanges(Boolean.TRUE.equals(request.getAppetiteChanges()));
        assessment.setConcentrationDifficulty(Boolean.TRUE.equals(request.getConcentrationDifficulty()));
        assessment.setSocialWithdrawal(Boolean.TRUE.equals(request.getSocialWithdrawal()));
        assessment.setSubmittedAsAnonymous(submittingUser != null && submittingUser.isAnonymous());

        Long clientIdToUse = request.getClientId() != null ? request.getClientId() : userId;
        clientRepository.findById(clientIdToUse).ifPresent(assessment::setClient);

        SelfAssessment savedAssessment = selfAssessmentRepository.save(assessment);
        persistAssessmentResponses(savedAssessment, responses);

        auditLogService.logAction(
                "SELF_ASSESSMENT_SUBMITTED",
                "SELF_ASSESSMENT",
                String.valueOf(savedAssessment.getId()),
                buildAuditDetails(savedAssessment),
                String.valueOf(userId),
                null,
                true
        );

        return toDto(savedAssessment);
    }

    public List<SavedSelfAssessmentDto> getSelfAssessmentsByUser(Long userId) {
        List<SelfAssessment> assessments = selfAssessmentRepository.findBySubmittedByUserIdOrderByAssessmentDateDesc(userId);
        if (assessments.isEmpty()) {
            assessments = selfAssessmentRepository.findByClientIdOrderByCreatedAtDesc(userId);
        }
        return assessments.stream().map(this::toDto).toList();
    }

    public SavedSelfAssessmentDto getLatestSelfAssessmentForUser(Long userId) {
        return selfAssessmentRepository.findTopBySubmittedByUserIdOrderByAssessmentDateDesc(userId)
                .or(() -> selfAssessmentRepository.findTopByClientIdOrderByCreatedAtDesc(userId))
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("No self-assessments found for user"));
    }

    public Map<String, Object> getSelfAssessmentTrend(Long userId) {
        List<SavedSelfAssessmentDto> assessments = getSelfAssessmentsByUser(userId);
        List<SavedSelfAssessmentDto> chronological = new ArrayList<>(assessments);
        Collections.reverse(chronological);

        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("assessments", assessments);
        trend.put("wellnessTrend", chronological.stream().map(dto -> dto.getOverallWellness() != null ? dto.getOverallWellness() : 0).toList());
        trend.put("stressTrend", chronological.stream().map(dto -> dto.getPssScore() != null ? dto.getPssScore() : 0).toList());
        trend.put("anxietyTrend", chronological.stream().map(dto -> dto.getGad7Score() != null ? dto.getGad7Score() : 0).toList());
        trend.put("depressionTrend", chronological.stream().map(dto -> dto.getPhq9Score() != null ? dto.getPhq9Score() : 0).toList());
        return trend;
    }

    private void persistAssessmentResponses(SelfAssessment assessment, Map<String, Object> responses) {
        if (responses.isEmpty()) {
            return;
        }

        responses.forEach((question, answer) -> {
            AssessmentResponse response = new AssessmentResponse();
            response.setAssessment(assessment);
            response.setQuestion(question);
            response.setAnswer(String.valueOf(answer));
            assessmentResponseRepository.save(response);
        });
    }

    private Long resolveSubmittingUserId(AssessmentSubmissionRequest request, User submittingUser) {
        if (submittingUser != null && submittingUser.getId() != null) {
            return submittingUser.getId();
        }
        if (request.getClientId() != null) {
            return request.getClientId();
        }
        throw new ResourceNotFoundException("Unable to resolve assessment owner");
    }

    private Map<String, Object> resolveResponses(AssessmentSubmissionRequest request) {
        Map<String, Object> responses = new LinkedHashMap<>();
        if (request.getResponses() != null && !request.getResponses().isEmpty()) {
            responses.putAll(request.getResponses());
        } else if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            responses.putAll(request.getAnswers());
        }
        return responses;
    }

    private SavedSelfAssessmentDto toDto(SelfAssessment assessment) {
        SavedSelfAssessmentDto dto = new SavedSelfAssessmentDto();
        dto.setId(assessment.getId());
        dto.setClientId(assessment.getSubmittedByUserId() != null
                ? assessment.getSubmittedByUserId()
                : assessment.getClient() != null ? assessment.getClient().getId() : null);
        dto.setAssessmentDate(assessment.getAssessmentDate() != null ? assessment.getAssessmentDate() : assessment.getCreatedAt());
        dto.setPhq9Score(assessment.getPhq9Score() != null ? assessment.getPhq9Score() : 0);
        dto.setGad7Score(assessment.getGad7Score() != null ? assessment.getGad7Score() : 0);
        dto.setPssScore(assessment.getPssScore() != null ? assessment.getPssScore() : 0);
        dto.setSleepQuality(assessment.getSleepQuality() != null ? assessment.getSleepQuality() : 5);
        dto.setOverallWellness(assessment.getOverallWellness() != null ? assessment.getOverallWellness() : 5);
        dto.setAppetiteChanges(Boolean.TRUE.equals(assessment.getAppetiteChanges()));
        dto.setConcentrationDifficulty(Boolean.TRUE.equals(assessment.getConcentrationDifficulty()));
        dto.setSocialWithdrawal(Boolean.TRUE.equals(assessment.getSocialWithdrawal()));
        dto.setFollowUpRequired(false);
        dto.setAnonymous(Boolean.TRUE.equals(assessment.getSubmittedAsAnonymous()));
        dto.setTitle(assessment.getTitle());
        dto.setDescription(assessment.getDescription());
        dto.setAssessmentType("COMPREHENSIVE");
        dto.setRecommendations(null);
        dto.setCreatedAt(assessment.getCreatedAt());
        dto.setResponses(readJsonMap(assessment.getResponsesJson() != null ? assessment.getResponsesJson() : assessment.getQuestionsJson()));
        return dto;
    }

    private String buildAuditDetails(SelfAssessment assessment) {
        return "Self-assessment recorded"
                + (Boolean.TRUE.equals(assessment.getSubmittedAsAnonymous()) ? " for anonymous user" : "")
                + " | PHQ-9=" + safeScore(assessment.getPhq9Score())
                + ", GAD-7=" + safeScore(assessment.getGad7Score())
                + ", PSS=" + safeScore(assessment.getPssScore());
    }

    private int safeScore(Integer value) {
        return value != null ? value : 0;
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload != null ? payload : Collections.emptyMap());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize self-assessment responses", e);
        }
    }

    private Map<String, Object> readJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }
}
