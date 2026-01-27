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

    public List<MentalHealthAcademicAnalysis> getAnalysesByClient(Long clientId) {
        return repository.findByClientId(clientId);
    }

    public MentalHealthAcademicAnalysis getLatestAnalysisForClient(Long clientId) {
        return repository.findTopByClientIdOrderByCreatedAtDesc(clientId)
                .orElseThrow(() -> new RuntimeException("No analysis found for client"));
    }

    public Object getAnalysisTrendForClient(Long clientId) {
        return repository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    // Additional business logic for analysis generation would go here
}