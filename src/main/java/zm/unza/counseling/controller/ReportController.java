package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.request.CounselorReportRequest;
import zm.unza.counseling.entity.Report;
import zm.unza.counseling.service.ReportService;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/reports", "/api/reports", "/v1/reports", "/reports"})
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report generation and management endpoints")
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get all reports", description = "Retrieve all generated reports with pagination")
    public ResponseEntity<ApiResponse<Page<Report>>> getAllReports(
            Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo
    ) {
        List<Report> filteredReports = reportService.getAllReports().stream()
                .filter(report -> matchesReportStatus(report.getStatus(), status))
                .filter(report -> matches(report.getType(), type))
                .filter(report -> matchesDateRange(report, dateFrom, dateTo))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(toPage(filteredReports, pageable)));
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
                                                              @RequestParam(required = false) String format,
                                                              @RequestParam(required = false) String startDate,
                                                              @RequestParam(required = false) String endDate) {
        Report report = reportService.generateReport(reportType, format, buildPeriod(startDate, endDate));
        return ResponseEntity.ok(ApiResponse.success(report, "Report generated successfully"));
    }

    @PostMapping("/counselor")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Create counselor report", description = "Create a counselor report linked to case, appointment, and session data")
    public ResponseEntity<ApiResponse<Report>> createCounselorReport(@Valid @RequestBody CounselorReportRequest request) {
        Report report = reportService.createCounselorReport(request);
        return ResponseEntity.ok(ApiResponse.success(report, "Counselor report created successfully"));
    }

    @PutMapping("/counselor/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Update counselor report", description = "Update a counselor report and its linked case context")
    public ResponseEntity<ApiResponse<Report>> updateCounselorReport(
            @PathVariable Long id,
            @Valid @RequestBody CounselorReportRequest request) {
        Report report = reportService.updateCounselorReport(id, request);
        return ResponseEntity.ok(ApiResponse.success(report, "Counselor report updated successfully"));
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
        reportService.scheduleReport(reportType, null, buildPeriod(startDate, endDate), schedule);
        return ResponseEntity.ok(ApiResponse.success("Report scheduled successfully"));
    }

    @GetMapping("/export/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Export report", description = "Export a report in specified format")
    public ResponseEntity<byte[]> exportReport(@PathVariable Long id,
                                               @RequestParam(defaultValue = "pdf") String format) {
        Report report = reportService.getReportById(id);
        byte[] reportData = reportService.exportReport(id, format);
        return ResponseEntity.ok()
                .contentType(resolveExportContentType(format))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(buildExportFilename(report, format))
                        .build()
                        .toString())
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
        reportService.updateReportSchedule(scheduleId, null, buildPeriod(startDate, endDate), schedule);
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

    private MediaType resolveExportContentType(String format) {
        String normalized = format == null ? "pdf" : format.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "csv" -> new MediaType("text", "csv");
            case "excel", "xls", "xlsx" -> new MediaType("application", "vnd.ms-excel");
            case "json" -> MediaType.APPLICATION_JSON;
            default -> MediaType.APPLICATION_PDF;
        };
    }

    private String buildExportFilename(Report report, String format) {
        String baseName = report.getTitle() == null || report.getTitle().isBlank()
                ? "report-" + report.getId()
                : report.getTitle()
                .trim()
                .replaceAll("[^a-zA-Z0-9._-]+", "-")
                .replaceAll("^-+|-+$", "");
        if (baseName.isBlank()) {
            baseName = "report-" + report.getId();
        }

        String extension = switch (format == null ? "pdf" : format.toLowerCase(Locale.ROOT)) {
            case "csv" -> "csv";
            case "excel", "xls", "xlsx" -> "xls";
            case "json" -> "json";
            default -> "pdf";
        };

        return baseName + "." + extension;
    }

    private Page<Report> toPage(List<Report> reports, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(reports);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), reports.size());
        List<Report> pageContent = start >= reports.size() ? List.of() : reports.subList(start, end);
        return new PageImpl<>(pageContent, pageable, reports.size());
    }

    private boolean matches(String value, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return value != null && value.equalsIgnoreCase(filter);
    }

    private boolean matchesReportStatus(String value, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }

        String normalizedFilter = filter.toUpperCase(Locale.ROOT);
        if ("GENERATED".equals(normalizedFilter)) {
            normalizedFilter = "COMPLETED";
        } else if ("DRAFT".equals(normalizedFilter)) {
            normalizedFilter = "PENDING";
        }

        return value != null && value.equalsIgnoreCase(normalizedFilter);
    }

    private boolean matchesDateRange(Report report, String dateFrom, String dateTo) {
        if (report.getReportDate() == null) {
            return dateFrom == null && dateTo == null;
        }

        String reportDate = report.getReportDate().toLocalDate().toString();
        boolean afterStart = dateFrom == null || dateFrom.isBlank() || reportDate.compareTo(dateFrom) >= 0;
        boolean beforeEnd = dateTo == null || dateTo.isBlank() || reportDate.compareTo(dateTo) <= 0;
        return afterStart && beforeEnd;
    }

    private String buildPeriod(String startDate, String endDate) {
        if ((startDate == null || startDate.isBlank()) && (endDate == null || endDate.isBlank())) {
            return "all-time";
        }
        if (startDate != null && !startDate.isBlank() && endDate != null && !endDate.isBlank()) {
            return String.format(Locale.ROOT, "%s..%s", startDate, endDate);
        }
        return startDate != null && !startDate.isBlank() ? startDate : endDate;
    }
}
