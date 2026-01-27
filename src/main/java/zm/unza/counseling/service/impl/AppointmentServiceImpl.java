package zm.unza.counseling.service.impl;

import zm.unza.counseling.dto.AppointmentDto;
import zm.unza.counseling.dto.CreateAppointmentRequest;
import zm.unza.counseling.dto.UpdateAppointmentRequest;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementation of AppointmentService
 */
@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<AppointmentDto> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(AppointmentDto::from);
    }

    @Override
    public AppointmentDto getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .map(AppointmentDto::from)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByClientId(Long clientId, Pageable pageable) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("Client not found with id: " + clientId));
        return appointmentRepository.findByStudent(client, pageable).map(AppointmentDto::from);
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByCounselorId(Long counselorId, Pageable pageable) {
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + counselorId));
        return appointmentRepository.findByCounselor(counselor, pageable).map(AppointmentDto::from);
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByStudentId(Long studentId, Pageable pageable) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + studentId));
        return appointmentRepository.findByStudent(student, pageable).map(AppointmentDto::from);
    }

    @Override
    public AppointmentDto createAppointment(CreateAppointmentRequest request) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + request.getStudentId()));
        User counselor = userRepository.findById(request.getCounselorId())
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + request.getCounselorId()));
        
        Appointment appointment = new Appointment();
        appointment.setTitle(request.getTitle());
        appointment.setStudent(student);
        appointment.setCounselor(counselor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setType(request.getType());
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        appointment.setDescription(request.getDescription());
        
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentDto updateAppointmentStatus(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        appointment.setStatus(request.getStatus());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setCancellationReason(request.getCancellationReason());
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    @Override
    public Page<AppointmentDto> getUpcomingAppointments(Pageable pageable) {
        return appointmentRepository.findByAppointmentDateAfter(LocalDateTime.now(), pageable).map(AppointmentDto::from);
    }

    @Override
    public Page<AppointmentDto> getPastAppointments(Pageable pageable) {
        return appointmentRepository.findByAppointmentDateBefore(LocalDateTime.now(), pageable).map(AppointmentDto::from);
    }

    @Override
    public Page<AppointmentDto> getCancelledAppointments(Pageable pageable) {
        return appointmentRepository.findByStatus(Appointment.AppointmentStatus.CANCELLED, pageable).map(AppointmentDto::from);
    }

    @Override
    public Page<AppointmentDto> getConfirmedAppointments(Pageable pageable) {
        return appointmentRepository.findByStatus(Appointment.AppointmentStatus.CONFIRMED, pageable).map(AppointmentDto::from);
    }

    @Override
    public Page<AppointmentDto> getPendingAppointments(Pageable pageable) {
        return appointmentRepository.findByStatus(Appointment.AppointmentStatus.SCHEDULED, pageable).map(AppointmentDto::from);
    }

    @Override
    public AppointmentDto cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentDto confirmAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentDto rescheduleAppointment(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        appointment.setAppointmentDate(request.getAppointmentDate());
        if (request.getTitle() != null) {
            appointment.setTitle(request.getTitle());
        }
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    @Override
    public boolean checkCounselorAvailability(Long counselorId, String dateTime) {
        // Implementation would check if counselor is available at the given time
        return true;
    }

    @Override
    public Object getAppointmentStatistics() {
        // Implementation would return appointment statistics
        return null;
    }

    @Override
    public byte[] exportAppointments(String format, String startDate, String endDate) {
        // Implementation would export appointments
        return new byte[0];
    }

    public Page<AppointmentDto> getTodaysAppointments(Pageable pageable) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return appointmentRepository.findByAppointmentDateBetween(startOfDay, endOfDay, pageable).map(AppointmentDto::from);
    }
}