package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import zm.unza.counseling.service.UserService;
import zm.unza.counseling.service.ai.AiAssistantService;
import zm.unza.counseling.service.ai.AiInsightsService;
import zm.unza.counseling.service.ai.AiUnavailableException;

import java.util.Map;
import java.util.Objects;

/**
 * LLM-backed counselor tools under the path the web client's aiApiService already targets.
 * Responses are returned unwrapped (no ApiResponse envelope) because aiApiService reads
 * {@code response.data} directly as the result object.
 */
@RestController
@RequestMapping({"/api/v1/mental-health-analysis/ai", "/api/mental-health-analysis/ai", "/mental-health-analysis/ai"})
@PreAuthorize("hasAnyRole('COUNSELOR','ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
public class AiFeaturesController {

    private final AiAssistantService assistant;
    private final AiInsightsService insights;
    private final UserService userService;

    /**
     * Counselor workload/effectiveness insights. Counselors may only view their own;
     * admins can view any counselor's.
     */
    @GetMapping("/counselor-insights/{counselorId}")
    public ResponseEntity<?> counselorInsights(@PathVariable Long counselorId, Authentication auth) {
        boolean admin = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_SUPER_ADMIN"));
        Long selfId = userService.getUserByEmail(auth.getName()).getId();
        if (!admin && !selfId.equals(counselorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("You can only view your own insights"));
        }
        return ResponseEntity.ok(insights.counselorInsights(counselorId));
    }

    /**
     * AI-ranked picks from the real resource library. {@code clientId} may be "general" (or any
     * non-numeric value) to recommend without a client profile.
     */
    @PostMapping("/resources/{clientId}")
    public ResponseEntity<?> recommendResources(@PathVariable String clientId,
                                                @RequestBody(required = false) Map<String, Object> body) {
        Long id = clientId != null && clientId.matches("\\d+") ? Long.valueOf(clientId) : null;
        String context = body == null ? null : Objects.toString(body.get("context"), null);
        return ResponseEntity.ok(insights.recommendResources(id, context));
    }

    @PostMapping("/session-notes/{clientId}")
    public ResponseEntity<?> sessionNotes(@PathVariable String clientId, @RequestBody Map<String, Object> sessionData) {
        try {
            return ResponseEntity.ok(assistant.structuredSessionNote(sessionData));
        } catch (AiUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiResponse.error(e.getMessage()));
        }
    }
}
