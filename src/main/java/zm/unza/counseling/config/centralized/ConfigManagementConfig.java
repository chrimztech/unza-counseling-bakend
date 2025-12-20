package zm.unza.counseling.config.centralized;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.Map;

/**
 * Centralized Configuration Management for Enterprise deployments.
 * Supports multiple configuration sources and environment-specific settings.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class ConfigManagementConfig {

    /**
     * Application configuration properties.
     */
    @Bean
    @ConfigurationProperties(prefix = "unza.counseling")
    public ApplicationConfig applicationConfig() {
        return new ApplicationConfig();
    }

    /**
     * Security configuration properties.
     */
    @Bean
    @ConfigurationProperties(prefix = "unza.counseling.security")
    public SecurityConfig securityPropertiesConfig() {
        return new SecurityConfig();
    }

    /**
     * Database configuration properties.
     */
    @Bean
    @ConfigurationProperties(prefix = "unza.counseling.database")
    public DatabaseConfig databaseConfig() {
        return new DatabaseConfig();
    }

    /**
     * Cache configuration properties.
     */
    @Bean
    @ConfigurationProperties(prefix = "unza.counseling.cache")
    public CacheConfig cachePropertiesConfig() {
        return new CacheConfig();
    }

    /**
     * Email configuration properties.
     */
    @Bean
    @ConfigurationProperties(prefix = "unza.counseling.email")
    public EmailConfig emailConfig() {
        return new EmailConfig();
    }

    /**
     * Monitoring configuration properties.
     */
    @Bean
    @ConfigurationProperties(prefix = "unza.counseling.monitoring")
    public MonitoringConfig monitoringConfig() {
        return new MonitoringConfig();
    }

    /**
     * Development-specific configuration.
     */
    @Profile("dev")
    @Bean
    public DevConfig devConfig() {
        return new DevConfig();
    }

    /**
     * Production-specific configuration.
     */
    @Profile("prod")
    @Bean
    public ProdConfig prodConfig() {
        return new ProdConfig();
    }

    // Configuration classes

    public static class ApplicationConfig {
        private String name = "UNZA Counseling Management System";
        private String version = "1.0.0";
        private String description = "Enterprise counseling management platform";
        private Environment environment;
        private Map<String, Object> metadata;

        public enum Environment {
            DEV, STAGING, PROD
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Environment getEnvironment() { return environment; }
        public void setEnvironment(Environment environment) { this.environment = environment; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    public static class SecurityConfig {
        private JwtConfig jwt;
        private CorsConfig cors;
        private List<String> allowedOrigins;
        private boolean enableAuditLogging;
        private int sessionTimeoutMinutes = 30;

        public static class JwtConfig {
            private String secret;
            private long expirationTime = 86400000; // 24 hours
            private String issuer = "unza-counseling";

            // Getters and setters
            public String getSecret() { return secret; }
            public void setSecret(String secret) { this.secret = secret; }
            public long getExpirationTime() { return expirationTime; }
            public void setExpirationTime(long expirationTime) { this.expirationTime = expirationTime; }
            public String getIssuer() { return issuer; }
            public void setIssuer(String issuer) { this.issuer = issuer; }
        }

        public static class CorsConfig {
            private List<String> allowedOrigins;
            private List<String> allowedMethods;
            private List<String> allowedHeaders;
            private boolean allowCredentials = true;

            // Getters and setters
            public List<String> getAllowedOrigins() { return allowedOrigins; }
            public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
            public List<String> getAllowedMethods() { return allowedMethods; }
            public void setAllowedMethods(List<String> allowedMethods) { this.allowedMethods = allowedMethods; }
            public List<String> getAllowedHeaders() { return allowedHeaders; }
            public void setAllowedHeaders(List<String> allowedHeaders) { this.allowedHeaders = allowedHeaders; }
            public boolean isAllowCredentials() { return allowCredentials; }
            public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }
        }

        // Getters and setters
        public JwtConfig getJwt() { return jwt; }
        public void setJwt(JwtConfig jwt) { this.jwt = jwt; }
        public CorsConfig getCors() { return cors; }
        public void setCors(CorsConfig cors) { this.cors = cors; }
        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
        public boolean isEnableAuditLogging() { return enableAuditLogging; }
        public void setEnableAuditLogging(boolean enableAuditLogging) { this.enableAuditLogging = enableAuditLogging; }
        public int getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
        public void setSessionTimeoutMinutes(int sessionTimeoutMinutes) { this.sessionTimeoutMinutes = sessionTimeoutMinutes; }
    }

    public static class DatabaseConfig {
        private String url;
        private String username;
        private String password;
        private int poolSize = 10;
        private int connectionTimeout = 30000;
        private boolean enableFlyway = true;

        // Getters and setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public int getPoolSize() { return poolSize; }
        public void setPoolSize(int poolSize) { this.poolSize = poolSize; }
        public int getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        public boolean isEnableFlyway() { return enableFlyway; }
        public void setEnableFlyway(boolean enableFlyway) { this.enableFlyway = enableFlyway; }
    }

    public static class CacheConfig {
        private String type = "redis";
        private String host = "localhost";
        private int port = 6379;
        private int timeout = 2000;
        private int maxConnections = 8;
        private boolean enableDistributedCache = true;

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        public boolean isEnableDistributedCache() { return enableDistributedCache; }
        public void setEnableDistributedCache(boolean enableDistributedCache) { this.enableDistributedCache = enableDistributedCache; }
    }

    public static class EmailConfig {
        private String host;
        private int port = 587;
        private String username;
        private String password;
        private boolean enableSsl = true;
        private String fromAddress;
        private boolean enableAsync = true;

        // Getters and setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public boolean isEnableSsl() { return enableSsl; }
        public void setEnableSsl(boolean enableSsl) { this.enableSsl = enableSsl; }
        public String getFromAddress() { return fromAddress; }
        public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
        public boolean isEnableAsync() { return enableAsync; }
        public void setEnableAsync(boolean enableAsync) { this.enableAsync = enableAsync; }
    }

    public static class MonitoringConfig {
        private boolean enableMetrics = true;
        private boolean enableHealthChecks = true;
        private boolean enablePrometheus = true;
        private boolean enableDistributedTracing = true;
        private String serviceName = "unza-counseling-backend";
        private List<String> customMetrics;

        // Getters and setters
        public boolean isEnableMetrics() { return enableMetrics; }
        public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
        public boolean isEnableHealthChecks() { return enableHealthChecks; }
        public void setEnableHealthChecks(boolean enableHealthChecks) { this.enableHealthChecks = enableHealthChecks; }
        public boolean isEnablePrometheus() { return enablePrometheus; }
        public void setEnablePrometheus(boolean enablePrometheus) { this.enablePrometheus = enablePrometheus; }
        public boolean isEnableDistributedTracing() { return enableDistributedTracing; }
        public void setEnableDistributedTracing(boolean enableDistributedTracing) { this.enableDistributedTracing = enableDistributedTracing; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public List<String> getCustomMetrics() { return customMetrics; }
        public void setCustomMetrics(List<String> customMetrics) { this.customMetrics = customMetrics; }
    }

    public static class DevConfig {
        private boolean enableDebugLogging = true;
        private boolean enableH2Console = true;
        private boolean enableSwagger = true;
        private boolean enableTestDataLoader = true;

        // Getters and setters
        public boolean isEnableDebugLogging() { return enableDebugLogging; }
        public void setEnableDebugLogging(boolean enableDebugLogging) { this.enableDebugLogging = enableDebugLogging; }
        public boolean isEnableH2Console() { return enableH2Console; }
        public void setEnableH2Console(boolean enableH2Console) { this.enableH2Console = enableH2Console; }
        public boolean isEnableSwagger() { return enableSwagger; }
        public void setEnableSwagger(boolean enableSwagger) { this.enableSwagger = enableSwagger; }
        public boolean isEnableTestDataLoader() { return enableTestDataLoader; }
        public void setEnableTestDataLoader(boolean enableTestDataLoader) { this.enableTestDataLoader = enableTestDataLoader; }
    }

    public static class ProdConfig {
        private boolean enableSecurityHeaders = true;
        private boolean enableEncryption = true;
        private boolean enableBackup = true;
        private boolean enableMonitoring = true;
        private String logLevel = "INFO";

        // Getters and setters
        public boolean isEnableSecurityHeaders() { return enableSecurityHeaders; }
        public void setEnableSecurityHeaders(boolean enableSecurityHeaders) { this.enableSecurityHeaders = enableSecurityHeaders; }
        public boolean isEnableEncryption() { return enableEncryption; }
        public void setEnableEncryption(boolean enableEncryption) { this.enableEncryption = enableEncryption; }
        public boolean isEnableBackup() { return enableBackup; }
        public void setEnableBackup(boolean enableBackup) { this.enableBackup = enableBackup; }
        public boolean isEnableMonitoring() { return enableMonitoring; }
        public void setEnableMonitoring(boolean enableMonitoring) { this.enableMonitoring = enableMonitoring; }
        public String getLogLevel() { return logLevel; }
        public void setLogLevel(String logLevel) { this.logLevel = logLevel; }
    }
}