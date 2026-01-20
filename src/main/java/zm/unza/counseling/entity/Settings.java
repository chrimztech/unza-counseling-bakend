package zm.unza.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String value;

    @Enumerated(EnumType.STRING)
    private SettingType type;

    @Enumerated(EnumType.STRING)
    private SettingCategory category;

    private String description;

    private boolean active;

    public enum SettingType {
        STRING, INTEGER, BOOLEAN, JSON
    }

    public enum SettingCategory {
        ORGANIZATION, APPOINTMENTS, NOTIFICATIONS, SECURITY
    }
}