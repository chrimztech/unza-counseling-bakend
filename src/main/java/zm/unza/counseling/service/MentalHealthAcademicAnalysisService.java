package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis;
import zm.unza.counseling.repository.MentalHealthAcademicAnalysisRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MentalHealthAcademicAnalysisService {
    
    private final MentalHealthAcademicAnalysisRepository repository;

    public List<MentalHealthAcademicAnalysis> getHighRiskAnalyses() {
        return repository.findHighRiskAnalyses();
    }

    public List<MentalHealthAcademicAnalysis> getUrgentInterventions() {
        return repository.findUrgentInterventions();
    }
    
    // Additional business logic for analysis generation would go here
}