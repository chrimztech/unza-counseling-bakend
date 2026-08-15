package zm.unza.counseling.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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

    // Without an explicit hierarchy, Spring Security treats roles as flat: a
    // @PreAuthorize("hasRole('ADMIN')") check only matches ROLE_ADMIN exactly, so
    // a SUPER_ADMIN-only account (no separate ROLE_ADMIN grant) was silently
    // locked out of every admin-only endpoint across the app. SUPER_ADMIN must
    // imply everything ADMIN can do.
    // Beans are static per Spring Security's guidance, so the role hierarchy is
    // available before the rest of this @Configuration class is initialized.
    @Bean
    static RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_SUPER_ADMIN > ROLE_ADMIN");
        return roleHierarchy;
    }

    @Bean
    static DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }

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

                 // 🔓 Public headline stats (login screen stat cards — no session yet)
                 .requestMatchers(
                         "/api/public/stats",
                         "/public/stats"
                 ).permitAll()

                // NOTE: previously there was a "public counselor availability" permitAll rule
                // here for "/counselors/available" and "/counselors/*/availability". Neither
                // route exists in CounselorController (dead config) and no live frontend flow
                // calls them (the reachable anonymous booking screen never checks per-counselor
                // availability — it submits a preference and staff assign a counselor
                // afterwards). Removed rather than implementing an endpoint nothing calls; see
                // the "Public appointment endpoints" rule above for the availability check that
                // IS actually used (/appointments/availability).

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

                // 🔓 WebSocket handshake (auth happens at the STOMP CONNECT frame level,
                // since WebSocket handshakes can't reliably carry an Authorization header)
                .requestMatchers(
                        "/ws/**",
                        "/api/ws/**"
                ).permitAll()

                // 🔓 Service-to-service webhooks from the clinic system (visit records and
                // SecurityAlert sync). Not user auth — protected instead by the
                // X-Service-Api-Key header checked inside ClinicController
                // (see app.cross-system.api-key). Both webhooks now share the same
                // auth mechanism so the external clinic system only needs one integration.
                .requestMatchers(
                        "/clinic/security-alerts/inbound/**",
                        "/api/clinic/security-alerts/inbound/**",
                        "/clinic/visits/inbound",
                        "/api/clinic/visits/inbound"
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
