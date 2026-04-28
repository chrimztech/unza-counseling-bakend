package zm.unza.counseling.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCounselorRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @JsonAlias("first_name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @JsonAlias("last_name")
    private String lastName;

    @JsonAlias("phone_number")
    private String phoneNumber;

    private String specialization;

    private String bio;

    @JsonAlias("office_location")
    private String officeLocation;

    private String department;
}
