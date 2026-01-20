package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zm.unza.counseling.dto.MentalHealthAcademicDtos.InterventionReport;
import zm.unza.counseling.dto.MentalHealthAcademicDtos.StudentAnalysisSummary;
import zm.unza.counseling.mapper.MentalHealthAcademicMapper;
import zm.unza.counseling.repository.MentalHealthAcademicAnalysisRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MentalHealthAcademicAnalysisRepository analysisRepository;
    private final MentalHealthAcademicMapper mapper;

    public InterventionReport getInterventionReport() {
        long totalInterventions = analysisRepository.countByInterventionNeeded(true);
        long counselingRecommended = analysisRepository.countByCounselingRecommended(true);
        long academicSupportRecommended = analysisRepository.countByAcademicSupportRecommended(true);
        long peerSupportRecommended = analysisRepository.countByPeerSupportRecommended(true);
        long lifestyleChangesRecommended = analysisRepository.countByLifestyleChangesRecommended(true);
        long referralRecommended = analysisRepository.countByReferralRecommended(true);

        List<StudentAnalysisSummary> highPriorityStudents = analysisRepository.findUrgentInterventions().stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());

        // Using the builder from the DTO
        return InterventionReport.builder()
                .totalStudentsAnalyzed(analysisRepository.count())
                .studentsNeedingIntervention(totalInterventions)
                .counselingRecommended(counselingRecommended)
                .academicSupportRecommended(academicSupportRecommended)
                .peerSupportRecommended(peerSupportRecommended)
                .lifestyleChangesRecommended(lifestyleChangesRecommended)
                .referralRecommended(referralRecommended)
                .urgentCases(highPriorityStudents)
                .build();
    }

    // Other analytics methods can be added here, e.g., for correlation, trend analysis, etc.
}