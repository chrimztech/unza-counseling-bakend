package zm.unza.counseling.config.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.regex.Pattern;

/**
 * API Versioning Configuration for Enterprise-grade API management.
 * Supports multiple versioning strategies (URL path, header, media type).
 */
@Configuration
public class ApiVersioningConfig implements WebMvcConfigurer {

    /**
     * API Version mapping configuration.
     * Maps API versions to their respective paths and configurations.
     */
    public static final Pattern API_VERSION_PATTERN = Pattern.compile("^/api/v(\\d+)(/.*)?$");
    
    /**
     * Current active API version
     */
    public static final String CURRENT_VERSION = "v1";
    
    /**
     * Supported API versions
     */
    public static final String[] SUPPORTED_VERSIONS = {"v1", "v2"};

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiVersionInterceptor())
                .addPathPatterns("/api/**");
    }

    /**
     * Validate API version format.
     */
    public static boolean isValidApiVersion(String version) {
        if (version == null) return false;
        return Pattern.matches("^v\\d+$", version);
    }

    /**
     * Extract version from API path.
     */
    public static String extractVersionFromPath(String path) {
        if (path == null) return CURRENT_VERSION;
        
        var matcher = API_VERSION_PATTERN.matcher(path);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        
        return CURRENT_VERSION;
    }

    /**
     * Check if API version is supported.
     */
    public static boolean isSupportedVersion(String version) {
        if (version == null) return false;
        
        for (String supported : SUPPORTED_VERSIONS) {
            if (supported.equals(version)) {
                return true;
            }
        }
        return false;
    }
}
