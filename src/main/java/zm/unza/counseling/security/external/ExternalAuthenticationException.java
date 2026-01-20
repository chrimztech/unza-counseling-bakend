package zm.unza.counseling.security.external;

/**
 * Exception thrown when external authentication fails
 */
public class ExternalAuthenticationException extends Exception {
    
    public ExternalAuthenticationException(String message) {
        super(message);
    }
    
    public ExternalAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}