package zm.unza.counseling.entity;

import java.util.List;

/**
 * Catalogue of supported LLM providers.
 *
 * Most vendors expose an OpenAI-compatible {@code /chat/completions} API, so they share one
 * wire format ({@link ApiStyle#OPENAI_COMPATIBLE}); only Anthropic needs its own client.
 * {@code suggestedModels} only pre-fill the UI — the super admin can type any model id or use
 * "Fetch models" to pull the live list from the provider, so this list never has to be current.
 */
public enum AiProviderType {

    OPENAI("OpenAI", ApiStyle.OPENAI_COMPATIBLE, "https://api.openai.com/v1", true,
            List.of("gpt-4o-mini", "gpt-4o", "gpt-4.1-mini")),
    ANTHROPIC("Anthropic (Claude)", ApiStyle.ANTHROPIC, "https://api.anthropic.com/v1", true,
            List.of("claude-sonnet-5-5", "claude-haiku-5-5", "claude-opus-5-5")),
    GROQ("Groq", ApiStyle.OPENAI_COMPATIBLE, "https://api.groq.com/openai/v1", true,
            List.of("llama-3.3-70b-versatile", "llama-3.1-8b-instant")),
    XAI("xAI (Grok)", ApiStyle.OPENAI_COMPATIBLE, "https://api.x.ai/v1", true,
            List.of("grok-3-mini", "grok-3")),
    GOOGLE_GEMINI("Google Gemini", ApiStyle.OPENAI_COMPATIBLE,
            "https://generativelanguage.googleapis.com/v1beta/openai", true,
            List.of("gemini-2.5-flash", "gemini-2.5-pro")),
    MISTRAL("Mistral AI", ApiStyle.OPENAI_COMPATIBLE, "https://api.mistral.ai/v1", true,
            List.of("mistral-small-latest", "mistral-large-latest")),
    DEEPSEEK("DeepSeek", ApiStyle.OPENAI_COMPATIBLE, "https://api.deepseek.com/v1", true,
            List.of("deepseek-chat")),
    OPENROUTER("OpenRouter", ApiStyle.OPENAI_COMPATIBLE, "https://openrouter.ai/api/v1", true,
            List.of("openrouter/auto")),
    OLLAMA("Ollama (self-hosted)", ApiStyle.OPENAI_COMPATIBLE, "http://localhost:11434/v1", false,
            List.of("llama3.1")),
    CUSTOM("Custom (OpenAI-compatible)", ApiStyle.OPENAI_COMPATIBLE, null, false,
            List.of());

    public enum ApiStyle { OPENAI_COMPATIBLE, ANTHROPIC }

    private final String displayName;
    private final ApiStyle apiStyle;
    private final String defaultBaseUrl;
    private final boolean apiKeyRequired;
    private final List<String> suggestedModels;

    AiProviderType(String displayName, ApiStyle apiStyle, String defaultBaseUrl,
                   boolean apiKeyRequired, List<String> suggestedModels) {
        this.displayName = displayName;
        this.apiStyle = apiStyle;
        this.defaultBaseUrl = defaultBaseUrl;
        this.apiKeyRequired = apiKeyRequired;
        this.suggestedModels = suggestedModels;
    }

    public String getDisplayName() { return displayName; }
    public ApiStyle getApiStyle() { return apiStyle; }
    public String getDefaultBaseUrl() { return defaultBaseUrl; }
    public boolean isApiKeyRequired() { return apiKeyRequired; }
    public List<String> getSuggestedModels() { return suggestedModels; }
}
