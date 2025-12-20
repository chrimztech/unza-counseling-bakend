package zm.unza.counseling.config.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Enterprise-grade circuit breaker configuration using Resilience4j
 * Provides fault tolerance and resilience patterns for external service calls
 */
@Configuration
@RequiredArgsConstructor
public class CircuitBreakerConfig {

    /**
     * Circuit breaker configuration for different service types
     */
    public static class ServiceCircuitBreakerConfigs {
        
        // Email service - very critical for notifications
        public static final CircuitBreakerConfig EMAIL_SERVICE_CONFIG = CircuitBreakerConfig.custom()
                .failureRateThreshold(50f)                    // Open circuit if 50% failure rate
                .minimumNumberOfCalls(10)                     // Minimum calls before evaluating
                .slidingWindowSize(20)                        // Sliding window of 20 calls
                .permittedNumberOfCallsInHalfOpenState(5)     // Test 5 calls when half-open
                .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30 seconds before half-open
                .build();
        
        // Database operations - high availability required
        public static final CircuitBreakerConfig DATABASE_CONFIG = CircuitBreakerConfig.custom()
                .failureRateThreshold(30f)                    // More sensitive for DB
                .minimumNumberOfCalls(5)
                .slidingWindowSize(15)
                .permittedNumberOfCallsInHalfOpenState(3)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .build();
        
        // External APIs - moderate tolerance
        public static final CircuitBreakerConfig EXTERNAL_API_CONFIG = CircuitBreakerConfig.custom()
                .failureRateThreshold(60f)                    // Higher tolerance for external
                .minimumNumberOfCalls(15)
                .slidingWindowSize(25)
                .permittedNumberOfCallsInHalfOpenState(10)
                .waitDurationInOpenState(Duration.ofSeconds(45))
                .build();
        
        // Authentication services - critical, low tolerance
        public static final CircuitBreakerConfig AUTH_SERVICE_CONFIG = CircuitBreakerConfig.custom()
                .failureRateThreshold(20f)                    // Very low tolerance
                .minimumNumberOfCalls(5)
                .slidingWindowSize(10)
                .permittedNumberOfCallsInHalfOpenState(2)
                .waitDurationInOpenState(Duration.ofSeconds(120)) // Longer wait
                .build();
    }

    /**
     * Retry configuration for different scenarios
     */
    public static class RetryConfigs {
        
        // Quick retry for transient failures
        public static final RetryConfig QUICK_RETRY_CONFIG = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(Exception.class)
                .build();
        
        // Exponential backoff for external APIs
        public static final RetryConfig EXPONENTIAL_RETRY_CONFIG = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofSeconds(1))
                .retryExceptions(Exception.class)
                .build();
        
        // Single retry for critical operations
        public static final RetryConfig CRITICAL_RETRY_CONFIG = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(200))
                .retryExceptions(Exception.class)
                .build();
    }

    /**
     * Time limiter configuration
     */
    public static final TimeLimiterConfig DEFAULT_TIME_LIMITER_CONFIG = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(30))
            .build();

    /**
     * Circuit breaker registry with custom configurations
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        
        // Register custom circuit breakers
        registry.circuitBreaker("emailService", ServiceCircuitBreakerConfigs.EMAIL_SERVICE_CONFIG);
        registry.circuitBreaker("database", ServiceCircuitBreakerConfigs.DATABASE_CONFIG);
        registry.circuitBreaker("externalApi", ServiceCircuitBreakerConfigs.EXTERNAL_API_CONFIG);
        registry.circuitBreaker("authService", ServiceCircuitBreakerConfigs.AUTH_SERVICE_CONFIG);
        
        return registry;
    }

    /**
     * Time limiter for async operations
     */
    @Bean
    public TimeLimiter timeLimiter() {
        return TimeLimiter.of(DEFAULT_TIME_LIMITER_CONFIG);
    }

    /**
     * Scheduled executor for circuit breaker operations
     */
    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(3, r -> {
            Thread thread = new Thread(r, "circuit-breaker-scheduler");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Circuit breaker event listener for monitoring
     */
    @Bean
    public CircuitBreaker.EventPublisher circuitBreakerEventPublisher(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("emailService").getEventPublisher()
                .onStateTransition(event -> {
                    // Log circuit breaker state changes
                    System.out.println("Circuit breaker state transition: " + event.getStateTransition());
                })
                .onFailureRateExceeded(event -> {
                    System.out.println("Failure rate exceeded: " + event.getFailureRate());
                });
    }

    /**
     * Service-specific circuit breaker configurations
     */
    public static class ServiceConfigs {
        
        /**
         * Circuit breaker for email service operations
         */
        public static CircuitBreaker emailServiceCircuitBreaker(CircuitBreakerRegistry registry) {
            return registry.circuitBreaker("emailService");
        }
        
        /**
         * Circuit breaker for database operations
         */
        public static CircuitBreaker databaseCircuitBreaker(CircuitBreakerRegistry registry) {
            return registry.circuitBreaker("database");
        }
        
        /**
         * Circuit breaker for external API calls
         */
        public static CircuitBreaker externalApiCircuitBreaker(CircuitBreakerRegistry registry) {
            return registry.circuitBreaker("externalApi");
        }
        
        /**
         * Circuit breaker for authentication services
         */
        public static CircuitBreaker authServiceCircuitBreaker(CircuitBreakerRegistry registry) {
            return registry.circuitBreaker("authService");
        }
    }

    /**
     * Retry configurations for different service types
     */
    public static class ServiceRetryConfigs {
        
        /**
         * Retry configuration for email service
         */
        public static Retry emailServiceRetry() {
            return Retry.of("emailService", RetryConfigs.EXPONENTIAL_RETRY_CONFIG);
        }
        
        /**
         * Retry configuration for database operations
         */
        public static Retry databaseRetry() {
            return Retry.of("database", RetryConfigs.QUICK_RETRY_CONFIG);
        }
        
        /**
         * Retry configuration for external APIs
         */
        public static Retry externalApiRetry() {
            return Retry.of("externalApi", RetryConfigs.EXPONENTIAL_RETRY_CONFIG);
        }
        
        /**
         * Retry configuration for authentication
         */
        public static Retry authServiceRetry() {
            return Retry.of("authService", RetryConfigs.CRITICAL_RETRY_CONFIG);
        }
    }

    /**
     * Circuit breaker monitoring and metrics
     */
    @Bean
    public CircuitBreakerMetrics circuitBreakerMetrics(CircuitBreakerRegistry registry) {
        return new CircuitBreakerMetrics(registry);
    }

    /**
     * Helper class for circuit breaker metrics
     */
    public static class CircuitBreakerMetrics {
        private final CircuitBreakerRegistry registry;

        public CircuitBreakerMetrics(CircuitBreakerRegistry registry) {
            this.registry = registry;
        }

        /**
         * Get circuit breaker status
         */
        public String getCircuitBreakerStatus(String name) {
            CircuitBreaker cb = registry.circuitBreaker(name);
            return cb.getState().name();
        }

        /**
         * Get failure rate
         */
        public float getFailureRate(String name) {
            CircuitBreaker cb = registry.circuitBreaker(name);
            return cb.getMetrics().getFailureRate();
        }

        /**
         * Get number of calls
         */
        public long getNumberOfCalls(String name) {
            CircuitBreaker cb = registry.circuitBreaker(name);
            return cb.getMetrics().getNumberOfTotalCalls();
        }
    }
}