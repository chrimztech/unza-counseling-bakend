package zm.unza.counseling.security.external;

import lombok.Data;
import zm.unza.counseling.entity.User;

/**
 * Response object for external authentication results
 */
@Data
public class ExternalAuthResponse {
    private boolean authenticated;
    private String message;
    private User user;
    private String externalId; // ID from the external system (student ID, employee ID, etc.)
    private String externalSystem; // Which system this came from (SIS, HR)

    public ExternalAuthResponse() {}

    public ExternalAuthResponse(boolean authenticated, String message) {
        this.authenticated = authenticated;
        this.message = message;
    }

    public ExternalAuthResponse(boolean authenticated, String message, User user, String externalId, String externalSystem) {
        this.authenticated = authenticated;
        this.message = message;
        this.user = user;
        this.externalId = externalId;
        this.externalSystem = externalSystem;
    }

    // Explicit getters and setters to ensure they work
    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalSystem() {
        return externalSystem;
    }

    public void setExternalSystem(String externalSystem) {
        this.externalSystem = externalSystem;
    }
}