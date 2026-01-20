package zm.unza.counseling.dto.response;

import lombok.Data;
import zm.unza.counseling.entity.Role;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private boolean active;
    private Set<Role> roles;
    private String department;
}
