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

    public Object getDashboardStats() {
        // Return dashboard statistics
        long totalAnalyses = repository.count();
        long highRiskCount = repository.findHighRiskAnalyses().size();
        long urgentCount = repository.findUrgentInterventions().size();
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalAnalyses", totalAnalyses);
        stats.put("highRiskCount", highRiskCount);
        stats.put("urgentCount", urgentCount);
        stats.put("recentAnalyses", repository.findAll());
        return stats;
    }

    public Object getInterventionReport() {
        // Return intervention report
        java.util.Map<String, Object> report = new java.util.HashMap<>();
        report.put("highRiskAnalyses", repository.findHighRiskAnalyses());
        report.put("urgentInterventions", repository.findUrgentInterventions());
        report.put("generatedAt", java.time.LocalDateTime.now());
        return report;
    }

}