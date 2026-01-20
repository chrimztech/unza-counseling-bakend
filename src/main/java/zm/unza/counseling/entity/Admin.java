package zm.unza.counseling.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {
    private String adminLevel; // SUPER_ADMIN, DEPARTMENT_ADMIN
    private String departmentManaged;
}
