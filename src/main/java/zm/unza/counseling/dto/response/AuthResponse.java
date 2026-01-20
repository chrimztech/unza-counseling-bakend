package zm.unza.counseling.dto.response;

import lombok.Data;
import zm.unza.counseling.entity.User;

@Data
public class AuthResponse {
    private String token;
    private String refreshToken;
    private User user;
    private int expiresIn;

    // Explicit getters and setters to ensure they work
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}