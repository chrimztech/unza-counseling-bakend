package zm.unza.counseling.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.AppointmentDto;
import zm.unza.counseling.dto.AppointmentStats;
import zm.unza.counseling.dto.AvailabilitySlot;
import zm.unza.counseling.dto.CreateAppointmentRequest;
import zm.unza.counseling.dto.UpdateAppointmentRequest;
import zm.unza.counseling.dto.request.AssignAppointmentRequest;
import zm.unza.counseling.dto.request.CancelRequest;
import zm.unza.counseling.dto.request.RescheduleRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.service.AppointmentService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/appointments", "/api/appointments", "/v1/appointments", "/appointments"})
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Get all appointments with pagination
     * GET /appointments
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getAllAppointments(Pageable pageable) {
        log.info("Fetching all appointments");
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAllAppointments(pageable)));
    }

    /**
     * Get appointment by ID
     * GET /appointments/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentDto>> getAppointmentById(@PathVariable Long id) {
        log.info("Fetching appointment with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentById(id)));
    }

    /**
     * Create a new appointment
     * POST /appointments
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<AppointmentDto>> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        log.info("Creating appointment for student: {}", request.getStudentId());
        return ResponseEntity.ok(ApiResponse.success(appointmentService.createAppointment(request), "Appointment created successfully"));
    }

    /**
     * Update an existing appointment
     * PUT /appointments/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<AppointmentDto>> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        log.info("Updating appointment with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(appointmentService.updateAppointment(id, request), "Appointment updated successfully"));
    }

    /**
     * Delete an appointment
     * DELETE /appointments/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(@PathVariable Long id) {
        log.info("Deleting appointment with id: {}", id);
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Appointment deleted successfully"));
    }

    /**
     * Get appointments by client ID
     * GET /appointments/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getAppointmentsByClient(@PathVariable Long clientId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentsByClientId(clientId, pageable)));
    }

    /**
     * Get appointments by counselor ID
     * GET /appointments/counselor/{counselorId}
     */
    @GetMapping("/counselor/{counselorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getAppointmentsByCounselor(@PathVariable Long counselorId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentsByCounselorId(counselorId, pageable)));
    }

    /**
     * Get today's appointments
     * GET /appointments/today
     */
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getTodaysAppointments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getTodaysAppointments(pageable)));
    }

    /**
     * Get upcoming appointments
     * GET /appointments/upcoming
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getUpcomingAppointments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getUpcomingAppointments(pageable)));
    }

    /**
     * Get past appointments
     * GET /appointments/past
     */
    @GetMapping("/past")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getPastAppointments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getPastAppointments(pageable)));
    }

    /**
     * Get pending appointments
     * GET /appointments/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getPendingAppointments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getPendingAppointments(pageable)));
    }

    /**
     * Get confirmed appointments
     * GET /appointments/confirmed
     */
    @GetMapping("/confirmed")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getConfirmedAppointments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getConfirmedAppointments(pageable)));
    }

    /**
     * Get cancelled appointments
     * GET /appointments/cancelled
     */
    @GetMapping("/cancelled")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getCancelledAppointments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getCancelledAppointments(pageable)));
    }

    /**
     * Cancel an appointment
     * PUT /appointments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<AppointmentDto>> cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) CancelRequest request) {
        log.info("Cancelling appointment with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(appointmentService.cancelAppointment(id, request), "Appointment cancelled successfully"));
    }

    /**
     * Confirm an appointment
     * PUT /appointments/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<AppointmentDto>> confirmAppointment(@PathVariable Long id) {
        log.info("Confirming appointment with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(appointmentService.confirmAppointment(id), "Appointment confirmed successfully"));
    }

    /**
     * Reschedule an appointment
     * PUT /appointments/{id}/reschedule
     */
    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<AppointmentDto>> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequest request) {
        log.info("Rescheduling appointment with id: {} to {}", id, request.getAppointmentDate());
        return ResponseEntity.ok(ApiResponse.success(appointmentService.rescheduleAppointment(id, request), "Appointment rescheduled successfully"));
    }

    /**
     * Get unassigned appointments
     * GET /appointments/unassigned
     */
    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getUnassignedAppointments(Pageable pageable) {
        log.info("Fetching unassigned appointments");
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getUnassignedAppointments(pageable), "Unassigned appointments retrieved successfully"));
    }

    /**
     * Get count of unassigned appointments
     * GET /appointments/unassigned/count
     */
    @GetMapping("/unassigned/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Long>> countUnassignedAppointments() {
        log.info("Counting unassigned appointments");
        return ResponseEntity.ok(ApiResponse.success(appointmentService.countUnassignedAppointments(), "Unassigned appointments count"));
    }

    /**
     * Check counselor availability
     * GET /appointments/availability
     */
    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<List<AvailabilitySlot>>> checkCounselorAvailability(
            @RequestParam Long counselorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
        log.info("Checking availability for counselor: {} on: {}", counselorId, dateTime);
        List<AvailabilitySlot> slots = appointmentService.getCounselorAvailabilitySlots(counselorId, dateTime);
        return ResponseEntity.ok(ApiResponse.success(slots, "Availability checked successfully"));
    }

    /**
     * Get appointment statistics
     * GET /appointments/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<AppointmentStats>> getAppointmentStatistics() {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentStatistics()));
    }

    /**
     * Export appointments
     * GET /appointments/export
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportAppointments(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        byte[] data = appointmentService.exportAppointments(format, startDate, endDate);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=appointments." + format)
                .body(data);
    }

    /**
     * Admin assigns a session to a counselor
     * POST /appointments/admin/assign
     */
    @PostMapping("/admin/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentDto>> assignSessionToCounselor(@Valid @RequestBody AssignAppointmentRequest request) {
        log.info("Admin assigning appointment {} to counselor {}", request.getAppointmentId(), request.getCounselorId());
        return ResponseEntity.ok(ApiResponse.success(appointmentService.assignSessionToCounselor(request), "Session assigned to counselor successfully"));
    }

    /**
     * Counselor takes an unassigned appointment
     * POST /appointments/counselor/take/{appointmentId}
     */
    @PostMapping("/counselor/take/{appointmentId}")
    @PreAuthorize("hasRole('COUNSELOR')")
    public ResponseEntity<ApiResponse<AppointmentDto>> counselorTakeAppointment(
            @PathVariable Long appointmentId,
            @RequestParam Long counselorId) {
        log.info("Counselor {} taking appointment {}", counselorId, appointmentId);
        return ResponseEntity.ok(ApiResponse.success(appointmentService.counselorTakeAppointment(appointmentId, counselorId), "Appointment taken successfully"));
    }
}
