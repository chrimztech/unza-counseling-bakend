package zm.unza.counseling.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("COUNSELOR")
public class Counselor extends User {
    private String specialization;
    private String bio;
    private String officeLocation;
    private boolean available = true;
}