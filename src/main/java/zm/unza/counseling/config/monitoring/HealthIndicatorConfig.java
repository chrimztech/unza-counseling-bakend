package zm.unza.counseling.config.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicators for enterprise monitoring
 */
@Component
public class HealthIndicatorConfig implements HealthIndicator {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public HealthIndicatorConfig(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;
        Status status = Status.UP;

        try {
            // Database connectivity check
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    details.put("database", "UP");
                    details.put("database_connection_time", System.currentTimeMillis());
                } else {
                    details.put("database", "DOWN");
                    isHealthy = false;
                    status = Status.DOWN;
                }
            }
        } catch (SQLException e) {
            details.put("database", "DOWN");
            details.put("database_error", e.getMessage());
            isHealthy = false;
            status = Status.DOWN;
        }

        try {
            // Check application-specific metrics
            Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE is_active = true", Long.class);
            Long appointmentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointments WHERE appointment_date >= CURRENT_DATE", Long.class);
            Long counselorCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM counselors WHERE is_available = true", Long.class);

            details.put("active_users", userCount != null ? userCount : 0);
            details.put("upcoming_appointments", appointmentCount != null ? appointmentCount : 0);
            details.put("available_counselors", counselorCount != null ? counselorCount : 0);
        } catch (Exception e) {
            details.put("metrics_error", e.getMessage());
            // Don't mark as unhealthy for metrics errors
        }

        // System information
        details.put("timestamp", LocalDateTime.now());
        details.put("system_time", System.currentTimeMillis());
        details.put("available_processors", Runtime.getRuntime().availableProcessors());
        details.put("free_memory_mb", Runtime.getRuntime().freeMemory() / (1024 * 1024));
        details.put("total_memory_mb", Runtime.getRuntime().totalMemory() / (1024 * 1024));

        Health.Builder healthBuilder = isHealthy ? Health.up() : Health.down();
        healthBuilder.withDetails(details);
        
        if (!isHealthy) {
            healthBuilder.withException(new RuntimeException("Database connection failed"));
        }

        return healthBuilder.build();
    }
}

/**
 * Database Health Indicator
 */
@Component
class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                // Test query to verify database is responsive
                connection.createStatement().execute("SELECT 1");
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("validationQuery", "SELECT 1")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
}

/**
 * Application Health Indicator
 */
@Component
class ApplicationHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        // Check application-specific components
        details.put("version", "1.0.0");
        details.put("environment", System.getProperty("spring.profiles.active", "unknown"));
        details.put("java_version", System.getProperty("java.version"));
        details.put("os_name", System.getProperty("os.name"));
        details.put("os_arch", System.getProperty("os.arch"));
        
        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        details.put("memory_used_mb", usedMemory / (1024 * 1024));
        details.put("memory_total_mb", totalMemory / (1024 * 1024));
        details.put("memory_max_mb", maxMemory / (1024 * 1024));
        details.put("memory_usage_percent", (double) usedMemory / maxMemory * 100);
        
        // Uptime
        long uptime = System.currentTimeMillis() - getApplicationStartTime();
        details.put("uptime_seconds", uptime / 1000);
        details.put("uptime_formatted", formatUptime(uptime));
        
        return Health.up()
            .withDetails(details)
            .build();
    }
    
    private long getApplicationStartTime() {
        // In a real application, you would store this at startup
        return System.currentTimeMillis(); // Simplified for this example
    }
    
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        return String.format("%d days, %d hours, %d minutes", days, hours % 24, minutes % 60);
    }
}