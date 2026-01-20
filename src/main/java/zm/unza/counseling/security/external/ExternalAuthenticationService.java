package zm.unza.counseling.security.external;

import zm.unza.counseling.entity.User;

/**
 * Interface for external authentication services
 * Common interface for SIS and HR system integrations
 */
public interface ExternalAuthenticationService {
    
    /**
     * Authenticate user against external system
     * @param username username or identifier
     * @param password password
     * @return ExternalAuthResponse containing user details and authentication status
     * @throws ExternalAuthenticationException if authentication fails
     */
    ExternalAuthResponse authenticate(String username, String password) throws ExternalAuthenticationException;
    
    /**
     * Validate if user exists in external system
     * @param username username or identifier
     * @return true if user exists, false otherwise
     */
    boolean validateUserExists(String username);
    
    /**
     * Get user details from external system
     * @param username username or identifier
     * @return User object with details from external system
     * @throws ExternalAuthenticationException if user details cannot be retrieved
     */
    User getUserDetails(String username) throws ExternalAuthenticationException;
}