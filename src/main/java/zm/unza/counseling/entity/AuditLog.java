package zm.unza.counseling.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String entityType;
    private String entityId;
    private String userId;
    private String details;
    private String ipAddress;
    private String severity;
    private boolean success;

    @Convert(converter = JsonMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    @CreatedDate
    private LocalDateTime createdAt;

    public static class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {
        private static final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(Map<String, Object> attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return "{}";
            }
            try {
                return mapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                return "{}";
            }
        }

        @Override
        public Map<String, Object> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) {
                return java.util.Collections.emptyMap();
            }
            try {
                return mapper.readValue(dbData, Map.class);
            } catch (IOException e) {
                return java.util.Collections.emptyMap();
            }
        }
    }
}
