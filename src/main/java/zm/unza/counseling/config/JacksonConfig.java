package zm.unza.counseling.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configure JavaTimeModule with a LocalDateTime deserializer that accepts
        // both local timestamps and ISO timestamps with timezone offsets.
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
            @Override
            public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                String value = parser.getValueAsString();
                if (value == null || value.isBlank()) {
                    return null;
                }

                try {
                    return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException ignored) {
                    // Fall back to offset-aware parsing below.
                }

                try {
                    return OffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
                            .atZoneSameInstant(ZoneId.systemDefault())
                            .toLocalDateTime();
                } catch (DateTimeParseException ignored) {
                    // Fall back to Instant parsing below.
                }

                try {
                    return Instant.parse(value)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                } catch (DateTimeParseException exception) {
                    throw context.weirdStringException(
                            value,
                            LocalDateTime.class,
                            "Expected an ISO local date-time or an ISO timestamp with timezone"
                    );
                }
            }
        });
        
        mapper.registerModule(javaTimeModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
