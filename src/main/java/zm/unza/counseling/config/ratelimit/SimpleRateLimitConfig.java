package zm.unza.counseling.config.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple enterprise-grade rate limiting configuration using Redis
 * Provides configurable rate limiting for API endpoints
 */
@Configuration
@RequiredArgsConstructor
public class SimpleRateLimitConfig {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConcurrentMap<String, SlidingWindowRateLimiter> cache = new ConcurrentHashMap<>();

    /**
     * Configuration for different rate limit tiers
     */
    public enum RateLimitTier {
        PUBLIC(100, 60),           // 100 requests per minute
        AUTHENTICATED(1000, 60),   // 1000 requests per minute
        ADMIN(5000, 60),           // 5000 requests per minute
        CRITICAL(10, 60),          // 10 requests per minute (login attempts)
        BULK(50, 60);              // 50 requests per minute (bulk operations)

        private final long capacity;
        private final int refillDurationSeconds;

        RateLimitTier(long capacity, int refillDurationSeconds) {
            this.capacity = capacity;
            this.refillDurationSeconds = refillDurationSeconds;
        }

        public long getCapacity() {
            return capacity;
        }

        public int getRefillDurationSeconds() {
            return refillDurationSeconds;
        }
    }

    /**
     * Redis script for sliding window rate limiting
     */
    @Bean
    public DefaultRedisScript<Long> slidingWindowRateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(
            "local current = redis.call('GET', KEYS[1]) " +
            "if current == false then " +
            "  redis.call('SET', KEYS[1], 1) " +
            "  redis.call('EXPIRE', KEYS[1], ARGV[2]) " +
            "  return 1 " +
            "else " +
            "  local count = tonumber(current) " +
            "  if count >= tonumber(ARGV[1]) then " +
            "    return 0 " +
            "  else " +
            "    redis.call('INCR', KEYS[1]) " +
            "    return count + 1 " +
            "  end " +
            "end"
        );
        script.setResultType(Long.class);
        return script;
    }

    /**
     * Check if request is allowed based on rate limits
     */
    public boolean isRateLimited(String key, RateLimitTier tier) {
        try {
            Long result = redisTemplate.execute(
                slidingWindowRateLimitScript(),
                Collections.singletonList(key),
                tier.getCapacity(),
                tier.getRefillDurationSeconds()
            );
            return result != null && result > 0;
        } catch (Exception e) {
            // Log error but don't block requests if Redis is down
            return true; // Allow requests if rate limiting fails
        }
    }

    /**
     * Get remaining tokens in bucket
     */
    public long getRemainingTokens(String key, RateLimitTier tier) {
        try {
            Object current = redisTemplate.opsForValue().get(key);
            if (current == null) {
                return tier.getCapacity();
            }
            long count = Long.parseLong(current.toString());
            return Math.max(0, tier.getCapacity() - count);
        } catch (Exception e) {
            return tier.getCapacity(); // Return full capacity if unable to check
        }
    }

    /**
     * Get remaining time until rate limit resets
     */
    public long getResetTime(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key);
            return ttl != null ? ttl * 1000 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Rate limit key generators for different contexts
     */
    public static class RateLimitKeyGenerator {
        
        /**
         * Generate key for IP-based rate limiting
         */
        public static String ipBasedKey(String ipAddress, String endpoint) {
            return String.format("rate_limit:ip:%s:%s", ipAddress, endpoint);
        }
        
        /**
         * Generate key for user-based rate limiting
         */
        public static String userBasedKey(String userId, String endpoint) {
            return String.format("rate_limit:user:%s:%s", userId, endpoint);
        }
        
        /**
         * Generate key for combined IP and user rate limiting
         */
        public static String combinedKey(String ipAddress, String userId, String endpoint) {
            if (userId != null) {
                return String.format("rate_limit:combined:%s:%s:%s", ipAddress, userId, endpoint);
            }
            return ipBasedKey(ipAddress, endpoint);
        }
        
        /**
         * Generate key for global rate limiting
         */
        public static String globalKey(String endpoint) {
            return String.format("rate_limit:global:%s", endpoint);
        }
    }

    /**
     * Simple sliding window rate limiter (fallback for local use)
     */
    private static class SlidingWindowRateLimiter {
        private final long capacity;
        private final long refillDurationMs;
        private final long[] window;
        private int currentIndex = 0;
        private long lastRefillTime;
        private long tokens;

        public SlidingWindowRateLimiter(long capacity, int refillDurationSeconds) {
            this.capacity = capacity;
            this.refillDurationMs = refillDurationSeconds * 1000L;
            this.window = new long[10]; // 10 sub-windows
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            
            // Refill tokens
            if (now - lastRefillTime >= refillDurationMs) {
                tokens = capacity;
                lastRefillTime = now;
            }

            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        public synchronized long getRemainingTokens() {
            return tokens;
        }
    }

    /**
     * Rate limit configuration for specific endpoints
     */
    public static class EndpointRateLimits {
        
        // Public endpoints (no authentication required)
        public static final RateLimitTier LOGIN_ENDPOINT = RateLimitTier.CRITICAL;
        public static final RateLimitTier REGISTER_ENDPOINT = RateLimitTier.CRITICAL;
        public static final RateLimitTier PUBLIC_API = RateLimitTier.PUBLIC;
        
        // Authenticated endpoints
        public static final RateLimitTier AUTHENTICATED_API = RateLimitTier.AUTHENTICATED;
        public static final RateLimitTier PROFILE_ENDPOINT = RateLimitTier.AUTHENTICATED;
        public static final RateLimitTier APPOINTMENT_ENDPOINT = RateLimitTier.AUTHENTICATED;
        
        // Admin endpoints
        public static final RateLimitTier ADMIN_API = RateLimitTier.ADMIN;
        public static final RateLimitTier BULK_OPERATION = RateLimitTier.BULK;
        
        // Emergency endpoints (very restrictive)
        public static final RateLimitTier EMERGENCY_API = RateLimitTier.CRITICAL;
    }
}