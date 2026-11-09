package zm.unza.counseling.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import zm.unza.counseling.entity.AiProviderConfig;
import zm.unza.counseling.entity.AiProviderType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin HTTP client for the two LLM wire formats we support.
 *
 * Deliberately uses the JDK HttpClient with the default trust store rather than the app's
 * shared RestTemplate bean, which trusts all certificates (for the internal UNZA APIs) — that
 * would be unsafe for requests carrying third-party API keys.
 */
@Component
public class LlmClient {

    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final ObjectMapper mapper;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public LlmClient(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /** Runs one completion. {@code apiKey} is the decrypted key (may be null for keyless providers). */
    public String complete(AiProviderConfig cfg, String apiKey, String systemPrompt,
                           List<AiMessage> messages, Integer maxTokensOverride) throws LlmException {
        String baseUrl = requireBaseUrl(cfg);
        int maxTokens = maxTokensOverride != null ? maxTokensOverride
                : (cfg.getMaxTokens() != null ? cfg.getMaxTokens() : 1024);
        Duration timeout = Duration.ofSeconds(cfg.getTimeoutSeconds() != null ? cfg.getTimeoutSeconds() : 60);

        return cfg.getProviderType().getApiStyle() == AiProviderType.ApiStyle.ANTHROPIC
                ? anthropic(cfg, baseUrl, apiKey, systemPrompt, messages, maxTokens, timeout)
                : openAiCompatible(cfg, baseUrl, apiKey, systemPrompt, messages, maxTokens, timeout);
    }

    /** Lists model ids the provider exposes for this key. */
    public List<String> listModels(AiProviderConfig cfg, String apiKey) throws LlmException {
        String baseUrl = requireBaseUrl(cfg);
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(baseUrl + "/models"))
                .timeout(Duration.ofSeconds(20)).GET();
        applyAuth(cfg, req, apiKey);
        JsonNode body = send(req.build());
        List<String> ids = new ArrayList<>();
        for (JsonNode m : body.path("data")) {
            String id = m.path("id").asText(null);
            if (id != null) ids.add(id.startsWith("models/") ? id.substring("models/".length()) : id);
        }
        ids.sort(String::compareTo);
        return ids;
    }

    // ---------------------------------------------------------------- OpenAI-compatible

    private String openAiCompatible(AiProviderConfig cfg, String baseUrl, String apiKey, String systemPrompt,
                                    List<AiMessage> messages, int maxTokens, Duration timeout) throws LlmException {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", cfg.getModel());
        ArrayNode msgs = body.putArray("messages");
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            msgs.addObject().put("role", "system").put("content", systemPrompt);
        }
        for (AiMessage m : messages) {
            msgs.addObject().put("role", m.role()).put("content", m.content());
        }
        // OpenAI's newer (reasoning) models reject max_tokens in favour of max_completion_tokens;
        // most other OpenAI-compatible vendors still only understand max_tokens.
        body.put(cfg.getProviderType() == AiProviderType.OPENAI ? "max_completion_tokens" : "max_tokens", maxTokens);
        if (cfg.getTemperature() != null) body.put("temperature", cfg.getTemperature());

        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(baseUrl + "/chat/completions"))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
        applyAuth(cfg, req, apiKey);
        if (cfg.getProviderType() == AiProviderType.OPENROUTER) {
            req.header("X-Title", "UNZA Counseling System");
        }

        JsonNode resp = send(req.build());
        String text = resp.path("choices").path(0).path("message").path("content").asText("");
        if (text.isBlank()) {
            String reason = resp.path("choices").path(0).path("finish_reason").asText("unknown");
            throw new LlmException("Provider returned an empty reply (finish_reason=" + reason + ")");
        }
        return text.trim();
    }

    // ---------------------------------------------------------------- Anthropic Messages API

    private String anthropic(AiProviderConfig cfg, String baseUrl, String apiKey, String systemPrompt,
                             List<AiMessage> messages, int maxTokens, Duration timeout) throws LlmException {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", cfg.getModel());
        body.put("max_tokens", maxTokens);
        if (systemPrompt != null && !systemPrompt.isBlank()) body.put("system", systemPrompt);
        if (cfg.getTemperature() != null) body.put("temperature", cfg.getTemperature());
        ArrayNode msgs = body.putArray("messages");
        for (AiMessage m : normaliseForAnthropic(messages)) {
            msgs.addObject().put("role", m.role()).put("content", m.content());
        }

        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(baseUrl + "/messages"))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
        applyAuth(cfg, req, apiKey);

        JsonNode resp = send(req.build());
        StringBuilder sb = new StringBuilder();
        for (JsonNode block : resp.path("content")) {
            if ("text".equals(block.path("type").asText())) sb.append(block.path("text").asText());
        }
        if (sb.isEmpty()) {
            throw new LlmException("Provider returned an empty reply (stop_reason="
                    + resp.path("stop_reason").asText("unknown") + ")");
        }
        return sb.toString().trim();
    }

    /** Anthropic requires the first turn to be "user" and roles to alternate. */
    private static List<AiMessage> normaliseForAnthropic(List<AiMessage> in) {
        List<AiMessage> out = new ArrayList<>();
        for (AiMessage m : in) {
            if (m.content() == null || m.content().isBlank()) continue;
            if (out.isEmpty() && !"user".equals(m.role())) continue;
            if (!out.isEmpty() && out.get(out.size() - 1).role().equals(m.role())) {
                AiMessage prev = out.remove(out.size() - 1);
                out.add(new AiMessage(prev.role(), prev.content() + "\n\n" + m.content()));
            } else {
                out.add(m);
            }
        }
        return out;
    }

    // ---------------------------------------------------------------- plumbing

    private void applyAuth(AiProviderConfig cfg, HttpRequest.Builder req, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) return;
        if (cfg.getProviderType().getApiStyle() == AiProviderType.ApiStyle.ANTHROPIC) {
            req.header("x-api-key", apiKey);
            req.header("anthropic-version", ANTHROPIC_VERSION);
        } else {
            req.header("Authorization", "Bearer " + apiKey);
        }
    }

    private JsonNode send(HttpRequest request) throws LlmException {
        HttpResponse<String> resp;
        try {
            resp = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (java.net.http.HttpTimeoutException e) {
            throw new LlmException("Request to provider timed out");
        } catch (java.io.IOException e) {
            throw new LlmException("Could not reach provider: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmException("Request interrupted");
        }

        JsonNode json;
        try {
            json = resp.body() == null || resp.body().isBlank()
                    ? mapper.createObjectNode() : mapper.readTree(resp.body());
        } catch (Exception e) {
            json = null;
        }

        if (resp.statusCode() / 100 != 2) {
            String detail = null;
            if (json != null) {
                JsonNode err = json.path("error");
                detail = err.isTextual() ? err.asText() : err.path("message").asText(null);
                if (detail == null) detail = json.path("message").asText(null);
            }
            if (detail == null) detail = truncate(resp.body(), 300);
            throw new LlmException("HTTP " + resp.statusCode() + ": " + truncate(detail, 500));
        }
        if (json == null) throw new LlmException("Provider returned a non-JSON response");
        return json;
    }

    private static String requireBaseUrl(AiProviderConfig cfg) throws LlmException {
        String url = cfg.resolveBaseUrl();
        if (url == null) throw new LlmException("No base URL configured for this provider");
        return url;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    /** A provider call failed. Message is safe to show to an admin (never contains the key). */
    public static class LlmException extends Exception {
        public LlmException(String message) { super(message); }
    }
}
