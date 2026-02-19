package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for creating a new client with optional case creation
 */
@Data
public class CreateClientRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phoneNumber;

    private String studentId;

    private String programme;

    private String faculty;

    private Integer yearOfStudy;

    private Double gpa;

    // Optional: Create case automatically
    private Boolean createCase = true;

    // Optional: Case subject (defaults to "Initial Counselling Request")
    private String caseSubject;

    // Optional: Case description (can be extracted from reasons for counselling)
    private String caseDescription;

    // Optional: Reasons for counselling (used to generate case description)
    private ReasonsForCounsellingRequest reasonsForCounselling;

    /**
     * Nested class for reasons for counselling
     */
    @Data
    public static class ReasonsForCounsellingRequest {
        private List<String> personal;
        private String personalOther;
        private List<String> health;
        private String healthOther;
        private List<String> educational;
        private String educationalOther;
        private List<String> career;
        private String careerOther;
        private List<String> financial;
        private String financialOther;
    }
}
