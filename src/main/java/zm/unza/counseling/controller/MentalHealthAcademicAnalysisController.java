package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.MentalHealthAcademicDtos.MentalHealthAcademicAnalysisResponse;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.mapper.MentalHealthAcademicMapper;
import zm.unza.counseling.service.MentalHealthAcademicAnalysisService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analysis")
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
}