package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.AppointmentDto;
import zm.unza.counseling.dto.CreateAppointmentRequest;
import zm.unza.counseling.dto.UpdateAppointmentRequest;
import zm.unza.counseling.dto.request.AssignAppointmentRequest;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service interface for appointment management
 */
public interface AppointmentService {

    /**
     * Get all appointments with pagination
     * @param pageable pagination information
     * @return paginated list of appointments
     */
    Page<AppointmentDto> getAllAppointments(Pageable pageable);

    /**
     * Get appointment by ID
     * @param id the appointment ID
     * @return the appointment DTO
     */
    AppointmentDto getAppointmentById(Long id);

    /**
     * Get appointments by client ID
     * @param clientId the client ID
     * @param pageable pagination information
     * @return paginated list of appointments
     */
    Page<AppointmentDto> getAppointmentsByClientId(Long clientId, Pageable pageable);

    /**
     * Get appointments by counselor ID
     * @param counselorId the counselor ID
     * @param pageable pagination information
     * @return paginated list of appointments
     */
    Page<AppointmentDto> getAppointmentsByCounselorId(Long counselorId, Pageable pageable);

    /**
     * Get appointments by student ID
     * @param studentId the student ID
     * @param pageable pagination information
     * @return paginated list of appointments
     */
    Page<AppointmentDto> getAppointmentsByStudentId(Long studentId, Pageable pageable);

    /**
     * Create appointment
     * @param request the create appointment request
     * @return the created appointment
     */
    AppointmentDto createAppointment(CreateAppointmentRequest request);

    /**
     * Update appointment status
     * @param id the appointment ID
     * @param request the update appointment request
     * @return the updated appointment
     */
    AppointmentDto updateAppointmentStatus(Long id, UpdateAppointmentRequest request);

    /**
     * Get upcoming appointments
     * @param pageable pagination information
     * @return paginated list of upcoming appointments
     */
    Page<AppointmentDto> getUpcomingAppointments(Pageable pageable);

    /**
     * Get past appointments
     * @param pageable pagination information
     * @return paginated list of past appointments
     */
    Page<AppointmentDto> getPastAppointments(Pageable pageable);

    /**
     * Get cancelled appointments
     * @param pageable pagination information
     * @return paginated list of cancelled appointments
     */
    Page<AppointmentDto> getCancelledAppointments(Pageable pageable);

    /**
     * Get confirmed appointments
     * @param pageable pagination information
     * @return paginated list of confirmed appointments
     */
    Page<AppointmentDto> getConfirmedAppointments(Pageable pageable);

    /**
     * Get pending appointments
     * @param pageable pagination information
     * @return paginated list of pending appointments
     */
    Page<AppointmentDto> getPendingAppointments(Pageable pageable);

    /**
     * Cancel appointment
     * @param id the appointment ID
     * @return the cancelled appointment
     */
    AppointmentDto cancelAppointment(Long id);

    /**
     * Confirm appointment
     * @param id the appointment ID
     * @return the confirmed appointment
     */
    AppointmentDto confirmAppointment(Long id);

    /**
     * Reschedule appointment
     * @param id the appointment ID
     * @param request the reschedule request
     * @return the rescheduled appointment
     */
    AppointmentDto rescheduleAppointment(Long id, UpdateAppointmentRequest request);

    /**
     * Check counselor availability
     * @param counselorId the counselor ID
     * @param dateTime the date and time to check
     * @return true if available, false otherwise
     */
    boolean checkCounselorAvailability(Long counselorId, String dateTime);

    /**
     * Get appointment statistics
     * @return appointment statistics
     */
    Object getAppointmentStatistics();

    /**
     * Export appointments
     * @param format the export format
     * @param startDate the start date
     * @param endDate the end date
     * @return the exported appointment data
     */
    byte[] exportAppointments(String format, String startDate, String endDate);
    
    /**
     * Get today's appointments
     * @param pageable pagination information
     * @return paginated list of today's appointments
     */
    Page<AppointmentDto> getTodaysAppointments(Pageable pageable);

    /**
     * Get unassigned appointments (appointments without a counselor)
     * @param pageable pagination information
     * @return paginated list of unassigned appointments
     */
    Page<AppointmentDto> getUnassignedAppointments(Pageable pageable);

    /**
     * Admin assigns a session to a counselor
     * @param request the assignment request containing appointment and counselor IDs
     * @return the updated appointment
     */
    AppointmentDto assignSessionToCounselor(AssignAppointmentRequest request);

    /**
     * Counselor takes up an unassigned appointment
     * @param appointmentId the appointment ID
     * @param counselorId the counselor ID taking the appointment
     * @return the updated appointment
     */
    AppointmentDto counselorTakeAppointment(Long appointmentId, Long counselorId);

    /**
     * Count unassigned appointments
     * @return count of unassigned appointments
     */
    Long countUnassignedAppointments();
}
