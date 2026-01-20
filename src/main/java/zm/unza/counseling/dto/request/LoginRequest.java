package zm.unza.counseling.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Authentication source is required")
    @Pattern(regexp = "(?i)SIS|HR|INTERNAL",
             message = "Invalid authentication source. Allowed values are SIS, HR, INTERNAL")
    @JsonProperty("authenticationSource")
    private String authenticationSource;

    // Explicit getters and setters to ensure they work
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthenticationSource() {
        return authenticationSource;
    }

    public void setAuthenticationSource(String authenticationSource) {
        this.authenticationSource = authenticationSource;
    }
}