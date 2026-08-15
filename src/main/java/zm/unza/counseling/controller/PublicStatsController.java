package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.repository.CaseRepository;
import zm.unza.counseling.repository.UserRepository;

import java.util.LinkedHashMap;
import java.util.Map;

// Unauthenticated: backs the login screen's stat cards, which render before
// any token exists. See SecurityConfig's permitAll rule for this path.
@RestController
@RequestMapping({"/api/public/stats", "/public/stats"})
@RequiredArgsConstructor
public class PublicStatsController {

    private final UserRepository userRepository;
    private final CaseRepository caseRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPublicStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("studentsSupported", userRepository.countStudents());
        stats.put("activeCounselors", userRepository.countCounselors());
        stats.put("casesHandled", caseRepository.count());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
