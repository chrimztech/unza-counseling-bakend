package zm.unza.counseling.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import zm.unza.counseling.security.JwtAuthenticationEntryPoint;
import zm.unza.counseling.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

            // Disable CSRF because this is a stateless REST API
            .csrf(AbstractHttpConfigurer::disable)

            // Enable CORS using our CorsConfig
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // Handle unauthorized requests
            .exceptionHandling(exception ->
                exception.authenticationEntryPoint(unauthorizedHandler)
            )

            // Stateless session (JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // Allow Spring error dispatch
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                // Allow CORS preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 🔓 PUBLIC AUTH ENDPOINTS (LOGIN)
                .requestMatchers(
                        "/auth/**",
                        "/api/auth/**",
                        "/v1/auth/**",
                        "/api/v1/auth/**"
                ).permitAll()

                 // 🔓 Public appointment endpoints
                 .requestMatchers(
                         "/api/appointments/stats",
                         "/appointments/stats",
                         "/api/appointments/availability",
                         "/appointments/availability"
                 ).permitAll()

                 // 🔓 Public counselor/user list endpoints
                .requestMatchers(
                        "/api/counselors/**",
                        "/counselors/**",
                        "/api/v1/counselors/**",
                        "/v1/counselors/**",
                        "/api/users/**",
                        "/users/**",
                        "/api/v1/users/**",
                        "/v1/users/**",
                        "/api/clients/**",
                        "/clients/**",
                        "/api/v1/clients/**",
                        "/v1/clients/**"
                ).permitAll()

                // 🔓 Swagger / documentation
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**"
                ).permitAll()

                // 🔓 Health check
                .requestMatchers(
                        "/actuator/**",
                        "/health"
                ).permitAll()

                // 🔐 Everything else requires authentication
                .anyRequest().authenticated()
            );

        // JWT Authentication Filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Authentication provider
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
