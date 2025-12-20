package zm.unza.counseling.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * API Version Interceptor for handling version-aware requests.
 * Extracts and validates API versions from requests.
 */
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final String API_VERSION_HEADER = "X-API-Version";
    private static final String DEFAULT_VERSION = "v1";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String version = extractVersion(request);
        
        // Set version in request attribute for later use
        request.setAttribute("apiVersion", version);
        
        // Add version header to response
        response.setHeader("X-API-Version", version);
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Post-handling logic if needed
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // After completion logic if needed
    }

    /**
     * Extract API version from request.
     * Priority: Header > URL Path > Default
     */
    private String extractVersion(HttpServletRequest request) {
        // Check header first
        String version = request.getHeader(API_VERSION_HEADER);
        if (version != null && !version.trim().isEmpty()) {
            return version;
        }
        
        // Check URL path
        String requestUri = request.getRequestURI();
        if (requestUri != null && requestUri.matches("^/api/v\\d+(/.*)?$")) {
            String[] parts = requestUri.split("/");
            for (String part : parts) {
                if (part.matches("^v\\d+$")) {
                    return part;
                }
            }
        }
        
        return DEFAULT_VERSION;
    }
}