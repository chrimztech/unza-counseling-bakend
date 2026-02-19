package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.MentalHealthAcademicDtos.MentalHealthAcademicAnalysisResponse;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.MentalHealthAcademicAnalysis;
import zm.unza.counseling.mapper.MentalHealthAcademicMapper;
import zm.unza.counseling.service.MentalHealthAcademicAnalysisService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/analysis","/api/analysis","/v1/analysis","/analysis"})
@RequiredArgsConstructor
public class MentalHealthAcademicAnalysisController {

    private final MentalHealthAcademicAnalysisService service;
    private final MentalHealthAcademicMapper mapper;

    @GetMapping("/high-risk")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<MentalHealthAcademicAnalysisResponse>>> getHighRiskAnalyses() {
        List<MentalHealthAcademicAnalysisResponse> response = service.getHighRiskAnalyses().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/urgent")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<MentalHealthAcademicAnalysisResponse>>> getUrgentInterventions() {
        List<MentalHealthAcademicAnalysisResponse> response = service.getUrgentInterventions().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<MentalHealthAcademicAnalysisResponse>>> getAnalysesByClient(@PathVariable Long clientId) {
        List<MentalHealthAcademicAnalysisResponse> response = service.getAnalysesByClient(clientId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/client/{clientId}/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT')")
    public ResponseEntity<ApiResponse<MentalHealthAcademicAnalysisResponse>> getLatestAnalysisForClient(@PathVariable Long clientId) {
        MentalHealthAcademicAnalysis analysis = service.getLatestAnalysisForClient(clientId);
        if (analysis == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        MentalHealthAcademicAnalysisResponse response = mapper.toResponse(analysis);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/client/{clientId}/trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<?>> getAnalysisTrendForClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(service.getAnalysisTrendForClient(clientId)));
    }

    // Dashboard endpoints
    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Object>> getDashboardStats() {
        // Return dashboard statistics
        Object stats = service.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/intervention-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Object>> getInterventionReport() {
        // Return intervention report
        Object report = service.getInterventionReport();
        return ResponseEntity.ok(ApiResponse.success(report));
    }

}