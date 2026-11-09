package zm.unza.counseling.service.ai;

/** No enabled provider is configured, or every provider in the fallback chain failed. */
public class AiUnavailableException extends RuntimeException {
    public AiUnavailableException(String message) {
        super(message);
    }
}
