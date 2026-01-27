package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.response.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "System health and status endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Basic health check endpoint")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", System.currentTimeMillis());
        healthInfo.put("service", "UNZA Counseling System");
        
        return ResponseEntity.ok(ApiResponse.success(healthInfo, "Health check successful"));
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness check", description = "Check if the application is ready to serve traffic")
    public ResponseEntity<ApiResponse<String>> readinessCheck() {
        return ResponseEntity.ok(ApiResponse.success("READY", "Application is ready"));
    }

    @GetMapping("/live")
    @Operation(summary = "Liveness check", description = "Check if the application is alive")
    public ResponseEntity<ApiResponse<String>> livenessCheck() {
        return ResponseEntity.ok(ApiResponse.success("ALIVE", "Application is alive"));
    }
}