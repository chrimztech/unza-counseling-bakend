package zm.unza.counseling.service.interfaces;

import zm.unza.counseling.dto.request.AppointmentRequest;
import zm.unza.counseling.dto.response.AppointmentResponse;
import zm.unza.counseling.dto.AppointmentDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for appointment management
 * Provides abstraction layer for appointment operations
 */
public interface AppointmentService {
    
    /**
     * Create a new appointment
     * 
     * @param request appointment data
     * @return created appointment response
     */
    AppointmentResponse createAppointment(AppointmentRequest request);
    
    /**
     * Update existing appointment
     * 
     * @param id appointment ID
     * @param request updated data
     * @return updated appointment response
     */
    AppointmentResponse updateAppointment(Long id, AppointmentRequest request);
    
    /**
     * Get appointment by ID
     * 
     * @param id appointment ID
     * @return appointment response
     */
    Optional<AppointmentResponse> getAppointmentById(Long id);
    
    /**
     * Get all appointments with pagination
     * 
     * @param page page number
     * @param size page size
     * @return paginated list of appointments
     */
    List<AppointmentResponse> getAllAppointments(int page, int size);
    
    /**
     * Get appointments by client ID
     * 
     * @param clientId client ID
     * @return list of appointments
     */
    List<AppointmentResponse> getAppointmentsByClientId(Long clientId);
    
    /**
     * Get appointments by counselor ID
     * 
     * @param counselorId counselor ID
     * @return list of appointments
     */
    List<AppointmentResponse> getAppointmentsByCounselorId(Long counselorId);
    
    /**
     * Get appointments by date range
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of appointments
     */
    List<AppointmentResponse> getAppointmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get upcoming appointments
     * 
     * @param limit number of appointments to return
     * @return list of upcoming appointments
     */
    List<AppointmentResponse> getUpcomingAppointments(int limit);
    
    /**
     * Cancel appointment
     * 
     * @param id appointment ID
     * @param reason cancellation reason
     */
    void cancelAppointment(Long id, String reason);
    
    /**
     * Reschedule appointment
     * 
     * @param id appointment ID
     * @param newDateTime new appointment date and time
     * @return updated appointment response
     */
    AppointmentResponse rescheduleAppointment(Long id, LocalDateTime newDateTime);
    
    /**
     * Get appointment statistics
     * 
     * @return appointment statistics
     */
    AppointmentDto getAppointmentStatistics();
    
    /**
     * Check appointment availability
     * 
     * @param counselorId counselor ID
     * @param dateTime appointment date and time
     * @return true if available
     */
    boolean isAppointmentAvailable(Long counselorId, LocalDateTime dateTime);
    
    /**
     * Delete appointment
     * 
     * @param id appointment ID
     */
    void deleteAppointment(Long id);
}