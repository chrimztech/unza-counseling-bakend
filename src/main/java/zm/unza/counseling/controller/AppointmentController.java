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
import zm.unza.counseling.dto.UpdateAppointmentRequest;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AppointmentDto>> getAllAppointments(Pageable pageable) {
        log.info("Fetching all appointments");
        return ResponseEntity.ok(appointmentService.getAllAppointments(pageable));
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
            @RequestParam String date) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.checkCounselorAvailability(counselorId, date)));
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
}
