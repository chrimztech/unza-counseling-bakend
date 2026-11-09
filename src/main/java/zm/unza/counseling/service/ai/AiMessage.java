package zm.unza.counseling.service.ai;

/** One turn in an LLM conversation. {@code role} is "user" or "assistant". */
public record AiMessage(String role, String content) {
    public static AiMessage user(String content) { return new AiMessage("user", content); }
    public static AiMessage assistant(String content) { return new AiMessage("assistant", content); }
}
