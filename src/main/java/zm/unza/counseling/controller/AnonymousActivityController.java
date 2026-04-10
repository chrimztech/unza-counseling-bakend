package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.response.AnonymousUserActivityDto;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.service.AnonymousActivityService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/admin/anonymous-activity", "/api/admin/anonymous-activity", "/v1/admin/anonymous-activity", "/admin/anonymous-activity"})
@RequiredArgsConstructor
public class AnonymousActivityController {

    private final AnonymousActivityService anonymousActivityService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AnonymousUserActivityDto>>> getAnonymousActivity() {
        return ResponseEntity.ok(ApiResponse.success(anonymousActivityService.getAnonymousActivity()));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getAnonymousActivityStats() {
        return ResponseEntity.ok(ApiResponse.success(anonymousActivityService.getAnonymousActivityStats()));
    }
}
