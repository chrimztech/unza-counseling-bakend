package zm.unza.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for system health monitoring response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemHealthResponse {
    
    private String status;
    private Long timestamp;
    private SystemInfo system;
    private MemoryInfo memory;
    private DatabaseInfo database;
    private Map<String, String> additionalInfo;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SystemInfo {
        private String osName;
        private String osVersion;
        private String osArchitecture;
        private int availableProcessors;
        private String javaVersion;
        private long uptimeMillis;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemoryInfo {
        private long totalMemoryMB;
        private long freeMemoryMB;
        private long usedMemoryMB;
        private long maxMemoryMB;
        private double usagePercentage;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DatabaseInfo {
        private String status;
        private long responseTimeMs;
        private String databaseName;
    }
}