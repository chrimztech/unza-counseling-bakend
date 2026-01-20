package zm.unza.counseling.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration("jwtOpenApiConfig")
@OpenAPIDefinition(
        info = @Info(
                title = "UNZA Counseling Management System API",
                version = "1.0",
                description = "API documentation for the UNZA Counseling Management System",
                contact = @Contact(
                        name = "UNZA IT Support",
                        email = "support@unza.zm"
                )
        )
)
@SecurityScheme(
        name = "bearer-jwt",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SecurityOpenApiConfig {
}