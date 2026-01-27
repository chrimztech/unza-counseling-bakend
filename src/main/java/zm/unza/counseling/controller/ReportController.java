package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.Report;
import zm.unza.counseling.service.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report generation and management endpoints")
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get all reports", description = "Retrieve all generated reports with pagination")
    public ResponseEntity<ApiResponse<Page<Report>>> getAllReports(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getAllReports(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get report by ID", description = "Retrieve a specific report")
    public ResponseEntity<ApiResponse<Report>> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Generate report", description = "Generate a new report")
    public ResponseEntity<ApiResponse<Report>> generateReport(@RequestParam String reportType,
                                                              @RequestParam(required = false) String startDate,
                                                              @RequestParam(required = false) String endDate) {
        Report report = reportService.generateReport(reportType, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report, "Report generated successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update report", description = "Update an existing report")
    public ResponseEntity<ApiResponse<Report>> updateReport(@PathVariable Long id, @RequestBody Report report) {
        Report updatedReport = reportService.updateReport(id, report);
        return ResponseEntity.ok(ApiResponse.success(updatedReport, "Report updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete report", description = "Delete a report")
    public ResponseEntity<ApiResponse<String>> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report deleted successfully"));
    }

    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get report types", description = "Get all available report types")
    public ResponseEntity<ApiResponse<List<String>>> getReportTypes() {
        List<String> types = reportService.getReportTypes();
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Schedule report", description = "Schedule a report for automatic generation")
    public ResponseEntity<ApiResponse<String>> scheduleReport(@RequestParam String reportType,
                                                              @RequestParam String schedule,
                                                              @RequestParam(required = false) String startDate,
                                                              @RequestParam(required = false) String endDate) {
        reportService.scheduleReport(reportType, schedule, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Report scheduled successfully"));
    }

    @GetMapping("/export/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Export report", description = "Export a report in specified format")
    public ResponseEntity<byte[]> exportReport(@PathVariable Long id,
                                               @RequestParam(defaultValue = "pdf") String format) {
        byte[] reportData = reportService.exportReport(id, format);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report." + format)
                .body(reportData);
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get report history", description = "Get history of generated reports")
    public ResponseEntity<ApiResponse<Page<Report>>> getReportHistory(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportHistory(pageable)));
    }

    @GetMapping("/scheduled")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get scheduled reports", description = "Get all scheduled reports")
    public ResponseEntity<ApiResponse<List<Report>>> getScheduledReports() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getScheduledReports()));
    }

    @PutMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update report schedule", description = "Update an existing report schedule")
    public ResponseEntity<ApiResponse<String>> updateReportSchedule(
            @PathVariable Long scheduleId,
            @RequestParam String schedule,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        reportService.updateReportSchedule(scheduleId, schedule, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Report schedule updated successfully"));
    }

    @DeleteMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete report schedule", description = "Delete a report schedule")
    public ResponseEntity<ApiResponse<String>> deleteReportSchedule(@PathVariable Long scheduleId) {
        reportService.deleteReportSchedule(scheduleId);
        return ResponseEntity.ok(ApiResponse.success("Report schedule deleted successfully"));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get report statistics", description = "Get statistics about reports")
    public ResponseEntity<ApiResponse<?>> getReportStatistics() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportStatistics()));
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get report analytics", description = "Get analytics about reports")
    public ResponseEntity<ApiResponse<?>> getReportAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportAnalytics()));
    }

    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Duplicate report", description = "Duplicate an existing report")
    public ResponseEntity<ApiResponse<Report>> duplicateReport(@PathVariable Long id) {
        Report duplicatedReport = reportService.duplicateReport(id);
        return ResponseEntity.ok(ApiResponse.success(duplicatedReport, "Report duplicated successfully"));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Archive report", description = "Archive a report")
    public ResponseEntity<ApiResponse<String>> archiveReport(@PathVariable Long id) {
        reportService.archiveReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report archived successfully"));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restore archived report", description = "Restore an archived report")
    public ResponseEntity<ApiResponse<String>> restoreReport(@PathVariable Long id) {
        reportService.restoreReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report restored successfully"));
    }

    @GetMapping("/archived")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get archived reports", description = "Get all archived reports")
    public ResponseEntity<ApiResponse<List<Report>>> getArchivedReports() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getArchivedReports()));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get report summary", description = "Get a summary of reports")
    public ResponseEntity<ApiResponse<?>> getReportSummary() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportSummary()));
    }

    @GetMapping("/appointment-trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get appointment trends", description = "Get trends in appointments")
    public ResponseEntity<ApiResponse<?>> getAppointmentTrends() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getAppointmentTrends()));
    }

    @GetMapping("/presenting-concerns")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get presenting concerns", description = "Get presenting concerns from reports")
    public ResponseEntity<ApiResponse<?>> getPresentingConcerns() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getPresentingConcerns()));
    }

    @GetMapping("/recent-sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get recent sessions", description = "Get recent sessions from reports")
    public ResponseEntity<ApiResponse<?>> getRecentSessions() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getRecentSessions()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get all report data", description = "Get all report data")
    public ResponseEntity<ApiResponse<?>> getAllReportData() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getAllReportData()));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export report (legacy)", description = "Export report data in legacy format")
    public ResponseEntity<byte[]> exportReportLegacy(@RequestParam(defaultValue = "csv") String format) {
        byte[] data = reportService.exportReportLegacy(format);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=reports." + format)
                .body(data);
    }
}