package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO for creating/updating a goal
 */
@Data
public class GoalRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private String category;

    private Integer targetValue;

    private Integer currentValue;

    private Integer progress;

    @NotNull(message = "Status is required")
    private String status;

    private LocalDate deadline;
}
