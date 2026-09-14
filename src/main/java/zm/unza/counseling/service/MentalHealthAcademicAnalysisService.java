package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.MentalHealthAcademicDtos.MentalHealthAcademicAnalysisRequest;
import zm.unza.counseling.dto.MentalHealthAcademicDtos.MentalHealthAcademicAnalysisResponse;
import zm.unza.counseling.entity.AcademicPerformance;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis;
import zm.unza.counseling.entity.RiskAssessment;
import zm.unza.counseling.entity.SelfAssessment;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.mapper.MentalHealthAcademicMapper;
import zm.unza.counseling.repository.AcademicPerformanceRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.MentalHealthAcademicAnalysisRepository;
import zm.unza.counseling.repository.RiskAssessmentRepository;
import zm.unza.counseling.repository.SelfAssessmentRepository;
import zm.unza.counseling.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MentalHealthAcademicAnalysisService {

    private final MentalHealthAcademicAnalysisRepository repository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final AcademicPerformanceRepository academicPerformanceRepository;
    private final SelfAssessmentRepository selfAssessmentRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final MentalHealthAcademicMapper mapper;

    // Every read method below is @Transactional(readOnly = true) because
    // MentalHealthAcademicAnalysis has several FetchType.LAZY associations
    // (analyzedBy, academicPerformance, selfAssessment, riskAssessment) that
    // mapper.toResponse() dereferences (e.g. analyzedBy.getFirstName()) — with
    // open-in-view disabled, doing that after the repository call's own
    // transaction has already closed throws LazyInitializationException (500).

    @Transactional(readOnly = true)
    public List<MentalHealthAcademicAnalysisResponse> getHighRiskAnalyses() {
        return repository.findHighRiskAnalyses().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MentalHealthAcademicAnalysisResponse> getUrgentInterventions() {
        return repository.findUrgentInterventions().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MentalHealthAcademicAnalysisResponse> getAnalysesByClient(Long clientId) {
        return repository.findByClientId(clientId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MentalHealthAcademicAnalysisResponse getLatestAnalysisForClient(Long clientId) {
        return repository.findTopByClientIdOrderByCreatedAtDesc(clientId)
                .map(mapper::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<MentalHealthAcademicAnalysisResponse> getAnalysisTrendForClient(Long clientId) {
        return repository.findByClientIdOrderByCreatedAtDesc(clientId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Object getDashboardStats() {
        // Return dashboard statistics
        long totalAnalyses = repository.count();
        long highRiskCount = repository.findHighRiskAnalyses().size();
        long urgentCount = repository.findUrgentInterventions().size();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalAnalyses", totalAnalyses);
        stats.put("highRiskCount", highRiskCount);
        stats.put("urgentCount", urgentCount);
        stats.put("recentAnalyses", repository.findAll().stream().map(mapper::toResponse).toList());
        return stats;
    }

    @Transactional(readOnly = true)
    public Object getInterventionReport() {
        // Return intervention report
        java.util.Map<String, Object> report = new java.util.HashMap<>();
        report.put("highRiskAnalyses", repository.findHighRiskAnalyses().stream().map(mapper::toResponse).toList());
        report.put("urgentInterventions", repository.findUrgentInterventions().stream().map(mapper::toResponse).toList());
        report.put("generatedAt", java.time.LocalDateTime.now());
        return report;
    }

    @Transactional
    public MentalHealthAcademicAnalysisResponse createAnalysis(MentalHealthAcademicAnalysisRequest request) {
        return mapper.toResponse(createAnalysisEntity(request));
    }

    private MentalHealthAcademicAnalysis createAnalysisEntity(MentalHealthAcademicAnalysisRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + request.getClientId()));
        
        MentalHealthAcademicAnalysis analysis = new MentalHealthAcademicAnalysis();
        analysis.setClient(client);
        analysis.setAnalysisDate(request.getAnalysisDate() != null ? request.getAnalysisDate() : java.time.LocalDate.now());
        analysis.setAnalysisPeriodStart(request.getAnalysisPeriodStart());
        analysis.setAnalysisPeriodEnd(request.getAnalysisPeriodEnd());
        
        // Mental Health Indicators
        analysis.setDepressionScore(request.getDepressionScore());
        analysis.setAnxietyScore(request.getAnxietyScore());
        analysis.setStressScore(request.getStressScore());
        analysis.setOverallMentalHealthScore(request.getOverallMentalHealthScore());
        analysis.setMentalHealthStatus(request.getMentalHealthStatus());
        
        // Academic Performance Indicators
        analysis.setCurrentGpa(request.getCurrentGpa());
        analysis.setGpaChange(request.getGpaChange());
        analysis.setAttendanceRate(request.getAttendanceRate());
        analysis.setAttendanceChange(request.getAttendanceChange());
        
        // Risk Factors
        analysis.setConcentrationIssues(request.getConcentrationIssues());
        analysis.setMotivationIssues(request.getMotivationIssues());
        analysis.setSleepIssues(request.getSleepIssues());
        analysis.setSocialIsolation(request.getSocialIsolation());
        analysis.setFinancialStress(request.getFinancialStress());
        analysis.setFamilyIssues(request.getFamilyIssues());
        analysis.setSubstanceUseConcern(request.getSubstanceUseConcern());
        
        // Intervention Recommendations
        analysis.setCounselingRecommended(request.getCounselingRecommended());
        analysis.setAcademicSupportRecommended(request.getAcademicSupportRecommended());
        analysis.setPeerSupportRecommended(request.getPeerSupportRecommended());
        analysis.setLifestyleChangesRecommended(request.getLifestyleChangesRecommended());
        analysis.setReferralRecommended(request.getReferralRecommended());
        analysis.setInterventionUrgency(request.getInterventionUrgency());
        
        // Analysis Notes
        analysis.setAnalysisSummary(request.getAnalysisSummary());
        analysis.setRecommendations(request.getRecommendations());
        analysis.setCounselorNotes(request.getCounselorNotes());
        
        // Set analyzed by user
        if (request.getAnalyzedBy() != null) {
            User analyzedBy = userRepository.findById(request.getAnalyzedBy()).orElse(null);
            analysis.setAnalyzedBy(analyzedBy);
        }
        
        // Link related entities
        if (request.getAcademicPerformanceId() != null) {
            AcademicPerformance academicPerformance = academicPerformanceRepository.findById(request.getAcademicPerformanceId()).orElse(null);
            analysis.setAcademicPerformance(academicPerformance);
        }
        
        if (request.getSelfAssessmentId() != null) {
            try {
                Long selfAssmtId = Long.parseLong(request.getSelfAssessmentId());
                SelfAssessment selfAssessment = selfAssessmentRepository.findById(selfAssmtId).orElse(null);
                analysis.setSelfAssessment(selfAssessment);
            } catch (NumberFormatException e) {
                // Ignore invalid self assessment ID
            }
        }
        
        if (request.getRiskAssessmentId() != null) {
            RiskAssessment riskAssessment = riskAssessmentRepository.findById(request.getRiskAssessmentId()).orElse(null);
            analysis.setRiskAssessment(riskAssessment);
        }
        
        // AI metadata
        analysis.setIsAiGenerated(request.getIsAiGenerated() != null ? request.getIsAiGenerated() : false);
        analysis.setAiConfidenceScore(request.getAiConfidenceScore());
        
        return repository.save(analysis);
    }

    @Transactional
    public MentalHealthAcademicAnalysisResponse updateAnalysis(Long id, MentalHealthAcademicAnalysisRequest request) {
        return mapper.toResponse(updateAnalysisEntity(id, request));
    }

    private MentalHealthAcademicAnalysis updateAnalysisEntity(Long id, MentalHealthAcademicAnalysisRequest request) {
        MentalHealthAcademicAnalysis analysis = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + id));
        
        // Update all fields
        if (request.getAnalysisDate() != null) analysis.setAnalysisDate(request.getAnalysisDate());
        if (request.getAnalysisPeriodStart() != null) analysis.setAnalysisPeriodStart(request.getAnalysisPeriodStart());
        if (request.getAnalysisPeriodEnd() != null) analysis.setAnalysisPeriodEnd(request.getAnalysisPeriodEnd());
        
        // Mental Health Indicators
        if (request.getDepressionScore() != null) analysis.setDepressionScore(request.getDepressionScore());
        if (request.getAnxietyScore() != null) analysis.setAnxietyScore(request.getAnxietyScore());
        if (request.getStressScore() != null) analysis.setStressScore(request.getStressScore());
        if (request.getOverallMentalHealthScore() != null) analysis.setOverallMentalHealthScore(request.getOverallMentalHealthScore());
        if (request.getMentalHealthStatus() != null) analysis.setMentalHealthStatus(request.getMentalHealthStatus());
        
        // Academic Performance Indicators
        if (request.getCurrentGpa() != null) analysis.setCurrentGpa(request.getCurrentGpa());
        if (request.getGpaChange() != null) analysis.setGpaChange(request.getGpaChange());
        if (request.getAttendanceRate() != null) analysis.setAttendanceRate(request.getAttendanceRate());
        if (request.getAttendanceChange() != null) analysis.setAttendanceChange(request.getAttendanceChange());
        
        // Risk Factors
        if (request.getConcentrationIssues() != null) analysis.setConcentrationIssues(request.getConcentrationIssues());
        if (request.getMotivationIssues() != null) analysis.setMotivationIssues(request.getMotivationIssues());
        if (request.getSleepIssues() != null) analysis.setSleepIssues(request.getSleepIssues());
        if (request.getSocialIsolation() != null) analysis.setSocialIsolation(request.getSocialIsolation());
        if (request.getFinancialStress() != null) analysis.setFinancialStress(request.getFinancialStress());
        if (request.getFamilyIssues() != null) analysis.setFamilyIssues(request.getFamilyIssues());
        if (request.getSubstanceUseConcern() != null) analysis.setSubstanceUseConcern(request.getSubstanceUseConcern());
        
        // Intervention Recommendations
        if (request.getCounselingRecommended() != null) analysis.setCounselingRecommended(request.getCounselingRecommended());
        if (request.getAcademicSupportRecommended() != null) analysis.setAcademicSupportRecommended(request.getAcademicSupportRecommended());
        if (request.getPeerSupportRecommended() != null) analysis.setPeerSupportRecommended(request.getPeerSupportRecommended());
        if (request.getLifestyleChangesRecommended() != null) analysis.setLifestyleChangesRecommended(request.getLifestyleChangesRecommended());
        if (request.getReferralRecommended() != null) analysis.setReferralRecommended(request.getReferralRecommended());
        if (request.getInterventionUrgency() != null) analysis.setInterventionUrgency(request.getInterventionUrgency());
        
        // Analysis Notes
        if (request.getAnalysisSummary() != null) analysis.setAnalysisSummary(request.getAnalysisSummary());
        if (request.getRecommendations() != null) analysis.setRecommendations(request.getRecommendations());
        if (request.getCounselorNotes() != null) analysis.setCounselorNotes(request.getCounselorNotes());
        
        return repository.save(analysis);
    }

    @Transactional(readOnly = true)
    public MentalHealthAcademicAnalysisResponse getAnalysisById(Long id) {
        return repository.findById(id).map(mapper::toResponse).orElse(null);
    }

    // Built from real, counselor-entered data (correlationScore/correlationStrength/
    // impactLevel on each analysis record) — not a fabricated "AI" result. The frontend's
    // CorrelationAnalysisResult type uses a smaller, unsigned strength/impact vocabulary
    // than the backend enums, so those are mapped down rather than passed through raw.
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getCorrelationAnalysis(Long clientId) {
        List<MentalHealthAcademicAnalysis> history = repository.findByClientIdOrderByCreatedAtDesc(clientId);
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();

        if (history.isEmpty()) {
            result.put("correlationScore", 0);
            result.put("strength", "NONE");
            result.put("impactLevel", "NONE");
            result.put("interpretation", "Not enough analysis history yet to compute a correlation.");
            result.put("dataPoints", List.of());
            return result;
        }

        MentalHealthAcademicAnalysis latest = history.get(0);
        String strength = mapCorrelationStrength(latest.getCorrelationStrength());
        String impact = mapImpactLevel(latest.getImpactLevel());

        result.put("correlationScore", latest.getCorrelationScore());
        result.put("strength", strength);
        result.put("impactLevel", impact);
        result.put("interpretation", buildCorrelationInterpretation(latest, strength));

        List<MentalHealthAcademicAnalysis> chronological = new java.util.ArrayList<>(history);
        java.util.Collections.reverse(chronological);
        List<java.util.Map<String, Object>> dataPoints = chronological.stream()
                .map(a -> {
                    java.util.Map<String, Object> point = new java.util.LinkedHashMap<>();
                    point.put("date", a.getAnalysisDate() != null ? a.getAnalysisDate().toString() : null);
                    point.put("label", a.getAnalysisDate() != null ? a.getAnalysisDate().toString() : null);
                    point.put("value", a.getCorrelationScore());
                    point.put("x", a.getOverallMentalHealthScore());
                    point.put("y", a.getCurrentGpa());
                    return point;
                })
                .toList();
        result.put("dataPoints", dataPoints);

        return result;
    }

    private String mapCorrelationStrength(MentalHealthAcademicAnalysis.CorrelationStrength strength) {
        if (strength == null) return "NONE";
        return switch (strength) {
            case NONE -> "NONE";
            case WEAK_POSITIVE, WEAK_NEGATIVE -> "WEAK";
            case MODERATE_POSITIVE, MODERATE_NEGATIVE -> "MODERATE";
            case STRONG_POSITIVE, STRONG_NEGATIVE -> "STRONG";
        };
    }

    private String mapImpactLevel(MentalHealthAcademicAnalysis.ImpactLevel impactLevel) {
        if (impactLevel == null) return "NONE";
        return switch (impactLevel) {
            case NONE -> "NONE";
            case MILD -> "MINIMAL";
            case MODERATE -> "MODERATE";
            case SEVERE -> "SIGNIFICANT";
            case CRITICAL -> "SEVERE";
        };
    }

    private String buildCorrelationInterpretation(MentalHealthAcademicAnalysis latest, String strength) {
        boolean negative = latest.getCorrelationStrength() != null
                && latest.getCorrelationStrength().name().endsWith("_NEGATIVE");
        String direction = negative
                ? "worsening mental health indicators tend to coincide with lower academic performance"
                : "mental health and academic performance are moving together";
        return switch (strength) {
            case "STRONG" -> "Strong correlation detected — " + direction + ".";
            case "MODERATE" -> "Moderate correlation detected — " + direction + ".";
            case "WEAK" -> "A weak correlation was detected between mental health and academic performance.";
            default -> "No meaningful correlation detected between mental health and academic performance.";
        };
    }

    @Transactional
    public void deleteAnalysis(Long id) {
        MentalHealthAcademicAnalysis analysis = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + id));
        repository.delete(analysis);
    }

}