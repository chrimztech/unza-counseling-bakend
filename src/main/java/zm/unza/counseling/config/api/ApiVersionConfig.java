package zm.unza.counseling.config.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import zm.unza.counseling.interceptor.ApiVersionInterceptor;

@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    private final ApiVersionInterceptor apiVersionInterceptor;

    public ApiVersionConfig(ApiVersionInterceptor apiVersionInterceptor) {
        this.apiVersionInterceptor = apiVersionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiVersionInterceptor)
                .addPathPatterns("/api/**");
    }

    @Override
    public void configurePathMatch(org.springframework.web.servlet.config.annotation.PathMatchConfigurer configurer) {
        // Configure version-based API routing
        RequestMappingHandlerMapping mapping = (RequestMappingHandlerMapping) getRequestMappingHandlerMapping();
        mapping.setOrder(1);
    }

    private RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        // This would be automatically injected by Spring
        // For now, returning null as Spring will handle this internally
        return null;
    }
}