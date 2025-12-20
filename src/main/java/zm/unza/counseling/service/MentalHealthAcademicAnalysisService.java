package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.MentalHealthAcademicDtos.*;
import zm.unza.counseling.entity.*;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis.*;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.mapper.MentalHealthAcademicMapper;
import zm.unza.counseling.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MentalHealthAcademicAnalysisService {

    private final Logger log = LoggerFactory.getLogger(MentalHealthAcademicAnalysisService.class);

    private final MentalHealthAcademicAnalysisRepository analysisRepository;
    private final ClientRepository clientRepository;
    private final AcademicPerformanceRepository academicPerformanceRepository;
    private final SelfAssessmentRepository selfAssessmentRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final CounselorRepository counselorRepository;
    private final MentalHealthAcademicMapper mapper;

    // Create new analysis
    public MentalHealthAcademicAnalysisResponse createAnalysis(MentalHealthAcademicAnalysisRequest request, Long counselorId) {
        log.info("Creating mental health academic analysis for client: {}", request.getClientId());

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        Counselor counselor = null;
        if (counselorId != null) {
            counselor = counselorRepository.findById(counselorId).orElse(null);
        }

        AcademicPerformance academicPerformance = null;
        if (request.getAcademicPerformanceId() != null) {
            academicPerformance = academicPerformanceRepository.findById(request.getAcademicPerformanceId()).orElse(null);
        } else {
            // Get latest academic performance
            academicPerformance = academicPerformanceRepository.findTopByClientIdOrderByRecordDateDesc(request.getClientId()).orElse(null);
        }

        SelfAssessment selfAssessment = null;
        if (request.getSelfAssessmentId() != null) {
            selfAssessment = selfAssessmentRepository.findById(request.getSelfAssessmentId()).orElse(null);
        }

        RiskAssessment riskAssessment = null;
        if (request.getRiskAssessmentId() != null) {
            riskAssessment = riskAssessmentRepository.findById(request.getRiskAssessmentId()).orElse(null);
        }

        // Calculate scores and determine status
        BigDecimal overallMentalHealth = calculateOverallMentalHealthScore(
                request.getDepressionScore(), request.getAnxietyScore(), request.getStressScore());
        
        MentalHealthStatus mentalHealthStatus = determineMentalHealthStatus(overallMentalHealth, request.getMentalHealthStatus() != null ? request.getMentalHealthStatus().name() : null);
        
        BigDecimal currentGpa = academicPerformance != null ? academicPerformance.getGpa() : null;
        BigDecimal gpaChange = calculateGpaChange(client.getId(), currentGpa);
        BigDecimal attendanceRate = academicPerformance != null ? academicPerformance.getAttendanceRate() : null;
        
        // Calculate correlation
        BigDecimal correlationScore = calculateCorrelationScore(client.getId());
        CorrelationStrength correlationStrength = determineCorrelationStrength(correlationScore);
        
        ImpactLevel impactLevel = determineImpactLevel(mentalHealthStatus, gpaChange, request);
        TrendDirection trendDirection = determineTrendDirection(client.getId());
        InterventionUrgency interventionUrgency = determineInterventionUrgency(
                request.getInterventionUrgency() != null ? request.getInterventionUrgency().name() : null, impactLevel, mentalHealthStatus);

        MentalHealthAcademicAnalysis analysis = MentalHealthAcademicAnalysis.builder()
                .client(client)
                .academicPerformance(academicPerformance)
                .selfAssessment(selfAssessment)
                .riskAssessment(riskAssessment)
                .analysisDate(LocalDate.now())
                .analysisPeriodStart(request.getAnalysisPeriodStart())
                .analysisPeriodEnd(request.getAnalysisPeriodEnd())
                .depressionScore(request.getDepressionScore())
                .anxietyScore(request.getAnxietyScore())
                .stressScore(request.getStressScore())
                .overallMentalHealthScore(overallMentalHealth)
                .mentalHealthStatus(mentalHealthStatus)
                .currentGpa(currentGpa)
                .gpaChange(gpaChange)
                .attendanceRate(attendanceRate)
                .correlationScore(correlationScore)
                .correlationStrength(correlationStrength)
                .impactLevel(impactLevel)
                .trendDirection(trendDirection)
                .concentrationIssues(request.getConcentrationIssues())
                .motivationIssues(request.getMotivationIssues())
                .sleepIssues(request.getSleepIssues())
                .socialIsolation(request.getSocialIsolation())
                .financialStress(request.getFinancialStress())
                .familyIssues(request.getFamilyIssues())
                .substanceUseConcern(request.getSubstanceUseConcern())
                .counselingRecommended(request.getCounselingRecommended())
                .academicSupportRecommended(request.getAcademicSupportRecommended())
                .peerSupportRecommended(request.getPeerSupportRecommended())
                .lifestyleChangesRecommended(request.getLifestyleChangesRecommended())
                .referralRecommended(request.getReferralRecommended())
                .interventionUrgency(interventionUrgency)
                .analysisSummary(request.getAnalysisSummary())
                .recommendations(request.getRecommendations())
                .counselorNotes(request.getCounselorNotes())
                .analyzedBy(counselor)
                .isAiGenerated(false)
                .build();

        MentalHealthAcademicAnalysis saved = analysisRepository.save(analysis);
        return mapper.toResponse(saved);
    }

    // Generate AI-powered analysis
    public AiAnalysisResponse generateAiAnalysis(AiAnalysisRequest request) {
        log.info("Generating AI analysis for client: {}", request.getClientId());
        
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        // Gather data
        List<AcademicPerformance> academicHistory = academicPerformanceRepository
                .findByClientIdOrderByRecordDateDesc(request.getClientId());
        
        List<MentalHealthAcademicAnalysis> previousAnalyses = analysisRepository
                .findByClientIdOrderByAnalysisDateDesc(request.getClientId());

        // Perform analysis
        AcademicPerformance latestAcademic = academicHistory.isEmpty() ? null : academicHistory.get(0);
        
        // Calculate mental health indicators based on patterns
        BigDecimal depressionScore = estimateDepressionScore(previousAnalyses, academicHistory);
        BigDecimal anxietyScore = estimateAnxietyScore(previousAnalyses, academicHistory);
        BigDecimal stressScore = estimateStressScore(previousAnalyses, academicHistory);
        BigDecimal overallScore = calculateOverallMentalHealthScore(depressionScore, anxietyScore, stressScore);
        
        MentalHealthStatus status = determineMentalHealthStatus(overallScore, null);
        BigDecimal correlationScore = calculateCorrelationScore(request.getClientId());
        CorrelationStrength correlationStrength = determineCorrelationStrength(correlationScore);
        
        // Identify risk factors
        RiskFactors riskFactors = identifyRiskFactors(academicHistory, previousAnalyses);
        
        // Generate recommendations
        Recommendations recommendations = generateRecommendations(status, riskFactors, latestAcademic);
        
        ImpactLevel impactLevel = determineImpactLevelFromFactors(riskFactors, status);
        TrendDirection trend = determineTrendDirection(request.getClientId());
        InterventionUrgency urgency = recommendations.getInterventionUrgency() != null ? recommendations.getInterventionUrgency() : InterventionUrgency.LOW;

        // Create analysis entity
        MentalHealthAcademicAnalysis analysis = MentalHealthAcademicAnalysis.builder()
                .client(client)
                .academicPerformance(latestAcademic)
                .analysisDate(LocalDate.now())
                .analysisPeriodStart(request.getAnalysisPeriodStart())
                .analysisPeriodEnd(request.getAnalysisPeriodEnd())
                .depressionScore(depressionScore)
                .anxietyScore(anxietyScore)
                .stressScore(stressScore)
                .overallMentalHealthScore(overallScore)
                .mentalHealthStatus(status)
                .currentGpa(latestAcademic != null ? latestAcademic.getGpa() : null)
                .gpaChange(calculateGpaChange(request.getClientId(), latestAcademic != null ? latestAcademic.getGpa() : null))
                .attendanceRate(latestAcademic != null ? latestAcademic.getAttendanceRate() : null)
                .correlationScore(correlationScore)
                .correlationStrength(correlationStrength)
                .impactLevel(impactLevel)
                .trendDirection(trend)
                .concentrationIssues(riskFactors.getConcentrationIssues())
                .motivationIssues(riskFactors.getMotivationIssues())
                .sleepIssues(riskFactors.getSleepIssues())
                .socialIsolation(riskFactors.getSocialIsolation())
                .financialStress(riskFactors.getFinancialStress())
                .familyIssues(riskFactors.getFamilyIssues())
                .substanceUseConcern(riskFactors.getSubstanceUseConcern())
                .counselingRecommended(recommendations.getCounselingRecommended())
                .academicSupportRecommended(recommendations.getAcademicSupportRecommended())
                .peerSupportRecommended(recommendations.getPeerSupportRecommended())
                .lifestyleChangesRecommended(recommendations.getLifestyleChangesRecommended())
                .referralRecommended(recommendations.getReferralRecommended())
                .interventionUrgency(urgency)
                .analysisSummary(generateNarrativeSummary(client, status, correlationStrength, impactLevel, trend))
                .recommendations(generateRecommendationText(recommendations))
                .isAiGenerated(true)
                .aiConfidenceScore(calculateConfidenceScore(academicHistory, previousAnalyses))
                .build();

        MentalHealthAcademicAnalysis saved = analysisRepository.save(analysis);

        // Build response
        List<String> keyFindings = generateKeyFindings(saved);
        List<String> suggestedInterventions = generateSuggestedInterventions(recommendations, riskFactors);
        String riskPrediction = generateRiskPrediction(trend, status, impactLevel);

        return AiAnalysisResponse.builder()
                .analysis(mapper.toResponse(saved))
                .aiNarrative(saved.getAnalysisSummary())
                .confidenceScore(saved.getAiConfidenceScore())
                .keyFindings(keyFindings)
                .suggestedInterventions(suggestedInterventions)
                .riskPrediction(riskPrediction)
                .build();
    }

    // Get by ID
    @Transactional(readOnly = true)
    public MentalHealthAcademicAnalysisResponse getById(Long id) {
        MentalHealthAcademicAnalysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with id: " + id));
        return mapper.toResponse(analysis);
    }

    // Get all for a client
    @Transactional(readOnly = true)
    public List<MentalHealthAcademicAnalysisResponse> getByClientId(Long clientId) {
        return analysisRepository.findByClientIdOrderByAnalysisDateDesc(clientId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // Get paginated
    @Transactional(readOnly = true)
    public Page<MentalHealthAcademicAnalysisResponse> getByClientIdPaginated(Long clientId, Pageable pageable) {
        return analysisRepository.findByClientId(clientId, pageable)
                .map(mapper::toResponse);
    }

    // Get latest for a client
    @Transactional(readOnly = true)
    public MentalHealthAcademicAnalysisResponse getLatestForClient(Long clientId) {
        return analysisRepository.findTopByClientIdOrderByAnalysisDateDesc(clientId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No analysis found for client: " + clientId));
    }

    // Get high-risk analyses
    @Transactional(readOnly = true)
    public List<MentalHealthAcademicAnalysisResponse> getHighRiskAnalyses() {
        return analysisRepository.findHighRiskAnalyses()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // Get urgent interventions
    @Transactional(readOnly = true)
    public List<MentalHealthAcademicAnalysisResponse> getUrgentInterventions() {
        return analysisRepository.findUrgentInterventions()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // Get correlation analysis
    @Transactional(readOnly = true)
    public CorrelationAnalysisResult getCorrelationAnalysis(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        List<Object[]> trendData = analysisRepository.findTrendDataByClient(clientId);
        
        List<DataPoint> mentalHealthData = new ArrayList<>();
        List<DataPoint> academicData = new ArrayList<>();
        
        for (Object[] row : trendData) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal mentalScore = (BigDecimal) row[1];
            BigDecimal gpa = (BigDecimal) row[2];
            
            if (mentalScore != null) {
                mentalHealthData.add(DataPoint.builder()
                        .date(date)
                        .value(mentalScore)
                        .label("Mental Health Score")
                        .build());
            }
            if (gpa != null) {
                academicData.add(DataPoint.builder()
                        .date(date)
                        .value(gpa)
                        .label("GPA")
                        .build());
            }
        }

        BigDecimal correlation = calculateCorrelationScore(clientId);
        CorrelationStrength strength = determineCorrelationStrength(correlation);
        String interpretation = interpretCorrelation(strength);

        return CorrelationAnalysisResult.builder()
                .clientId(clientId)
                .clientName(client.getFirstName() + " " + client.getLastName())
                .correlationCoefficient(correlation)
                .correlationStrength(strength.name())
                .interpretation(interpretation)
                .mentalHealthData(mentalHealthData)
                .academicData(academicData)
                .statisticalSignificance(determineStatisticalSignificance(correlation, trendData.size()))
                .recommendation(generateCorrelationRecommendation(strength))
                .build();
    }

    // Get trend analysis
    @Transactional(readOnly = true)
    public TrendAnalysisResult getTrendAnalysis(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        List<Object[]> trendData = analysisRepository.findTrendDataByClient(clientId);
        
        List<TrendDataPoint> dataPoints = trendData.stream()
                .map(row -> TrendDataPoint.builder()
                        .date((LocalDate) row[0])
                        .mentalHealthScore((BigDecimal) row[1])
                        .gpa((BigDecimal) row[2])
                        .period(((LocalDate) row[0]).getMonth().name())
                        .build())
                .collect(Collectors.toList());

        String mentalHealthTrend = calculateMentalHealthTrend(dataPoints);
        String academicTrend = calculateAcademicTrend(dataPoints);
        String overallTrend = determineOverallTrend(mentalHealthTrend, academicTrend);

        return TrendAnalysisResult.builder()
                .clientId(clientId)
                .clientName(client.getFirstName() + " " + client.getLastName())
                .mentalHealthTrend(mentalHealthTrend)
                .academicTrend(academicTrend)
                .overallTrend(overallTrend)
                .trendData(dataPoints)
                .prediction(generatePrediction(mentalHealthTrend, academicTrend))
                .riskAssessment(assessRiskFromTrend(overallTrend))
                .build();
    }

    // Get dashboard statistics
    @Transactional(readOnly = true)
    public AnalysisDashboardStats getDashboardStats() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        
        List<Object[]> mentalHealthCounts = analysisRepository.countByMentalHealthStatus();
        List<Object[]> impactCounts = analysisRepository.countByImpactLevel();
        
        MentalHealthDistribution mhDist = buildMentalHealthDistribution(mentalHealthCounts);
        ImpactDistribution impactDist = buildImpactDistribution(impactCounts);

        long total = analysisRepository.count();
        long highRisk = analysisRepository.findHighRiskAnalyses().size();
        long urgent = analysisRepository.findUrgentInterventions().size();
        long declining = analysisRepository.findByTrendDirection(TrendDirection.DECLINING).size();
        long improving = analysisRepository.findByTrendDirection(TrendDirection.IMPROVING).size();

        List<Object[]> correlationData = analysisRepository.findMentalHealthGpaCorrelationData();
        BigDecimal avgCorrelation = calculateAverageCorrelation(correlationData);

        return AnalysisDashboardStats.builder()
                .totalAnalyses(total)
                .highRiskStudents(highRisk)
                .needingIntervention(urgent)
                .averageCorrelation(avgCorrelation)
                .decliningStudents(declining)
                .improvingStudents(improving)
                .mentalHealthDistribution(mhDist)
                .impactDistribution(impactDist)
                .build();
    }

    // Get intervention report
    @Transactional(readOnly = true)
    public InterventionReport getInterventionReport() {
        List<MentalHealthAcademicAnalysis> allAnalyses = analysisRepository.findAll();
        List<MentalHealthAcademicAnalysis> needingIntervention = analysisRepository.findUrgentInterventions();
        
        long immediate = needingIntervention.stream()
                .filter(a -> a.getInterventionUrgency() == InterventionUrgency.IMMEDIATE)
                .count();
        long high = needingIntervention.stream()
                .filter(a -> a.getInterventionUrgency() == InterventionUrgency.HIGH)
                .count();
        long moderate = needingIntervention.stream()
                .filter(a -> a.getInterventionUrgency() == InterventionUrgency.MODERATE)
                .count();

        List<StudentAnalysisSummary> urgentCases = needingIntervention.stream()
                .limit(10)
                .map(mapper::toSummary)
                .collect(Collectors.toList());

        List<String> commonRiskFactors = identifyCommonRiskFactors(allAnalyses);
        List<String> topRecommendations = generateTopRecommendations(needingIntervention);

        return InterventionReport.builder()
                .totalStudentsAnalyzed((long) allAnalyses.size())
                .studentsNeedingIntervention((long) needingIntervention.size())
                .immediateInterventions(immediate)
                .highPriorityInterventions(high)
                .moderateInterventions(moderate)
                .urgentCases(urgentCases)
                .commonRiskFactors(commonRiskFactors)
                .topRecommendations(topRecommendations)
                .build();
    }

    // Helper methods
    private BigDecimal calculateOverallMentalHealthScore(BigDecimal depression, BigDecimal anxiety, BigDecimal stress) {
        if (depression == null && anxiety == null && stress == null) return null;
        
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        
        if (depression != null) { sum = sum.add(depression); count++; }
        if (anxiety != null) { sum = sum.add(anxiety); count++; }
        if (stress != null) { sum = sum.add(stress); count++; }
        
        return count > 0 ? sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : null;
    }

    private MentalHealthStatus determineMentalHealthStatus(BigDecimal score, String requestedStatus) {
        if (requestedStatus != null) {
            try {
                return MentalHealthStatus.valueOf(requestedStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Fall through
            }
        }
        
        if (score == null) return MentalHealthStatus.NOT_ASSESSED;
        
        double val = score.doubleValue();
        if (val <= 5) return MentalHealthStatus.EXCELLENT;
        if (val <= 10) return MentalHealthStatus.GOOD;
        if (val <= 15) return MentalHealthStatus.MODERATE;
        if (val <= 20) return MentalHealthStatus.AT_RISK;
        return MentalHealthStatus.CRISIS;
    }

    private BigDecimal calculateGpaChange(Long clientId, BigDecimal currentGpa) {
        if (currentGpa == null) return null;
        
        List<AcademicPerformance> records = academicPerformanceRepository.findByClientIdOrderByRecordDateDesc(clientId);
        if (records.size() < 2) return BigDecimal.ZERO;
        
        BigDecimal previousGpa = records.get(1).getGpa();
        if (previousGpa == null) return BigDecimal.ZERO;
        
        return currentGpa.subtract(previousGpa);
    }

    private BigDecimal calculateCorrelationScore(Long clientId) {
        List<Object[]> data = analysisRepository.findTrendDataByClient(clientId);
        if (data.size() < 3) return BigDecimal.ZERO;
        
        List<Double> mentalScores = new ArrayList<>();
        List<Double> gpas = new ArrayList<>();
        
        for (Object[] row : data) {
            BigDecimal mental = (BigDecimal) row[1];
            BigDecimal gpa = (BigDecimal) row[2];
            if (mental != null && gpa != null) {
                mentalScores.add(mental.doubleValue());
                gpas.add(gpa.doubleValue());
            }
        }
        
        if (mentalScores.size() < 3) return BigDecimal.ZERO;
        
        // Calculate Pearson correlation
        double correlation = calculatePearsonCorrelation(mentalScores, gpas);
        return BigDecimal.valueOf(correlation).setScale(3, RoundingMode.HALF_UP);
    }

    private double calculatePearsonCorrelation(List<Double> x, List<Double> y) {
        int n = x.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
        
        for (int i = 0; i < n; i++) {
            sumX += x.get(i);
            sumY += y.get(i);
            sumXY += x.get(i) * y.get(i);
            sumX2 += x.get(i) * x.get(i);
            sumY2 += y.get(i) * y.get(i);
        }
        
        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
        
        return denominator == 0 ? 0 : numerator / denominator;
    }

    private CorrelationStrength determineCorrelationStrength(BigDecimal correlation) {
        if (correlation == null) return CorrelationStrength.NEGLIGIBLE;
        
        double val = correlation.doubleValue();
        if (val > 0.7) return CorrelationStrength.STRONG_POSITIVE;
        if (val > 0.4) return CorrelationStrength.MODERATE_POSITIVE;
        if (val > 0.2) return CorrelationStrength.WEAK_POSITIVE;
        if (val > -0.2) return CorrelationStrength.NEGLIGIBLE;
        if (val > -0.4) return CorrelationStrength.WEAK_NEGATIVE;
        if (val > -0.7) return CorrelationStrength.MODERATE_NEGATIVE;
        return CorrelationStrength.STRONG_NEGATIVE;
    }

    private ImpactLevel determineImpactLevel(MentalHealthStatus status, BigDecimal gpaChange, 
                                             MentalHealthAcademicAnalysisRequest request) {
        if (status == MentalHealthStatus.CRISIS) return ImpactLevel.SEVERE;
        if (status == MentalHealthStatus.AT_RISK) return ImpactLevel.SIGNIFICANT;
        
        int riskFactorCount = countRiskFactors(request);
        if (riskFactorCount >= 4) return ImpactLevel.SIGNIFICANT;
        if (riskFactorCount >= 2) return ImpactLevel.MODERATE;
        if (riskFactorCount >= 1) return ImpactLevel.MINIMAL;
        
        if (gpaChange != null && gpaChange.doubleValue() < -0.5) return ImpactLevel.SIGNIFICANT;
        if (gpaChange != null && gpaChange.doubleValue() < -0.3) return ImpactLevel.MODERATE;
        
        return ImpactLevel.NONE;
    }

    private int countRiskFactors(MentalHealthAcademicAnalysisRequest request) {
        int count = 0;
        if (Boolean.TRUE.equals(request.getConcentrationIssues())) count++;
        if (Boolean.TRUE.equals(request.getMotivationIssues())) count++;
        if (Boolean.TRUE.equals(request.getSleepIssues())) count++;
        if (Boolean.TRUE.equals(request.getSocialIsolation())) count++;
        if (Boolean.TRUE.equals(request.getFinancialStress())) count++;
        if (Boolean.TRUE.equals(request.getFamilyIssues())) count++;
        if (Boolean.TRUE.equals(request.getSubstanceUseConcern())) count++;
        return count;
    }

    private TrendDirection determineTrendDirection(Long clientId) {
        List<MentalHealthAcademicAnalysis> analyses = analysisRepository.findByClientIdOrderByAnalysisDateDesc(clientId);
        if (analyses.size() < 2) return TrendDirection.UNKNOWN;
        
        MentalHealthAcademicAnalysis latest = analyses.get(0);
        MentalHealthAcademicAnalysis previous = analyses.get(1);
        
        if (latest.getOverallMentalHealthScore() == null || previous.getOverallMentalHealthScore() == null) {
            return TrendDirection.UNKNOWN;
        }
        
        int comparison = latest.getOverallMentalHealthScore().compareTo(previous.getOverallMentalHealthScore());
        // Lower score is better for mental health (DASS scale)
        if (comparison < 0) return TrendDirection.IMPROVING;
        if (comparison > 0) return TrendDirection.DECLINING;
        return TrendDirection.STABLE;
    }

    private InterventionUrgency determineInterventionUrgency(String requested, ImpactLevel impact, MentalHealthStatus status) {
        if (requested != null) {
            try {
                return InterventionUrgency.valueOf(requested.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Fall through
            }
        }
        
        if (status == MentalHealthStatus.CRISIS) return InterventionUrgency.IMMEDIATE;
        if (status == MentalHealthStatus.AT_RISK || impact == ImpactLevel.SEVERE) return InterventionUrgency.HIGH;
        if (impact == ImpactLevel.SIGNIFICANT) return InterventionUrgency.MODERATE;
        if (impact == ImpactLevel.MODERATE) return InterventionUrgency.LOW;
        return InterventionUrgency.NONE;
    }

    // Additional helper methods for AI analysis
    private BigDecimal estimateDepressionScore(List<MentalHealthAcademicAnalysis> analyses, List<AcademicPerformance> academic) {
        if (!analyses.isEmpty()) {
            return analyses.stream()
                    .filter(a -> a.getDepressionScore() != null)
                    .map(MentalHealthAcademicAnalysis::getDepressionScore)
                    .findFirst()
                    .orElse(BigDecimal.valueOf(10));
        }
        return BigDecimal.valueOf(10); // Default moderate
    }

    private BigDecimal estimateAnxietyScore(List<MentalHealthAcademicAnalysis> analyses, List<AcademicPerformance> academic) {
        if (!analyses.isEmpty()) {
            return analyses.stream()
                    .filter(a -> a.getAnxietyScore() != null)
                    .map(MentalHealthAcademicAnalysis::getAnxietyScore)
                    .findFirst()
                    .orElse(BigDecimal.valueOf(10));
        }
        return BigDecimal.valueOf(10);
    }

    private BigDecimal estimateStressScore(List<MentalHealthAcademicAnalysis> analyses, List<AcademicPerformance> academic) {
        if (!analyses.isEmpty()) {
            return analyses.stream()
                    .filter(a -> a.getStressScore() != null)
                    .map(MentalHealthAcademicAnalysis::getStressScore)
                    .findFirst()
                    .orElse(BigDecimal.valueOf(12));
        }
        return BigDecimal.valueOf(12);
    }

    private RiskFactors identifyRiskFactors(List<AcademicPerformance> academic, List<MentalHealthAcademicAnalysis> analyses) {
        boolean lowAttendance = academic.stream()
                .anyMatch(a -> a.getAttendanceRate() != null && a.getAttendanceRate().doubleValue() < 70);
        boolean decliningGpa = false;
        if (academic.size() >= 2) {
            decliningGpa = academic.get(0).getGpa().compareTo(academic.get(1).getGpa()) < 0;
        }
        
        return RiskFactors.builder()
                .concentrationIssues(decliningGpa)
                .motivationIssues(lowAttendance)
                .sleepIssues(false)
                .socialIsolation(false)
                .financialStress(false)
                .familyIssues(false)
                .substanceUseConcern(false)
                .build();
    }

    private Recommendations generateRecommendations(MentalHealthStatus status, RiskFactors risks, AcademicPerformance academic) {
        boolean counseling = status == MentalHealthStatus.AT_RISK || status == MentalHealthStatus.CRISIS;
        boolean academicSupport = academic != null && academic.getGpa() != null && academic.getGpa().doubleValue() < 2.5;
        boolean peerSupport = Boolean.TRUE.equals(risks.getSocialIsolation());
        boolean lifestyle = Boolean.TRUE.equals(risks.getSleepIssues()) || Boolean.TRUE.equals(risks.getConcentrationIssues());
        boolean referral = status == MentalHealthStatus.CRISIS;
        
        String urgency = status == MentalHealthStatus.CRISIS ? "IMMEDIATE" :
                         status == MentalHealthStatus.AT_RISK ? "HIGH" : "MODERATE";
        
        return Recommendations.builder()
                .counselingRecommended(counseling)
                .academicSupportRecommended(academicSupport)
                .peerSupportRecommended(peerSupport)
                .lifestyleChangesRecommended(lifestyle)
                .referralRecommended(referral)
                .interventionUrgency(urgency)
                .build();
    }

    private ImpactLevel determineImpactLevelFromFactors(RiskFactors risks, MentalHealthStatus status) {
        if (status == MentalHealthStatus.CRISIS) return ImpactLevel.SEVERE;
        if (status == MentalHealthStatus.AT_RISK) return ImpactLevel.SIGNIFICANT;
        if (risks.getActiveCount() >= 4) return ImpactLevel.SIGNIFICANT;
        if (risks.getActiveCount() >= 2) return ImpactLevel.MODERATE;
        if (risks.getActiveCount() >= 1) return ImpactLevel.MINIMAL;
        return ImpactLevel.NONE;
    }

    private String generateNarrativeSummary(Client client, MentalHealthStatus status, 
                                           CorrelationStrength correlation, ImpactLevel impact, TrendDirection trend) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analysis Summary for ").append(client.getFirstName()).append(": ");
        sb.append("Mental health status is assessed as ").append(status.name().toLowerCase().replace("_", " ")).append(". ");
        sb.append("The correlation between mental health and academic performance is ").append(correlation.name().toLowerCase().replace("_", " ")).append(". ");
        sb.append("Current impact level on academics is ").append(impact.name().toLowerCase()).append(" ");
        sb.append("with an overall ").append(trend.name().toLowerCase()).append(" trend.");
        return sb.toString();
    }

    private String generateRecommendationText(Recommendations recs) {
        List<String> recommendations = new ArrayList<>();
        if (Boolean.TRUE.equals(recs.getCounselingRecommended())) recommendations.add("Individual counseling sessions");
        if (Boolean.TRUE.equals(recs.getAcademicSupportRecommended())) recommendations.add("Academic support services");
        if (Boolean.TRUE.equals(recs.getPeerSupportRecommended())) recommendations.add("Peer support groups");
        if (Boolean.TRUE.equals(recs.getLifestyleChangesRecommended())) recommendations.add("Lifestyle modifications");
        if (Boolean.TRUE.equals(recs.getReferralRecommended())) recommendations.add("Professional mental health referral");
        return String.join("; ", recommendations);
    }

    private BigDecimal calculateConfidenceScore(List<AcademicPerformance> academic, List<MentalHealthAcademicAnalysis> analyses) {
        int dataPoints = academic.size() + analyses.size();
        if (dataPoints >= 10) return BigDecimal.valueOf(0.85);
        if (dataPoints >= 5) return BigDecimal.valueOf(0.70);
        if (dataPoints >= 3) return BigDecimal.valueOf(0.55);
        return BigDecimal.valueOf(0.40);
    }

    private List<String> generateKeyFindings(MentalHealthAcademicAnalysis analysis) {
        List<String> findings = new ArrayList<>();
        findings.add("Mental health status: " + analysis.getMentalHealthStatus().name());
        if (analysis.getCurrentGpa() != null) {
            findings.add("Current GPA: " + analysis.getCurrentGpa());
        }
        if (analysis.getCorrelationStrength() != null) {
            findings.add("Correlation strength: " + analysis.getCorrelationStrength().name());
        }
        findings.add("Impact level: " + analysis.getImpactLevel().name());
        return findings;
    }

    private List<String> generateSuggestedInterventions(Recommendations recs, RiskFactors risks) {
        List<String> interventions = new ArrayList<>();
        if (Boolean.TRUE.equals(recs.getCounselingRecommended())) {
            interventions.add("Schedule regular counseling sessions");
        }
        if (Boolean.TRUE.equals(recs.getAcademicSupportRecommended())) {
            interventions.add("Connect with academic advisor for study strategies");
        }
        if (Boolean.TRUE.equals(risks.getSleepIssues())) {
            interventions.add("Implement sleep hygiene practices");
        }
        if (Boolean.TRUE.equals(risks.getSocialIsolation())) {
            interventions.add("Join student support groups or clubs");
        }
        return interventions;
    }

    private String generateRiskPrediction(TrendDirection trend, MentalHealthStatus status, ImpactLevel impact) {
        if (trend == TrendDirection.DECLINING && (status == MentalHealthStatus.AT_RISK || status == MentalHealthStatus.CRISIS)) {
            return "HIGH RISK: Immediate intervention recommended to prevent further decline";
        }
        if (trend == TrendDirection.DECLINING) {
            return "MODERATE RISK: Monitoring and proactive support recommended";
        }
        if (trend == TrendDirection.IMPROVING) {
            return "LOW RISK: Positive trajectory, continue current support";
        }
        return "STABLE: Continue regular monitoring";
    }

    private String interpretCorrelation(CorrelationStrength strength) {
        switch (strength) {
            case STRONG_NEGATIVE:
                return "Strong inverse relationship: as mental health issues increase, academic performance significantly decreases";
            case MODERATE_NEGATIVE:
                return "Moderate inverse relationship: mental health issues appear to negatively affect academics";
            case WEAK_NEGATIVE:
                return "Slight inverse relationship between mental health and academics";
            case NEGLIGIBLE:
                return "No significant relationship detected between mental health and academics";
            case WEAK_POSITIVE:
            case MODERATE_POSITIVE:
            case STRONG_POSITIVE:
                return "Positive relationship detected (unusual pattern, may need review)";
            default:
                return "Insufficient data to determine relationship";
        }
    }

    private String determineStatisticalSignificance(BigDecimal correlation, int sampleSize) {
        if (sampleSize < 5) return "Insufficient data for significance testing";
        if (Math.abs(correlation.doubleValue()) > 0.5 && sampleSize >= 10) return "Statistically significant";
        if (Math.abs(correlation.doubleValue()) > 0.3 && sampleSize >= 15) return "Marginally significant";
        return "Not statistically significant";
    }

    private String generateCorrelationRecommendation(CorrelationStrength strength) {
        switch (strength) {
            case STRONG_NEGATIVE:
            case MODERATE_NEGATIVE:
                return "Strong evidence suggests mental health interventions may improve academic performance";
            case WEAK_NEGATIVE:
                return "Consider mental health support as part of academic improvement plan";
            default:
                return "Continue monitoring both mental health and academic performance";
        }
    }

    private String calculateMentalHealthTrend(List<TrendDataPoint> dataPoints) {
        if (dataPoints.size() < 2) return "INSUFFICIENT_DATA";
        // Simple trend calculation
        int improving = 0, declining = 0;
        for (int i = 1; i < dataPoints.size(); i++) {
            if (dataPoints.get(i).getMentalHealthScore() != null && dataPoints.get(i-1).getMentalHealthScore() != null) {
                if (dataPoints.get(i).getMentalHealthScore().compareTo(dataPoints.get(i-1).getMentalHealthScore()) < 0) {
                    improving++;
                } else {
                    declining++;
                }
            }
        }
        if (improving > declining) return "IMPROVING";
        if (declining > improving) return "DECLINING";
        return "STABLE";
    }

    private String calculateAcademicTrend(List<TrendDataPoint> dataPoints) {
        if (dataPoints.size() < 2) return "INSUFFICIENT_DATA";
        int improving = 0, declining = 0;
        for (int i = 1; i < dataPoints.size(); i++) {
            if (dataPoints.get(i).getGpa() != null && dataPoints.get(i-1).getGpa() != null) {
                if (dataPoints.get(i).getGpa().compareTo(dataPoints.get(i-1).getGpa()) > 0) {
                    improving++;
                } else if (dataPoints.get(i).getGpa().compareTo(dataPoints.get(i-1).getGpa()) < 0) {
                    declining++;
                }
            }
        }
        if (improving > declining) return "IMPROVING";
        if (declining > improving) return "DECLINING";
        return "STABLE";
    }

    private String determineOverallTrend(String mentalTrend, String academicTrend) {
        if ("IMPROVING".equals(mentalTrend) && "IMPROVING".equals(academicTrend)) return "POSITIVE";
        if ("DECLINING".equals(mentalTrend) && "DECLINING".equals(academicTrend)) return "CONCERNING";
        if ("DECLINING".equals(mentalTrend) || "DECLINING".equals(academicTrend)) return "MIXED";
        return "STABLE";
    }

    private String generatePrediction(String mentalTrend, String academicTrend) {
        if ("DECLINING".equals(mentalTrend) && "DECLINING".equals(academicTrend)) {
            return "Risk of continued decline without intervention";
        }
        if ("IMPROVING".equals(mentalTrend) && "IMPROVING".equals(academicTrend)) {
            return "Positive outlook with continued support";
        }
        return "Outcome depends on intervention and support";
    }

    private String assessRiskFromTrend(String overallTrend) {
        switch (overallTrend) {
            case "CONCERNING": return "HIGH";
            case "MIXED": return "MODERATE";
            case "STABLE": return "LOW";
            case "POSITIVE": return "MINIMAL";
            default: return "UNKNOWN";
        }
    }

    private MentalHealthDistribution buildMentalHealthDistribution(List<Object[]> counts) {
        MentalHealthDistribution dist = new MentalHealthDistribution();
        for (Object[] row : counts) {
            MentalHealthStatus status = (MentalHealthStatus) row[0];
            Long count = (Long) row[1];
            switch (status) {
                case EXCELLENT: dist.setExcellent(count); break;
                case GOOD: dist.setGood(count); break;
                case MODERATE: dist.setModerate(count); break;
                case AT_RISK: dist.setAtRisk(count); break;
                case CRISIS: dist.setCrisis(count); break;
            }
        }
        return dist;
    }

    private ImpactDistribution buildImpactDistribution(List<Object[]> counts) {
        ImpactDistribution dist = new ImpactDistribution();
        for (Object[] row : counts) {
            ImpactLevel level = (ImpactLevel) row[0];
            Long count = (Long) row[1];
            switch (level) {
                case NONE: dist.setNone(count); break;
                case MINIMAL: dist.setMinimal(count); break;
                case MODERATE: dist.setModerate(count); break;
                case SIGNIFICANT: dist.setSignificant(count); break;
                case SEVERE: dist.setSevere(count); break;
            }
        }
        return dist;
    }

    private BigDecimal calculateAverageCorrelation(List<Object[]> data) {
        if (data.isEmpty()) return BigDecimal.ZERO;
        
        List<Double> mentalScores = new ArrayList<>();
        List<Double> gpas = new ArrayList<>();
        
        for (Object[] row : data) {
            BigDecimal mental = (BigDecimal) row[0];
            BigDecimal gpa = (BigDecimal) row[1];
            if (mental != null && gpa != null) {
                mentalScores.add(mental.doubleValue());
                gpas.add(gpa.doubleValue());
            }
        }
        
        if (mentalScores.size() < 2) return BigDecimal.ZERO;
        
        double correlation = calculatePearsonCorrelation(mentalScores, gpas);
        return BigDecimal.valueOf(correlation).setScale(2, RoundingMode.HALF_UP);
    }


    private List<String> identifyCommonRiskFactors(List<MentalHealthAcademicAnalysis> analyses) {
        Map<String, Long> factorCounts = new HashMap<>();
        for (MentalHealthAcademicAnalysis a : analyses) {
            if (Boolean.TRUE.equals(a.getConcentrationIssues())) 
                factorCounts.merge("Concentration Issues", 1L, Long::sum);
            if (Boolean.TRUE.equals(a.getMotivationIssues())) 
                factorCounts.merge("Motivation Issues", 1L, Long::sum);
            if (Boolean.TRUE.equals(a.getSleepIssues())) 
                factorCounts.merge("Sleep Issues", 1L, Long::sum);
            if (Boolean.TRUE.equals(a.getSocialIsolation())) 
                factorCounts.merge("Social Isolation", 1L, Long::sum);
            if (Boolean.TRUE.equals(a.getFinancialStress())) 
                factorCounts.merge("Financial Stress", 1L, Long::sum);
        }
        return factorCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<String> generateTopRecommendations(List<MentalHealthAcademicAnalysis> analyses) {
        List<String> recommendations = new ArrayList<>();
        recommendations.add("Increase availability of counseling services");
        recommendations.add("Implement peer support programs");
        recommendations.add("Provide academic tutoring services");
        recommendations.add("Offer stress management workshops");
        recommendations.add("Create early warning system for at-risk students");
        return recommendations;
    }

}