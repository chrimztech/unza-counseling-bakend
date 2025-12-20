package zm.unza.counseling.config.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Enterprise-grade OpenAPI configuration with comprehensive documentation
 * Provides detailed API documentation with security schemes and versioning
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        developmentServer(),
                        stagingServer(),
                        productionServer()
                ))
                .tags(List.of(
                    new Tag().name("Authentication").description("User authentication and authorization"),
                    new Tag().name("Users").description("User management operations"),
                    new Tag().name("Clients").description("Client management operations"),
                    new Tag().name("Counselors").description("Counselor management operations"),
                    new Tag().name("Appointments").description("Appointment scheduling and management"),
                    new Tag().name("Sessions").description("Counseling session management"),
                    new Tag().name("Assessments").description("Risk and self-assessment operations"),
                    new Tag().name("Academic Performance").description("Academic performance tracking"),
                    new Tag().name("Notifications").description("Notification and messaging system"),
                    new Tag().name("Resources").description("Educational resources and materials"),
                    new Tag().name("Analytics").description("Business intelligence and reporting"),
                    new Tag().name("Administration").description("System administration operations")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", 
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")
                                    .description("JWT Authorization header using the Bearer scheme. Enter 'Bearer' [space] and then your token in the text input below.")
                        )
                        .addSecuritySchemes("API Key",
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.APIKEY)
                                    .in(SecurityScheme.In.HEADER)
                                    .name("X-API-Key")
                                    .description("API key for system-to-system authentication")
                        )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("UNZA Counseling Management System API")
                .description("""
                    Comprehensive API for managing university counseling services, appointments, and academic performance analysis.
                    
                    ## Features
                    
                    * **User Management**: Complete user registration, authentication, and profile management
                    * **Client Management**: Student client records and counseling history
                    * **Counselor Management**: Counselor profiles, availability, and scheduling
                    * **Appointment System**: Advanced scheduling with conflict detection and reminders
                    * **Assessment Tools**: Risk assessment and self-evaluation questionnaires
                    * **Academic Integration**: Academic performance tracking and analytics
                    * **Notification System**: Real-time notifications via email, SMS, and in-app
                    * **Reporting & Analytics**: Comprehensive reporting and business intelligence
                    
                    ## Authentication
                    
                    The API uses JWT (JSON Web Token) authentication. Include the token in the Authorization header:
                    
                    ```
                    Authorization: Bearer {your-jwt-token}
                    ```
                    
                    ## Rate Limiting
                    
                    API endpoints are rate-limited based on user roles:
                    * Public endpoints: 100 requests per minute
                    * Authenticated users: 1000 requests per minute
                    * Admin users: 5000 requests per minute
                    * Critical operations: 10 requests per minute
                    
                    ## Error Handling
                    
                    All errors follow RFC 7807 problem details specification with consistent error codes and messages.
                    
                    ## API Versioning
                    
                    This API uses URL versioning. Current version is v1. All endpoints are prefixed with `/api/v1/`.
                    """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("UNZA IT Department")
                        .email("it@unza.zm")
                        .url("https://unza.zm/support"))
                .license(new License()
                        .name("UNZA License")
                        .url("https://unza.zm/license"));
    }

    private Server developmentServer() {
        return new Server()
                .url("http://localhost:8080/api/v1")
                .description("Development Server");
    }

    private Server stagingServer() {
        return new Server()
                .url("https://staging-api.unza.zm/api/v1")
                .description("Staging Server");
    }

    private Server productionServer() {
        return new Server()
                .url("https://api.unza.zm/api/v1")
                .description("Production Server");
    }
}