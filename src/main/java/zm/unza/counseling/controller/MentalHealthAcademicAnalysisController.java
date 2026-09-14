package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.MentalHealthAcademicDtos.MentalHealthAcademicAnalysisRequest;
import zm.unza.counseling.dto.MentalHealthAcademicDtos.MentalHealthAcademicAnalysisResponse;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.service.MentalHealthAcademicAnalysisService;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/analysis","/api/analysis","/v1/analysis","/analysis"})
@RequiredArgsConstructor
public class MentalHealthAcademicAnalysisController {

    private final MentalHealthAcademicAnalysisService service;

    @GetMapping("/high-risk")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<MentalHealthAcademicAnalysisResponse>>> getHighRiskAnalyses() {
        return ResponseEntity.ok(ApiResponse.success(service.getHighRiskAnalyses()));
    }

    @GetMapping("/urgent")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<MentalHealthAcademicAnalysisResponse>>> getUrgentInterventions() {
        return ResponseEntity.ok(ApiResponse.success(service.getUrgentInterventions()));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<MentalHealthAcademicAnalysisResponse>>> getAnalysesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(service.getAnalysesByClient(clientId)));
    }

    @GetMapping("/client/{clientId}/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT', 'STUDENT')")
    public ResponseEntity<ApiResponse<MentalHealthAcademicAnalysisResponse>> getLatestAnalysisForClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(service.getLatestAnalysisForClient(clientId)));
    }

    @GetMapping("/client/{clientId}/trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<List<MentalHealthAcademicAnalysisResponse>>> getAnalysisTrendForClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(service.getAnalysisTrendForClient(clientId)));
    }

    @GetMapping("/client/{clientId}/correlation")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getCorrelationAnalysis(@PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.success(service.getCorrelationAnalysis(clientId)));
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

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<MentalHealthAcademicAnalysisResponse>> createAnalysis(@RequestBody MentalHealthAcademicAnalysisRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createAnalysis(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<MentalHealthAcademicAnalysisResponse>> updateAnalysis(@PathVariable Long id, @RequestBody MentalHealthAcademicAnalysisRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.updateAnalysis(id, request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'CLIENT', 'STUDENT')")
    public ResponseEntity<ApiResponse<MentalHealthAcademicAnalysisResponse>> getAnalysisById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getAnalysisById(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAnalysis(@PathVariable Long id) {
        service.deleteAnalysis(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}