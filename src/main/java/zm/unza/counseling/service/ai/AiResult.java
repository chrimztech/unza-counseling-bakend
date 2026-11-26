package zm.unza.counseling.service.ai;

/**
 * A successful completion, plus which provider/model actually produced it. {@code attempt} is the
 * position of that provider in the fallback chain (1 = the default provider answered; 2+ = earlier
 * providers failed and a fallback answered).
 */
public record AiResult(String content, Long providerId, String providerName, String model, long latencyMs, int attempt) {

    public AiResult(String content, Long providerId, String providerName, String model, long latencyMs) {
        this(content, providerId, providerName, model, latencyMs, 1);
    }

    public boolean usedFallback() { return attempt > 1; }
}
