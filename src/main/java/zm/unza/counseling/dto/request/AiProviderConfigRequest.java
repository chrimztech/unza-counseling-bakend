package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import zm.unza.counseling.entity.AiProviderType;

/**
 * Create/update payload for an AI provider. On update, a null or blank {@code apiKey}
 * keeps the stored key; send {@code clearApiKey=true} to remove it.
 */
@Data
public class AiProviderConfigRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private AiProviderType providerType;

    @Size(max = 500)
    private String baseUrl;

    @Size(max = 500)
    private String apiKey;

    private Boolean clearApiKey;

    @NotBlank
    @Size(max = 150)
    private String model;

    @DecimalMin("0.0")
    @DecimalMax("2.0")
    private Double temperature;

    @Min(16)
    @Max(32000)
    private Integer maxTokens;

    @Min(5)
    @Max(300)
    private Integer timeoutSeconds;

    private Boolean enabled;

    private Boolean isDefault;

    @Min(0)
    @Max(10000)
    private Integer priority;
}
