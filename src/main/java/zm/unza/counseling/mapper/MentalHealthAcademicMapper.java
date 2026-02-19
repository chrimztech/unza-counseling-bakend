package zm.unza.counseling.mapper;

import org.springframework.stereotype.Component;
import zm.unza.counseling.dto.MentalHealthAcademicDtos.*;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis;

@Component
public class MentalHealthAcademicMapper {

    public MentalHealthAcademicAnalysisResponse toResponse(MentalHealthAcademicAnalysis analysis) {
        if (analysis == null) return null;

        RiskFactors riskFactors = RiskFactors.builder()
                .concentrationIssues(analysis.getConcentrationIssues())
                .motivationIssues(analysis.getMotivationIssues())
                .sleepIssues(analysis.getSleepIssues())
                .socialIsolation(analysis.getSocialIsolation())
                .financialStress(analysis.getFinancialStress())
                .familyIssues(analysis.getFamilyIssues())
                .substanceUseConcern(analysis.getSubstanceUseConcern())
                .build();

        Recommendations recommendations = Recommendations.builder()
                .counselingRecommended(analysis.getCounselingRecommended())
                .academicSupportRecommended(analysis.getAcademicSupportRecommended())
                .peerSupportRecommended(analysis.getPeerSupportRecommended())
                .lifestyleChangesRecommended(analysis.getLifestyleChangesRecommended())
                .referralRecommended(analysis.getReferralRecommended())
                .interventionUrgency(analysis.getInterventionUrgency() != null ? analysis.getInterventionUrgency().name() : null)
                .build();

        Client client = analysis.getClient();
        Long clientId = client != null ? client.getId() : null;
        String clientName = client != null ? 
                client.getFirstName() + " " + client.getLastName() : null;
        String studentNumber = client != null ? client.getStudentId() : null;

        return MentalHealthAcademicAnalysisResponse.builder()
                .id(analysis.getId())
                .clientId(clientId)
                .clientName(clientName)
                .studentNumber(studentNumber)
                .academicPerformanceId(analysis.getAcademicPerformance() != null ? analysis.getAcademicPerformance().getId() : null)
                .selfAssessmentId(analysis.getSelfAssessment() != null ? String.valueOf(analysis.getSelfAssessment().getId()) : null)
                .riskAssessmentId(analysis.getRiskAssessment() != null ? analysis.getRiskAssessment().getId() : null)
                .analysisDate(analysis.getAnalysisDate())
                .analysisPeriodStart(analysis.getAnalysisPeriodStart())
                .analysisPeriodEnd(analysis.getAnalysisPeriodEnd())
                .depressionScore(analysis.getDepressionScore())
                .anxietyScore(analysis.getAnxietyScore())
                .stressScore(analysis.getStressScore())
                .overallMentalHealthScore(analysis.getOverallMentalHealthScore())
                .mentalHealthStatus(analysis.getMentalHealthStatus() != null ? analysis.getMentalHealthStatus().name() : null)
                .currentGpa(analysis.getCurrentGpa())
                .gpaChange(analysis.getGpaChange())
                .attendanceRate(analysis.getAttendanceRate())
                .attendanceChange(analysis.getAttendanceChange())
                .correlationScore(analysis.getCorrelationScore())
                .correlationStrength(analysis.getCorrelationStrength() != null ? analysis.getCorrelationStrength().name() : null)
                .impactLevel(analysis.getImpactLevel() != null ? analysis.getImpactLevel().name() : null)
                .trendDirection(analysis.getTrendDirection() != null ? analysis.getTrendDirection().name() : null)
                .riskFactors(riskFactors)
                .recommendations(recommendations)
                .analysisSummary(analysis.getAnalysisSummary())
                .recommendationText(analysis.getRecommendations())
                .counselorNotes(analysis.getCounselorNotes())
                .analyzedById(analysis.getAnalyzedBy() != null ? analysis.getAnalyzedBy().getId() : null)
                .analyzedByName(analysis.getAnalyzedBy() != null ? 
                        analysis.getAnalyzedBy().getFirstName() + " " + analysis.getAnalyzedBy().getLastName() : null)
                .isAiGenerated(analysis.getIsAiGenerated())
                .aiConfidenceScore(analysis.getAiConfidenceScore())
                .overallRiskScore(analysis.calculateOverallRiskScore())
                .createdAt(analysis.getCreatedAt())
                .updatedAt(analysis.getUpdatedAt())
                .build();
    }

    public StudentAnalysisSummary toSummary(MentalHealthAcademicAnalysis analysis) {
        if (analysis == null) return null;
        
        Client client = analysis.getClient();
        Long clientId = client != null ? client.getId() : null;
        String clientName = client != null ? 
                client.getFirstName() + " " + client.getLastName() : null;
        String studentNumber = client != null ? client.getStudentId() : null;
        
        return StudentAnalysisSummary.builder()
                .clientId(clientId)
                .clientName(clientName)
                .studentNumber(studentNumber)
                .latestMentalHealthScore(analysis.getOverallMentalHealthScore())
                .latestGpa(analysis.getCurrentGpa())
                .mentalHealthStatus(analysis.getMentalHealthStatus() != null ? analysis.getMentalHealthStatus().name() : null)
                .impactLevel(analysis.getImpactLevel() != null ? analysis.getImpactLevel().name() : null)
                .interventionUrgency(analysis.getInterventionUrgency() != null ? analysis.getInterventionUrgency().name() : null)
                .lastAnalysisDate(analysis.getAnalysisDate())
                .requiresFollowUp(analysis.isInterventionNeeded())
                .build();
    }
}
