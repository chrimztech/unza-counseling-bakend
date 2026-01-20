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
@RequestMapping("/api/reports")
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
}