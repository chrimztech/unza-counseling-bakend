package zm.unza.counseling.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zm.unza.counseling.entity.AiProviderConfig;
import zm.unza.counseling.repository.AiProviderConfigRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Single entry point for every AI feature in the app.
 *
 * Tries the default provider first, then the remaining enabled providers in priority order,
 * so one vendor's outage, rate limit or expired key doesn't take the assistant down.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiGatewayService {

    private final AiProviderConfigRepository repository;
    private final AiKeyEncryptor encryptor;
    private final LlmClient llmClient;

    public boolean isAvailable() {
        return repository.existsByEnabledTrue();
    }

    public AiResult chat(String systemPrompt, List<AiMessage> messages) {
        return chat(systemPrompt, messages, null);
    }

    public AiResult chat(String systemPrompt, List<AiMessage> messages, Integer maxTokens) {
        List<AiProviderConfig> chain = repository.findByEnabledTrueOrderByIsDefaultDescPriorityAscIdAsc();
        if (chain.isEmpty()) {
            throw new AiUnavailableException("No AI provider is configured. A super admin can add one under Admin → AI Providers.");
        }

        List<String> failures = new ArrayList<>();
        for (AiProviderConfig cfg : chain) {
            long start = System.currentTimeMillis();
            try {
                String key = encryptor.decrypt(cfg.getEncryptedApiKey());
                String text = llmClient.complete(cfg, key, systemPrompt, messages, maxTokens);
                long latency = System.currentTimeMillis() - start;
                repository.recordSuccess(cfg.getId(), LocalDateTime.now());
                return new AiResult(text, cfg.getId(), cfg.getName(), cfg.getModel(), latency);
            } catch (LlmClient.LlmException | IllegalStateException e) {
                String msg = e.getMessage();
                log.warn("AI provider '{}' ({}) failed, trying next: {}", cfg.getName(), cfg.getProviderType(), msg);
                repository.recordFailure(cfg.getId(), truncate(msg), LocalDateTime.now());
                failures.add(cfg.getName() + ": " + msg);
            }
        }
        throw new AiUnavailableException("All AI providers failed — " + String.join(" | ", failures));
    }

    /** Runs one provider directly (no fallback). Used by the admin "Test connection" button. */
    public AiResult testProvider(AiProviderConfig cfg) throws LlmClient.LlmException {
        long start = System.currentTimeMillis();
        String key = encryptor.decrypt(cfg.getEncryptedApiKey());
        String text = llmClient.complete(cfg, key,
                "You are a connectivity check. Reply with exactly: OK",
                // Generous cap: reasoning models spend tokens thinking before the visible reply,
                // and a tiny limit makes a healthy provider look broken.
                List.of(AiMessage.user("Reply with OK.")), 512);
        return new AiResult(text, cfg.getId(), cfg.getName(), cfg.getModel(), System.currentTimeMillis() - start);
    }

    private static String truncate(String s) {
        if (s == null) return null;
        return s.length() <= 1000 ? s : s.substring(0, 1000);
    }
}
