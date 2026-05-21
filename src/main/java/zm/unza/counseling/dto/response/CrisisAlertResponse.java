package zm.unza.counseling.dto.response;

import zm.unza.counseling.entity.CrisisAlert;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class CrisisAlertResponse {
    private Long id;
    private String sourceType;
    private Long sourceId;
    private Long clientId;
    private String clientName;
    private String severity;
    private List<String> triggeredKeywords;
    private String status;
    private String counselorNotes;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    public static CrisisAlertResponse from(CrisisAlert a) {
        CrisisAlertResponse r = new CrisisAlertResponse();
        r.id = a.getId();
        r.sourceType = a.getSourceType().name();
        r.sourceId = a.getSourceId();
        if (a.getClient() != null) {
            r.clientId = a.getClient().getId();
            r.clientName = a.getClient().getFirstName() + " " + a.getClient().getLastName();
        }
        r.severity = a.getSeverity().name();
        r.triggeredKeywords = a.getTriggeredKeywords() != null
                ? Arrays.asList(a.getTriggeredKeywords().split(","))
                : List.of();
        r.status = a.getStatus().name();
        r.counselorNotes = a.getCounselorNotes();
        if (a.getReviewedBy() != null) {
            r.reviewedByName = a.getReviewedBy().getFirstName() + " " + a.getReviewedBy().getLastName();
        }
        r.reviewedAt = a.getReviewedAt();
        r.createdAt = a.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getSourceType() { return sourceType; }
    public Long getSourceId() { return sourceId; }
    public Long getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public String getSeverity() { return severity; }
    public List<String> getTriggeredKeywords() { return triggeredKeywords; }
    public String getStatus() { return status; }
    public String getCounselorNotes() { return counselorNotes; }
    public String getReviewedByName() { return reviewedByName; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
