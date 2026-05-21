package zm.unza.counseling.dto.response;

import lombok.Data;
import zm.unza.counseling.entity.ClinicReferral;

import java.time.LocalDateTime;

@Data
public class ClinicReferralResponse {

    private Long id;
    private String referralNumber;
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private Long counselorId;
    private String counselorName;
    private Long caseId;
    private String caseNumber;
    private ClinicReferral.Urgency urgency;
    private ClinicReferral.ReferralStatus status;
    private String reason;
    private String clinicalNotes;
    private String clinicNotes;
    private LocalDateTime clinicAppointmentDate;
    private String externalReferenceId;
    private LocalDateTime sentAt;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ClinicReferralResponse from(ClinicReferral r) {
        ClinicReferralResponse dto = new ClinicReferralResponse();
        dto.setId(r.getId());
        dto.setReferralNumber(r.getReferralNumber());
        dto.setClientId(r.getClient().getId());
        dto.setClientName(r.getClient().getFullName());
        dto.setClientEmail(r.getClient().getEmail());
        dto.setCounselorId(r.getCounselor().getId());
        dto.setCounselorName(r.getCounselor().getFullName());
        if (r.getLinkedCase() != null) {
            dto.setCaseId(r.getLinkedCase().getId());
            dto.setCaseNumber(r.getLinkedCase().getCaseNumber());
        }
        dto.setUrgency(r.getUrgency());
        dto.setStatus(r.getStatus());
        dto.setReason(r.getReason());
        dto.setClinicalNotes(r.getClinicalNotes());
        dto.setClinicNotes(r.getClinicNotes());
        dto.setClinicAppointmentDate(r.getClinicAppointmentDate());
        dto.setExternalReferenceId(r.getExternalReferenceId());
        dto.setSentAt(r.getSentAt());
        dto.setRespondedAt(r.getRespondedAt());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
