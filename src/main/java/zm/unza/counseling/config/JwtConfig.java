package zm.unza.counseling.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    private String secret;
    private long expiration;
    private long refreshExpiration;

    public String getSecret() { return secret; }
    public long getExpiration() { return expiration; }
    public long getRefreshExpiration() { return refreshExpiration; }
}