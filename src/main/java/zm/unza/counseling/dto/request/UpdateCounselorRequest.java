package zm.unza.counseling.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateCounselorRequest {

    @Email(message = "Invalid email format")
    private String email;

    @JsonAlias("first_name")
    private String firstName;

    @JsonAlias("last_name")
    private String lastName;

    @JsonAlias("phone_number")
    private String phoneNumber;

    private String specialization;

    private String bio;

    @JsonAlias("office_location")
    private String officeLocation;

    private String department;

    private Boolean available;
}
