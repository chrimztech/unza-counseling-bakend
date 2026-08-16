package zm.unza.counseling.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateAdminRequest {

    @Email(message = "Invalid email format")
    private String email;

    @JsonAlias("first_name")
    private String firstName;

    @JsonAlias("last_name")
    private String lastName;

    @JsonAlias("phone_number")
    private String phoneNumber;

    private String adminLevel;

    @JsonAlias("department_managed")
    private String departmentManaged;

    @JsonSetter("adminLevel")
    public void setAdminLevel(Object adminLevel) {
        this.adminLevel = normalizeAdminLevel(adminLevel);
    }

    @JsonSetter("admin_level")
    public void setAdminLevelAlias(Object adminLevel) {
        setAdminLevel(adminLevel);
    }

    private String normalizeAdminLevel(Object adminLevel) {
        if (adminLevel == null) {
            return null;
        }

        String value = String.valueOf(adminLevel).trim();
        if (value.isEmpty()) {
            return null;
        }

        return switch (value.toUpperCase()) {
            case "1", "STANDARD", "STANDARD_ADMIN", "ADMIN", "DEPARTMENT_ADMIN" -> "STANDARD_ADMIN";
            case "2", "SENIOR", "SENIOR_ADMIN" -> "SENIOR_ADMIN";
            case "3", "SUPER", "SUPER_ADMIN", "SYSTEM_ADMIN" -> "SUPER_ADMIN";
            default -> value.toUpperCase();
        };
    }
}
