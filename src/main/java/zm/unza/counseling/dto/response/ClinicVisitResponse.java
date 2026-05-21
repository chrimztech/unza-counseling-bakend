package zm.unza.counseling.dto.response;

import lombok.Data;
import zm.unza.counseling.entity.ClinicVisit;

import java.time.LocalDateTime;

@Data
public class ClinicVisitResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private Long referralId;
    private String referralNumber;
    private LocalDateTime visitDate;
    private ClinicVisit.VisitType visitType;
    private String visitPurpose;
    private String notes;
    private String recordedBy;
    private Boolean counselorNotified;
    private LocalDateTime createdAt;

    public static ClinicVisitResponse from(ClinicVisit v) {
        ClinicVisitResponse dto = new ClinicVisitResponse();
        dto.setId(v.getId());
        dto.setClientId(v.getClient().getId());
        dto.setClientName(v.getClient().getFullName());
        dto.setClientEmail(v.getClient().getEmail());
        if (v.getReferral() != null) {
            dto.setReferralId(v.getReferral().getId());
            dto.setReferralNumber(v.getReferral().getReferralNumber());
        }
        dto.setVisitDate(v.getVisitDate());
        dto.setVisitType(v.getVisitType());
        dto.setVisitPurpose(v.getVisitPurpose());
        dto.setNotes(v.getNotes());
        dto.setRecordedBy(v.getRecordedBy());
        dto.setCounselorNotified(v.getCounselorNotified());
        dto.setCreatedAt(v.getCreatedAt());
        return dto;
    }
}
