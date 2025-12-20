package zm.unza.counseling.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "counselors")
public class Counselor extends User {
    public Counselor() {
        super();
    }
}
