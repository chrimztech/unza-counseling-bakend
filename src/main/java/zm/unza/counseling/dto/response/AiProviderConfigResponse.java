package zm.unza.counseling.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import zm.unza.counseling.entity.AiProviderConfig;
import zm.unza.counseling.entity.AiProviderType;

import java.time.LocalDateTime;

/** Admin view of a provider. Never carries the API key — only whether one is set and its last 4 chars. */
@Data
@Builder
public class AiProviderConfigResponse {
    private Long id;
    private String name;
    private AiProviderType providerType;
    private String providerDisplayName;
    private String baseUrl;
    private String effectiveBaseUrl;
    private boolean hasApiKey;
    private String apiKeyHint;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Integer timeoutSeconds;
    private boolean enabled;
    @JsonProperty("isDefault")
    private boolean isDefault;
    private Integer priority;
    private long totalRequests;
    private long failedRequests;
    private LocalDateTime lastUsedAt;
    private String lastError;
    private LocalDateTime lastTestedAt;
    private Boolean lastTestSuccess;
    private String lastTestMessage;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AiProviderConfigResponse from(AiProviderConfig c) {
        return AiProviderConfigResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .providerType(c.getProviderType())
                .providerDisplayName(c.getProviderType().getDisplayName())
                .baseUrl(c.getBaseUrl())
                .effectiveBaseUrl(c.resolveBaseUrl())
                .hasApiKey(c.getEncryptedApiKey() != null && !c.getEncryptedApiKey().isBlank())
                .apiKeyHint(c.getApiKeyHint())
                .model(c.getModel())
                .temperature(c.getTemperature())
                .maxTokens(c.getMaxTokens())
                .timeoutSeconds(c.getTimeoutSeconds())
                .enabled(Boolean.TRUE.equals(c.getEnabled()))
                .isDefault(Boolean.TRUE.equals(c.getIsDefault()))
                .priority(c.getPriority())
                .totalRequests(c.getTotalRequests() == null ? 0 : c.getTotalRequests())
                .failedRequests(c.getFailedRequests() == null ? 0 : c.getFailedRequests())
                .lastUsedAt(c.getLastUsedAt())
                .lastError(c.getLastError())
                .lastTestedAt(c.getLastTestedAt())
                .lastTestSuccess(c.getLastTestSuccess())
                .lastTestMessage(c.getLastTestMessage())
                .updatedBy(c.getUpdatedBy())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
