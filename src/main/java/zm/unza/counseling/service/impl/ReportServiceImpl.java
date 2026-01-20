package zm.unza.counseling.service.impl;

import zm.unza.counseling.entity.Report;
import zm.unza.counseling.entity.Report.ReportFormat;
import zm.unza.counseling.entity.Report.ReportType;
import zm.unza.counseling.entity.Report.ReportStatus;
import zm.unza.counseling.repository.ReportRepository;
import zm.unza.counseling.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of ReportService
 */
@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Override
    public Report generateReport(ReportType type, ReportFormat format) {
        Report report = new Report(
            type.name() + " Report",
            "Generated " + type.name() + " report in " + format.name() + " format",
            type.name(),
            format.name()
        );
        report.setStatus(ReportStatus.PENDING.name());
        return reportRepository.save(report);
    }

    @Override
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @Override
    public Page<Report> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }

    @Override
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
    }

    @Override
    public Report updateReportStatus(Long id, ReportStatus status) {
        Report report = getReportById(id);
        report.setStatus(status.name());
        return reportRepository.save(report);
    }

    @Override
    public Report updateReport(Long id, Report report) {
        Report existingReport = getReportById(id);
        existingReport.setTitle(report.getTitle());
        existingReport.setDescription(report.getDescription());
        existingReport.setType(report.getType());
        existingReport.setFormat(report.getFormat());
        existingReport.setStatus(report.getStatus());
        return reportRepository.save(existingReport);
    }

    @Override
    public void deleteReport(Long id) {
        Report report = getReportById(id);
        reportRepository.delete(report);
    }

    @Override
    public List<Report> getReportsByType(ReportType type) {
        return reportRepository.findByType(type.name());
    }

    @Override
    public List<Report> getReportsByStatus(ReportStatus status) {
        return reportRepository.findByStatus(status.name());
    }

    @Override
    public Report generateReport(String type, String format, String period) {
        Report report = new Report(
            type + " Report",
            "Generated " + type + " report in " + format + " format for " + period,
            type,
            format
        );
        report.setStatus(ReportStatus.PENDING.name());
        return reportRepository.save(report);
    }

    @Override
    public List<String> getReportTypes() {
        return reportRepository.findAllTypes();
    }

    @Override
    public Report scheduleReport(String type, String format, String period, String schedule) {
        Report report = new Report(
            type + " Scheduled Report",
            "Scheduled " + type + " report in " + format + " format for " + period,
            type,
            format
        );
        report.setStatus(Report.ReportStatus.PENDING.name());
        return reportRepository.save(report);
    }

    @Override
    public byte[] exportReport(Long id, String format) {
        Report report = getReportById(id);
        // Implementation would depend on the export format
        return new byte[0];
    }

    @Override
    public Page<Report> getReportHistory(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }

    @Override
    public void generateScheduledReports(String type) {
        // Implementation for generating scheduled reports
    }
}