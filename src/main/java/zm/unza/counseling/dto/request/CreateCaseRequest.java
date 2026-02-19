package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zm.unza.counseling.entity.Case;

public class CreateCaseRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    private Long counselorId;

    private Case.CasePriority priority = Case.CasePriority.MEDIUM;

    private String subject;

    @NotBlank(message = "Case description is required")
    private String description;

    private String notes;

    // Getters and Setters
    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getCounselorId() {
        return counselorId;
    }

    public void setCounselorId(Long counselorId) {
        this.counselorId = counselorId;
    }

    public Case.CasePriority getPriority() {
        return priority;
    }

    public void setPriority(Case.CasePriority priority) {
        this.priority = priority;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
