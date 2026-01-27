package zm.unza.counseling.service;

import zm.unza.counseling.entity.Report;
import zm.unza.counseling.entity.Report.ReportFormat;
import zm.unza.counseling.entity.Report.ReportType;
import zm.unza.counseling.entity.Report.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for report generation and management
 */
public interface ReportService {

    /**
     * Generate a new report
     * @param type the type of report to generate
     * @param format the format of the report
     * @return the generated report
     */
    Report generateReport(ReportType type, ReportFormat format);

    /**
     * Get all reports
     * @return list of all reports
     */
    List<Report> getAllReports();

    /**
     * Get all reports with pagination
     * @param pageable pagination information
     * @return paginated list of reports
     */
    Page<Report> getAllReports(Pageable pageable);

    /**
     * Get report by ID
     * @param id the report ID
     * @return the report
     */
    Report getReportById(Long id);

    /**
     * Update report status
     * @param id the report ID
     * @param status the new status
     * @return the updated report
     */
    Report updateReportStatus(Long id, ReportStatus status);

    /**
     * Update report
     * @param id the report ID
     * @param report the updated report
     * @return the updated report
     */
    Report updateReport(Long id, Report report);

    /**
     * Delete report by ID
     * @param id the report ID
     */
    void deleteReport(Long id);

    /**
     * Get reports by type
     * @param type the report type
     * @return list of reports of the specified type
     */
    List<Report> getReportsByType(ReportType type);

    /**
     * Get reports by status
     * @param status the report status
     * @return list of reports with the specified status
     */
    List<Report> getReportsByStatus(ReportStatus status);

    /**
     * Generate report
     * @param type the report type
     * @param format the report format
     * @param period the report period
     * @return the generated report
     */
    Report generateReport(String type, String format, String period);

    /**
     * Get report types
     * @return list of available report types
     */
    List<String> getReportTypes();

    /**
     * Schedule report
     * @param type the report type
     * @param format the report format
     * @param period the report period
     * @param schedule the schedule
     * @return the scheduled report
     */
    Report scheduleReport(String type, String format, String period, String schedule);

    /**
     * Export report
     * @param id the report ID
     * @param format the export format
     * @return the exported report data
     */
    byte[] exportReport(Long id, String format);

    /**
     * Get report history
     * @param pageable pagination information
     * @return paginated list of report history
     */
    Page<Report> getReportHistory(Pageable pageable);

    /**
     * Generate scheduled reports
     * @param type the report type
     */
    void generateScheduledReports(String type);

    List<Report> getScheduledReports();
    Report updateReportSchedule(Long id, String type, String format, String schedule);
    void deleteReportSchedule(Long id);
    Object getReportStatistics();
    Object getReportAnalytics();
    Report duplicateReport(Long id);
    void archiveReport(Long id);
    void restoreReport(Long id);
    List<Report> getArchivedReports();
    Object getReportSummary();
    Object getAppointmentTrends();
    Object getPresentingConcerns();
    Object getRecentSessions();
    Object getAllReportData();
    byte[] exportReportLegacy(String format);
}