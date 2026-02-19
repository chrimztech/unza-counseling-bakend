package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.NotNull;

public class CaseAssignmentRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Counselor ID is required")
    private Long counselorId;

    private String assignmentReason;

    private String assignmentNotes;

    // Getters and Setters
    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public Long getCounselorId() {
        return counselorId;
    }

    public void setCounselorId(Long counselorId) {
        this.counselorId = counselorId;
    }

    public String getAssignmentReason() {
        return assignmentReason;
    }

    public void setAssignmentReason(String assignmentReason) {
        this.assignmentReason = assignmentReason;
    }

    public String getAssignmentNotes() {
        return assignmentNotes;
    }

    public void setAssignmentNotes(String assignmentNotes) {
        this.assignmentNotes = assignmentNotes;
    }
}
