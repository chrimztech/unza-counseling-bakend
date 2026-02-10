package zm.unza.counseling.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.AppointmentDto;
import zm.unza.counseling.dto.CreateAppointmentRequest;
import zm.unza.counseling.dto.UpdateAppointmentRequest;
import zm.unza.counseling.dto.request.AssignAppointmentRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.service.AppointmentService;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<AppointmentDto>> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        log.info("Creating appointment for student: {}", request.getStudentId());
        return ResponseEntity.ok(ApiResponse.success(appointmentService.createAppointment(request), "Appointment created successfully"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getAllAppointments(Pageable pageable) {
        log.info("Fetching all appointments");
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAllAppointments(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentDto> getAppointmentById(@PathVariable Long id) {
        log.info("Fetching appointment with id: {}", id);
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getAppointmentsByClient(@PathVariable Long clientId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentsByClientId(clientId, pageable)));
    }

    @GetMapping("/counselor/{counselorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getAppointmentsByCounselor(@PathVariable Long counselorId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentsByCounselorId(counselorId, pageable)));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getUpcomingAppointments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getUpcomingAppointments(pageable)));
    }

    @GetMapping("/past")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getPastAppointments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getPastAppointments(pageable)));
    }

    @GetMapping("/cancelled")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getCancelledAppointments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getCancelledAppointments(pageable)));
    }

    @GetMapping("/confirmed")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getConfirmedAppointments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getConfirmedAppointments(pageable)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getPendingAppointments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getPendingAppointments(pageable)));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<AppointmentDto>> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.cancelAppointment(id), "Appointment cancelled successfully"));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<AppointmentDto>> confirmAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.confirmAppointment(id), "Appointment confirmed successfully"));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<AppointmentDto>> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.rescheduleAppointment(id, request), "Appointment rescheduled successfully"));
    }

    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<?>> checkCounselorAvailability(
            @RequestParam Long counselorId,
            @RequestParam String dateTime) {
        boolean available = appointmentService.checkCounselorAvailability(counselorId, dateTime);
        return ResponseEntity.ok(ApiResponse.success(available, available ? "Counselor is available" : "Counselor is not available at this time"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<?>> getAppointmentStatistics() {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentStatistics()));
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT', 'CLIENT')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getTodaysAppointments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getTodaysAppointments(pageable)));
    }

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

    // ========== New endpoints for session assignment ==========

    /**
     * Get unassigned appointments (appointments without a counselor)
     */
    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getUnassignedAppointments(Pageable pageable) {
        log.info("Fetching unassigned appointments");
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getUnassignedAppointments(pageable), "Unassigned appointments retrieved successfully"));
    }

    /**
     * Get count of unassigned appointments
     */
    @GetMapping("/unassigned/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<Long>> countUnassignedAppointments() {
        log.info("Counting unassigned appointments");
        return ResponseEntity.ok(ApiResponse.success(appointmentService.countUnassignedAppointments(), "Unassigned appointments count"));
    }

    /**
     * Admin assigns a session to a counselor
     */
    @PostMapping("/admin/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentDto>> assignSessionToCounselor(@Valid @RequestBody AssignAppointmentRequest request) {
        log.info("Admin assigning appointment {} to counselor {}", request.getAppointmentId(), request.getCounselorId());
        return ResponseEntity.ok(ApiResponse.success(appointmentService.assignSessionToCounselor(request), "Session assigned to counselor successfully"));
    }

    /**
     * Counselor takes an unassigned appointment
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
