package zm.unza.counseling.dto.response;

import lombok.Data;
import zm.unza.counseling.entity.Appointment;
import java.time.LocalDateTime;

@Data
public class AppointmentResponse {
    private Long id;
    private String title;
    private Long studentId;
    private String studentName;
    private Long counselorId;
    private String counselorName;
    private LocalDateTime appointmentDate;
    private Integer duration;
    private Appointment.AppointmentType type;
    private Appointment.AppointmentStatus status;
    private String location;
    private String meetingLink;
}
