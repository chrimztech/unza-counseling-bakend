package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * An LLM provider connection configured by a super admin.
 *
 * The API key is stored AES-GCM encrypted ({@code encryptedApiKey}) and is never serialised
 * back to clients — controllers expose {@code AiProviderConfigResponse}, which only carries a
 * masked hint (last 4 characters).
 */
@Entity
@Table(name = "ai_provider_configs")
@EntityListeners(AuditingEntityListener.class)
@Data
public class AiProviderConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 40)
    private AiProviderType providerType;

    /** Null means "use the provider type's default base URL". */
    @Column(name = "base_url", length = 500)
    private String baseUrl;

    @ToString.Exclude
    @Column(name = "encrypted_api_key", columnDefinition = "TEXT")
    private String encryptedApiKey;

    /** Last 4 characters of the plaintext key, for display only. */
    @Column(name = "api_key_hint", length = 8)
    private String apiKeyHint;

    @Column(nullable = false, length = 150)
    private String model;

    /** Optional; omitted from requests when null (some reasoning models reject it). */
    private Double temperature;

    @Column(name = "max_tokens")
    private Integer maxTokens = 1024;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 60;

    @Column(nullable = false)
    private Boolean enabled = true;

    /** The provider tried first. At most one row is default. */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /** Fallback order after the default provider: lower runs first. */
    @Column(nullable = false)
    private Integer priority = 100;

    @Column(name = "total_requests", nullable = false)
    private Long totalRequests = 0L;

    @Column(name = "failed_requests", nullable = false)
    private Long failedRequests = 0L;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "last_tested_at")
    private LocalDateTime lastTestedAt;

    @Column(name = "last_test_success")
    private Boolean lastTestSuccess;

    @Column(name = "last_test_message", length = 1000)
    private String lastTestMessage;

    @Column(name = "updated_by", length = 150)
    private String updatedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String resolveBaseUrl() {
        String url = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : providerType.getDefaultBaseUrl();
        if (url == null) return null;
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
