package zm.unza.counseling.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.AiProviderConfigRequest;
import zm.unza.counseling.entity.AiProviderConfig;
import zm.unza.counseling.entity.AiProviderType;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.AiProviderConfigRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiProviderAdminService {

    private final AiProviderConfigRepository repository;
    private final AiKeyEncryptor encryptor;
    private final AiGatewayService gateway;
    private final LlmClient llmClient;

    public List<AiProviderConfig> list() {
        return repository.findAllByOrderByIsDefaultDescPriorityAscIdAsc();
    }

    public AiProviderConfig get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI provider not found: " + id));
    }

    @Transactional
    public AiProviderConfig create(AiProviderConfigRequest req, String actor) {
        AiProviderConfig cfg = new AiProviderConfig();
        apply(cfg, req);
        if (isBlank(req.getApiKey()) && cfg.getProviderType().isApiKeyRequired()) {
            throw new ValidationException(cfg.getProviderType().getDisplayName() + " requires an API key");
        }
        // The first provider added becomes the default so the assistant works immediately.
        if (repository.count() == 0) cfg.setIsDefault(true);
        cfg.setUpdatedBy(actor);
        AiProviderConfig saved = repository.save(cfg);
        if (Boolean.TRUE.equals(saved.getIsDefault())) repository.clearDefaultExcept(saved.getId());
        return saved;
    }

    @Transactional
    public AiProviderConfig update(Long id, AiProviderConfigRequest req, String actor) {
        AiProviderConfig cfg = get(id);
        apply(cfg, req);
        if (cfg.getProviderType().isApiKeyRequired() && isBlank(cfg.getEncryptedApiKey())) {
            throw new ValidationException(cfg.getProviderType().getDisplayName() + " requires an API key");
        }
        cfg.setUpdatedBy(actor);
        AiProviderConfig saved = repository.save(cfg);
        if (Boolean.TRUE.equals(saved.getIsDefault())) repository.clearDefaultExcept(saved.getId());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        AiProviderConfig cfg = get(id);
        boolean wasDefault = Boolean.TRUE.equals(cfg.getIsDefault());
        repository.delete(cfg);
        repository.flush();
        if (wasDefault) promoteNextDefault();
    }

    @Transactional
    public AiProviderConfig makeDefault(Long id, String actor) {
        AiProviderConfig cfg = get(id);
        cfg.setIsDefault(true);
        cfg.setEnabled(true);
        cfg.setUpdatedBy(actor);
        AiProviderConfig saved = repository.save(cfg);
        repository.clearDefaultExcept(saved.getId());
        return saved;
    }

    /**
     * Sends a tiny prompt through this one provider and stores the outcome on the row.
     * Not @Transactional on purpose: the provider call can take up to the timeout and we don't
     * want to hold a DB connection/transaction open for it.
     */
    public Map<String, Object> test(Long id) {
        AiProviderConfig cfg = get(id);
        cfg.setLastTestedAt(LocalDateTime.now());
        try {
            AiResult r = gateway.testProvider(cfg);
            cfg.setLastTestSuccess(true);
            cfg.setLastTestMessage("Connected in " + r.latencyMs() + " ms. Reply: " + abbreviate(r.content(), 120));
            repository.save(cfg);
            return Map.of("success", true, "latencyMs", r.latencyMs(), "reply", r.content(), "model", cfg.getModel());
        } catch (LlmClient.LlmException | IllegalStateException e) {
            cfg.setLastTestSuccess(false);
            cfg.setLastTestMessage(abbreviate(e.getMessage(), 1000));
            repository.save(cfg);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * Lists models for either a saved provider ({@code id} set) or an unsaved draft from the
     * add-provider form (type + baseUrl + apiKey), so the admin can pick a model before saving.
     * A key typed in the form takes precedence over the stored one.
     */
    public List<String> models(Long id, AiProviderType type, String baseUrl, String apiKey) {
        AiProviderConfig cfg;
        String key;
        if (id != null) {
            cfg = get(id);
            key = !isBlank(apiKey) ? apiKey : encryptor.decrypt(cfg.getEncryptedApiKey());
            if (type != null) cfg.setProviderType(type);
            if (!isBlank(baseUrl)) cfg.setBaseUrl(baseUrl);
        } else {
            if (type == null) throw new ValidationException("providerType is required");
            cfg = new AiProviderConfig();
            cfg.setProviderType(type);
            cfg.setBaseUrl(baseUrl);
            key = apiKey;
        }
        try {
            return llmClient.listModels(cfg, key);
        } catch (LlmClient.LlmException e) {
            throw new ValidationException("Could not fetch models: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------------------------------

    private void apply(AiProviderConfig cfg, AiProviderConfigRequest req) {
        cfg.setName(req.getName().trim());
        cfg.setProviderType(req.getProviderType());
        cfg.setBaseUrl(isBlank(req.getBaseUrl()) ? null : req.getBaseUrl().trim());
        if (cfg.getProviderType() == AiProviderType.CUSTOM && cfg.getBaseUrl() == null) {
            throw new ValidationException("A base URL is required for a custom provider");
        }
        if (cfg.getBaseUrl() != null && !cfg.getBaseUrl().matches("(?i)^https?://.+")) {
            throw new ValidationException("Base URL must start with http:// or https://");
        }
        cfg.setModel(req.getModel().trim());
        cfg.setTemperature(req.getTemperature());
        if (req.getMaxTokens() != null) cfg.setMaxTokens(req.getMaxTokens());
        if (req.getTimeoutSeconds() != null) cfg.setTimeoutSeconds(req.getTimeoutSeconds());
        if (req.getEnabled() != null) cfg.setEnabled(req.getEnabled());
        if (req.getIsDefault() != null) cfg.setIsDefault(req.getIsDefault());
        if (req.getPriority() != null) cfg.setPriority(req.getPriority());

        if (Boolean.TRUE.equals(req.getClearApiKey())) {
            cfg.setEncryptedApiKey(null);
            cfg.setApiKeyHint(null);
        } else if (!isBlank(req.getApiKey())) {
            String key = req.getApiKey().trim();
            cfg.setEncryptedApiKey(encryptor.encrypt(key));
            cfg.setApiKeyHint(AiKeyEncryptor.hint(key));
        }
        // A disabled provider can't be the one tried first.
        if (Boolean.FALSE.equals(cfg.getEnabled())) cfg.setIsDefault(false);
    }

    private void promoteNextDefault() {
        repository.findByEnabledTrueOrderByIsDefaultDescPriorityAscIdAsc().stream().findFirst().ifPresent(next -> {
            next.setIsDefault(true);
            repository.save(next);
        });
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String abbreviate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
