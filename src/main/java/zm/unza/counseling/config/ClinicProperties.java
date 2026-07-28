package zm.unza.counseling.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Base URL of the university clinic system, used by {@link zm.unza.counseling.service.ClinicAlertSyncService}
 * to push SecurityAlert records created on this side over to the clinic system.
 *
 * Bound from CLINIC_SYSTEM_URL via application.yml (app.clinic.base-url).
 */
@Configuration
@ConfigurationProperties(prefix = "app.clinic")
public class ClinicProperties {

    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank();
    }
}
