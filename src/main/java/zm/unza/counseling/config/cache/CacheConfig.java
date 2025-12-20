package zm.unza.counseling.config.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;

/**
 * Enterprise-grade cache configuration with Redis
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure Redis cache manager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Default TTL
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * Configure Redis template for manual operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // JSON serializer
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        // String serializer
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Configure serializers
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Cache configurations for different data types
     */
    public static class CacheNames {
        // User-related caches
        public static final String USERS = "users";
        public static final String USER_PROFILES = "userProfiles";
        
        // Appointment-related caches
        public static final String APPOINTMENTS = "appointments";
        public static final String APPOINTMENT_SLOTS = "appointmentSlots";
        
        // Client-related caches
        public static final String CLIENTS = "clients";
        public static final String CLIENT_ANALYTICS = "clientAnalytics";
        
        // Counselor-related caches
        public static final String COUNSELORS = "counselors";
        public static final String COUNSELOR_SCHEDULES = "counselorSchedules";
        
        // Assessment-related caches
        public static final String RISK_ASSESSMENTS = "riskAssessments";
        public static final String SELF_ASSESSMENTS = "selfAssessments";
        public static final String ACADEMIC_PERFORMANCE = "academicPerformance";
        
        // System caches
        public static final String SYSTEM_CONFIG = "systemConfig";
        public static final String DASHBOARD_STATS = "dashboardStats";
        public static final String REPORTS = "reports";
        
        // Notification caches
        public static final String NOTIFICATIONS = "notifications";
        public static final String MESSAGES = "messages";
    }

    /**
     * TTL configurations for different cache types
     */
    public static class CacheTTL {
        // Short-term caches (5-15 minutes)
        public static final Duration USER_SESSION = Duration.ofMinutes(15);
        public static final Duration APPOINTMENT_SLOTS = Duration.ofMinutes(5);
        public static final Duration DASHBOARD_STATS = Duration.ofMinutes(5);
        
        // Medium-term caches (30-60 minutes)
        public static final Duration USERS = Duration.ofMinutes(30);
        public static final Duration CLIENTS = Duration.ofMinutes(30);
        public static final Duration COUNSELORS = Duration.ofMinutes(30);
        public static final Duration APPOINTMENTS = Duration.ofMinutes(30);
        
        // Long-term caches (2-24 hours)
        public static final Duration RISK_ASSESSMENTS = Duration.ofHours(2);
        public static final Duration SELF_ASSESSMENTS = Duration.ofHours(2);
        public static final Duration ACADEMIC_PERFORMANCE = Duration.ofHours(2);
        public static final Duration REPORTS = Duration.ofHours(6);
        
        // Very long-term caches (24+ hours)
        public static final Duration SYSTEM_CONFIG = Duration.ofHours(24);
        public static final Duration CLIENT_ANALYTICS = Duration.ofHours(12);
    }
}

/**
 * Cache invalidation helper
 */
@Component
class CacheEvictionHelper {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    public CacheEvictionHelper(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Evict specific cache by name
     */
    public void evictCache(String cacheName) {
        if (cacheManager.getCache(cacheName) != null) {
            cacheManager.getCache(cacheName).clear();
        }
    }

    /**
     * Evict cache by pattern (for Redis)
     */
    public void evictCacheByPattern(String pattern) {
        redisTemplate.delete(redisTemplate.keys(pattern));
    }

    /**
     * Evict user-related caches
     */
    public void evictUserCaches(Long userId) {
        evictCache(CacheConfig.CacheNames.USERS);
        evictCache(CacheConfig.CacheNames.USER_PROFILES);
        evictCacheByPattern("users:" + userId + ":*");
        evictCacheByPattern("userProfiles:" + userId + ":*");
    }

    /**
     * Evict appointment-related caches
     */
    public void evictAppointmentCaches(Long appointmentId) {
        evictCache(CacheConfig.CacheNames.APPOINTMENTS);
        evictCache(CacheConfig.CacheNames.APPOINTMENT_SLOTS);
        evictCacheByPattern("appointments:" + appointmentId + ":*");
        evictCacheByPattern("appointmentSlots:*");
    }

    /**
     * Evict client-related caches
     */
    public void evictClientCaches(Long clientId) {
        evictCache(CacheConfig.CacheNames.CLIENTS);
        evictCache(CacheConfig.CacheNames.CLIENT_ANALYTICS);
        evictCache(CacheConfig.CacheNames.RISK_ASSESSMENTS);
        evictCache(CacheConfig.CacheNames.SELF_ASSESSMENTS);
        evictCache(CacheConfig.CacheNames.ACADEMIC_PERFORMANCE);
        evictCacheByPattern("clients:" + clientId + ":*");
        evictCacheByPattern("clientAnalytics:" + clientId + ":*");
    }

    /**
     * Evict counselor-related caches
     */
    public void evictCounselorCaches(Long counselorId) {
        evictCache(CacheConfig.CacheNames.COUNSELORS);
        evictCache(CacheConfig.CacheNames.COUNSELOR_SCHEDULES);
        evictCacheByPattern("counselors:" + counselorId + ":*");
        evictCacheByPattern("counselorSchedules:" + counselorId + ":*");
    }

    /**
     * Evict assessment-related caches
     */
    public void evictAssessmentCaches(Long clientId) {
        evictCache(CacheConfig.CacheNames.RISK_ASSESSMENTS);
        evictCache(CacheConfig.CacheNames.SELF_ASSESSMENTS);
        evictCache(CacheConfig.CacheNames.ACADEMIC_PERFORMANCE);
        evictCache(CacheConfig.CacheNames.CLIENT_ANALYTICS);
        evictCacheByPattern("riskAssessments:" + clientId + ":*");
        evictCacheByPattern("selfAssessments:" + clientId + ":*");
        evictCacheByPattern("academicPerformance:" + clientId + ":*");
    }

    /**
     * Evict all application caches
     */
    public void evictAllCaches() {
        // Evict all named caches
        evictCache(CacheConfig.CacheNames.USERS);
        evictCache(CacheConfig.CacheNames.USER_PROFILES);
        evictCache(CacheConfig.CacheNames.APPOINTMENTS);
        evictCache(CacheConfig.CacheNames.APPOINTMENT_SLOTS);
        evictCache(CacheConfig.CacheNames.CLIENTS);
        evictCache(CacheConfig.CacheNames.CLIENT_ANALYTICS);
        evictCache(CacheConfig.CacheNames.COUNSELORS);
        evictCache(CacheConfig.CacheNames.COUNSELOR_SCHEDULES);
        evictCache(CacheConfig.CacheNames.RISK_ASSESSMENTS);
        evictCache(CacheConfig.CacheNames.SELF_ASSESSMENTS);
        evictCache(CacheConfig.CacheNames.ACADEMIC_PERFORMANCE);
        evictCache(CacheConfig.CacheNames.SYSTEM_CONFIG);
        evictCache(CacheConfig.CacheNames.DASHBOARD_STATS);
        evictCache(CacheConfig.CacheNames.REPORTS);
        evictCache(CacheConfig.CacheNames.NOTIFICATIONS);
        evictCache(CacheConfig.CacheNames.MESSAGES);

        // Clear all Redis keys
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }
}

/**
 * Custom cache annotations
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface EvictUserCache {
    String[] value() default {};
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface EvictAppointmentCache {
    String[] value() default {};
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface EvictClientCache {
    String[] value() default {};
}