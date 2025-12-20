package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.AppointmentDto;
import zm.unza.counseling.dto.CreateAppointmentRequest;
import zm.unza.counseling.dto.UpdateAppointmentRequest;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    // private final NotificationService notificationService; // Assuming a NotificationService exists

    public Page<AppointmentDto> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(AppointmentDto::from);
    }

    public AppointmentDto getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .map(AppointmentDto::from)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
    }

    public Page<AppointmentDto> getAppointmentsByStudentId(Long studentId, Pageable pageable) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + studentId));
        return appointmentRepository.findByStudent(student, pageable).map(AppointmentDto::from);
    }

    public Page<AppointmentDto> getAppointmentsByCounselorId(Long counselorId, Pageable pageable) {
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + counselorId));
        return appointmentRepository.findByCounselor(counselor, pageable).map(AppointmentDto::from);
    }

    @Transactional
    public AppointmentDto createAppointment(CreateAppointmentRequest request) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + request.getStudentId()));
        User counselor = userRepository.findById(request.getCounselorId())
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + request.getCounselorId()));

        // Check for conflicting appointments
        LocalDateTime start = request.getAppointmentDate();
        LocalDateTime end = start.plusMinutes(60); // Assuming 60-minute duration
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(counselor, start, end);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Counselor has a conflicting appointment at the selected time.");
        }

        Appointment appointment = new Appointment();
        appointment.setTitle(request.getTitle());
        appointment.setStudent(student);
        appointment.setCounselor(counselor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setType(request.getType());
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // notificationService.createNotification(student, "Your appointment has been scheduled.", "/appointments/" + savedAppointment.getId());
        // notificationService.createNotification(counselor, "You have a new appointment.", "/appointments/" + savedAppointment.getId());

        return AppointmentDto.from(savedAppointment);
    }

    @Transactional
    public AppointmentDto updateAppointmentStatus(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));

        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }
        if (request.getAppointmentDate() != null) {
            appointment.setAppointmentDate(request.getAppointmentDate());
        }
        if (request.getCancellationReason() != null) {
            appointment.setCancellationReason(request.getCancellationReason());
        }

        return AppointmentDto.from(appointmentRepository.save(appointment));
    }
}
