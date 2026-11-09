package zm.unza.counseling.service.ai;

/** A successful completion, plus which provider/model actually produced it. */
public record AiResult(String content, Long providerId, String providerName, String model, long latencyMs) {}
