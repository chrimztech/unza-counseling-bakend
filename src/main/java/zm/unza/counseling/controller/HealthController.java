package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.SystemHealthResponse;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/health", "/api/health", "/v1/health", "/health"})
@RequiredArgsConstructor
@Tag(name = "Health", description = "System health and status endpoints")
public class HealthController {

    private final DataSource dataSource;

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

    @GetMapping("/system")
    @Operation(summary = "System health monitor", description = "Get detailed system health including CPU, memory, and database status")
    public ResponseEntity<ApiResponse<SystemHealthResponse>> getSystemHealth() {
        Runtime runtime = Runtime.getRuntime();
        
        // System info
        SystemHealthResponse.SystemInfo systemInfo = SystemHealthResponse.SystemInfo.builder()
                .osName(System.getProperty("os.name"))
                .osVersion(System.getProperty("os.version"))
                .osArchitecture(System.getProperty("os.arch"))
                .availableProcessors(runtime.availableProcessors())
                .javaVersion(System.getProperty("java.version"))
                .uptimeMillis(System.currentTimeMillis() - java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime())
                .build();
        
        // Memory info
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        SystemHealthResponse.MemoryInfo memoryInfo = SystemHealthResponse.MemoryInfo.builder()
                .totalMemoryMB(totalMemory / (1024 * 1024))
                .freeMemoryMB(freeMemory / (1024 * 1024))
                .usedMemoryMB(usedMemory / (1024 * 1024))
                .maxMemoryMB(maxMemory / (1024 * 1024))
                .usagePercentage(Math.round((double) usedMemory / totalMemory * 100.0 * 10.0) / 10.0)
                .build();
        
        // Database info
        SystemHealthResponse.DatabaseInfo dbInfo;
        try (Connection conn = dataSource.getConnection()) {
            long start = System.currentTimeMillis();
            conn.createStatement().execute("SELECT 1");
            long responseTime = System.currentTimeMillis() - start;
            
            dbInfo = SystemHealthResponse.DatabaseInfo.builder()
                    .status("UP")
                    .responseTimeMs(responseTime)
                    .databaseName(conn.getCatalog())
                    .build();
        } catch (Exception e) {
            dbInfo = SystemHealthResponse.DatabaseInfo.builder()
                    .status("DOWN")
                    .responseTimeMs(-1)
                    .databaseName("Unknown")
                    .build();
        }
        
        // Build response
        SystemHealthResponse response = SystemHealthResponse.builder()
                .status("UP")
                .timestamp(System.currentTimeMillis())
                .system(systemInfo)
                .memory(memoryInfo)
                .database(dbInfo)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, "System health retrieved successfully"));
    }
}