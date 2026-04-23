package zm.unza.counseling.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.CounselorReportRequest;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.Report;
import zm.unza.counseling.entity.Session;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.CaseRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.ReportRepository;
import zm.unza.counseling.repository.SessionRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.AuditLogService;
import zm.unza.counseling.service.ReportService;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of ReportService
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ReportRepository reportRepository;
    private final ClientRepository clientRepository;
    private final CaseRepository caseRepository;
    private final AppointmentRepository appointmentRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Override
    public Report generateReport(Report.ReportType type, Report.ReportFormat format) {
        Report report = new Report(
                type.name() + " Report",
                "Generated " + type.name() + " report in " + format.name() + " format",
                type.name(),
                format.name()
        );
        report.setStatus(Report.ReportStatus.COMPLETED.name());
        report.setGeneratedAt(LocalDateTime.now());
        report.setReportDate(LocalDateTime.now());
        report.setReportData(buildGenericReportData(type.name(), null, null));
        report.setReportDataJson(writeJson(report.getReportData()));
        Report savedReport = reportRepository.save(report);
        auditReport("REPORT_GENERATED", savedReport, "Generic report generated");
        return hydrateReport(savedReport);
    }

    @Override
    public Report createCounselorReport(CounselorReportRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new NoSuchElementException("Client not found with id: " + request.getClientId()));

        Case caseEntity = resolveCase(request, client);
        Appointment appointment = resolveAppointment(request, caseEntity, client);
        Session session = resolveSession(request, appointment, client);
        User counselor = resolveCounselor(request, caseEntity, appointment, session);

        Report report = new Report();
        populateCounselorReport(report, request, client, counselor, caseEntity, appointment, session);
        Report savedReport = reportRepository.save(report);
        auditReport("REPORT_CREATED", savedReport, "Counselor report created");
        return hydrateReport(savedReport);
    }

    @Override
    public Report updateCounselorReport(Long id, CounselorReportRequest request) {
        Report report = getReportById(id);
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new NoSuchElementException("Client not found with id: " + request.getClientId()));

        Case caseEntity = resolveCase(request, client);
        Appointment appointment = resolveAppointment(request, caseEntity, client);
        Session session = resolveSession(request, appointment, client);
        User counselor = resolveCounselor(request, caseEntity, appointment, session);

        populateCounselorReport(report, request, client, counselor, caseEntity, appointment, session);
        Report savedReport = reportRepository.save(report);
        auditReport("REPORT_UPDATED", savedReport, "Counselor report updated");
        return hydrateReport(savedReport);
    }

    @Override
    public List<Report> getAllReports() {
        return reportRepository.findAll().stream().map(this::hydrateReport).collect(Collectors.toList());
    }

    @Override
    public Page<Report> getAllReports(Pageable pageable) {
        Page<Report> page = reportRepository.findAll(pageable);
        List<Report> hydrated = page.getContent().stream().map(this::hydrateReport).collect(Collectors.toList());
        return new PageImpl<>(hydrated, pageable, page.getTotalElements());
    }

    @Override
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .map(this::hydrateReport)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
    }

    @Override
    public Report updateReportStatus(Long id, Report.ReportStatus status) {
        Report report = getReportById(id);
        report.setStatus(status.name());
        report.setUpdatedAt(LocalDateTime.now());
        Report savedReport = reportRepository.save(report);
        auditReport("REPORT_STATUS_UPDATED", savedReport, "Report status updated");
        return hydrateReport(savedReport);
    }

    @Override
    public Report updateReport(Long id, Report report) {
        Report existingReport = getReportById(id);
        existingReport.setTitle(report.getTitle());
        existingReport.setDescription(report.getDescription());
        existingReport.setType(report.getType());
        existingReport.setFormat(report.getFormat());
        existingReport.setStatus(report.getStatus());
        existingReport.setClientId(report.getClientId());
        existingReport.setCounselorId(report.getCounselorId());
        existingReport.setCaseId(report.getCaseId());
        existingReport.setAppointmentId(report.getAppointmentId());
        existingReport.setSessionId(report.getSessionId());
        existingReport.setReportDate(report.getReportDate() != null ? report.getReportDate() : existingReport.getReportDate());
        if (report.getReportData() != null && !report.getReportData().isEmpty()) {
            existingReport.setReportData(report.getReportData());
            existingReport.setReportDataJson(writeJson(report.getReportData()));
        }
        existingReport.setUpdatedAt(LocalDateTime.now());
        Report savedReport = reportRepository.save(existingReport);
        auditReport("REPORT_UPDATED", savedReport, "Report updated");
        return hydrateReport(savedReport);
    }

    @Override
    public void deleteReport(Long id) {
        Report report = getReportById(id);
        auditReport("REPORT_DELETED", report, "Report deleted");
        reportRepository.delete(report);
    }

    @Override
    public List<Report> getReportsByType(Report.ReportType type) {
        return reportRepository.findByType(type.name()).stream().map(this::hydrateReport).collect(Collectors.toList());
    }

    @Override
    public List<Report> getReportsByStatus(Report.ReportStatus status) {
        return reportRepository.findByStatus(status.name()).stream().map(this::hydrateReport).collect(Collectors.toList());
    }

    @Override
    public Report generateReport(String type, String format, String period) {
        Report report = new Report(
                buildReportTitle(type, null, null),
                "Generated " + type + " report",
                type,
                format != null ? format : Report.ReportFormat.JSON.name()
        );
        report.setStatus(Report.ReportStatus.COMPLETED.name());
        report.setGeneratedAt(LocalDateTime.now());
        report.setReportDate(LocalDateTime.now());
        report.setReportData(buildGenericReportData(type, format, period));
        report.setReportDataJson(writeJson(report.getReportData()));
        Report savedReport = reportRepository.save(report);
        auditReport("REPORT_GENERATED", savedReport, "Report generated from generic endpoint");
        return hydrateReport(savedReport);
    }

    @Override
    public List<String> getReportTypes() {
        List<String> types = reportRepository.findAllTypes();
        if (!types.isEmpty()) {
            return types;
        }
        return List.of(
                "COUNSELOR_CASE_REPORT",
                Report.ReportType.DASHBOARD.name(),
                Report.ReportType.MENTAL_HEALTH_ANALYSIS.name(),
                Report.ReportType.RISK_ASSESSMENT.name(),
                Report.ReportType.APPOINTMENT_SUMMARY.name()
        );
    }

    @Override
    public Report scheduleReport(String type, String format, String period, String schedule) {
        Report report = new Report(
                type + " Scheduled Report",
                "Scheduled " + type + " report",
                type,
                format != null ? format : Report.ReportFormat.JSON.name()
        );
        report.setStatus("SCHEDULED");
        report.setReportDate(LocalDateTime.now());
        Map<String, Object> reportData = new LinkedHashMap<>();
        reportData.put("schedule", schedule);
        reportData.put("period", period);
        reportData.put("type", type);
        report.setReportData(reportData);
        report.setReportDataJson(writeJson(report.getReportData()));
        Report savedReport = reportRepository.save(report);
        auditReport("REPORT_SCHEDULED", savedReport, "Report scheduled");
        return hydrateReport(savedReport);
    }

    @Override
    public byte[] exportReport(Long id, String format) {
        Report report = getReportById(id);
        String normalizedFormat = normalizeExportFormat(format);
        Map<String, Object> structuredExportData = buildStructuredExportData(report);
        LinkedHashMap<String, String> flatExportData = flattenExportData(structuredExportData);

        return switch (normalizedFormat) {
            case "csv" -> buildDelimitedExport(flatExportData, ',').getBytes(StandardCharsets.UTF_8);
            case "excel" -> buildDelimitedExport(flatExportData, '\t').getBytes(StandardCharsets.UTF_8);
            case "json" -> writeJson(structuredExportData).getBytes(StandardCharsets.UTF_8);
            default -> renderSimplePdf(buildPdfLines(report, structuredExportData));
        };
    }

    @Override
    public Page<Report> getReportHistory(Pageable pageable) {
        return getAllReports(pageable);
    }

    @Override
    public void generateScheduledReports(String type) {
        log.info("Scheduled report generation requested for type {}", type);
    }

    @Override
    public List<Report> getScheduledReports() {
        return reportRepository.findByStatus("SCHEDULED").stream().map(this::hydrateReport).collect(Collectors.toList());
    }

    @Override
    public Report updateReportSchedule(Long id, String type, String format, String schedule) {
        Report report = getReportById(id);
        report.setType(type != null ? type : report.getType());
        report.setFormat(format != null ? format : report.getFormat());
        report.setStatus("SCHEDULED");
        Map<String, Object> reportData = hydrateReport(report).getReportData();
        Map<String, Object> updatedData = new LinkedHashMap<>(reportData != null ? reportData : Collections.emptyMap());
        updatedData.put("schedule", schedule);
        report.setReportData(updatedData);
        report.setReportDataJson(writeJson(updatedData));
        report.setUpdatedAt(LocalDateTime.now());
        Report savedReport = reportRepository.save(report);
        auditReport("REPORT_SCHEDULE_UPDATED", savedReport, "Report schedule updated");
        return hydrateReport(savedReport);
    }

    @Override
    public void deleteReportSchedule(Long id) {
        Report report = getReportById(id);
        report.setStatus(Report.ReportStatus.COMPLETED.name());
        Map<String, Object> reportData = hydrateReport(report).getReportData();
        if (reportData != null) {
            reportData.remove("schedule");
            report.setReportData(reportData);
            report.setReportDataJson(writeJson(reportData));
        }
        reportRepository.save(report);
        auditReport("REPORT_SCHEDULE_DELETED", report, "Report schedule deleted");
    }

    @Override
    public Object getReportStatistics() {
        List<Report> reports = reportRepository.findAll().stream()
                .map(this::hydrateReport)
                .collect(Collectors.toList());
        double averageGenerationTime = reports.stream()
                .filter(report -> report.getCreatedAt() != null && report.getGeneratedAt() != null)
                .mapToLong(report -> Math.max(ChronoUnit.MINUTES.between(report.getCreatedAt(), report.getGeneratedAt()), 0))
                .average()
                .orElse(0);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalReports", reports.size());
        stats.put("scheduledReports", reports.stream().filter(report -> "SCHEDULED".equalsIgnoreCase(report.getStatus())).count());
        stats.put("generatedReports", reports.stream().filter(report -> Report.ReportStatus.COMPLETED.name().equalsIgnoreCase(report.getStatus())).count());
        stats.put("completedReports", reportRepository.countByStatus(Report.ReportStatus.COMPLETED.name()));
        stats.put("pendingReports", reportRepository.countByStatus(Report.ReportStatus.PENDING.name()));
        stats.put("failedReports", reportRepository.countByStatus(Report.ReportStatus.FAILED.name()));
        stats.put("archivedReports", reportRepository.countByStatus("ARCHIVED"));
        stats.put("totalDownloads", 0L);
        stats.put("averageGenerationTime", Math.round(averageGenerationTime * 10.0) / 10.0);
        stats.put("reportsByType", getReportTypes().stream()
                .collect(Collectors.toMap(type -> type, type -> Optional.ofNullable(reportRepository.countByType(type)).orElse(0L))));
        return stats;
    }

    @Override
    public Object getReportAnalytics() {
        List<Report> reports = reportRepository.findAll().stream()
                .map(this::hydrateReport)
                .collect(Collectors.toList());

        List<Map<String, Object>> downloadsByType = reports.stream()
                .collect(Collectors.groupingBy(
                        report -> report.getType() != null ? report.getType() : "UNKNOWN",
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> mostPopularReports = reports.stream()
                .sorted(Comparator.comparing(Report::getUpdatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .limit(5)
                .map(report -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("title", report.getTitle());
                    item.put("downloads", 0L);
                    return item;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> generationTimes = reports.stream()
                .filter(report -> report.getCreatedAt() != null && report.getGeneratedAt() != null)
                .sorted(Comparator.comparing(Report::getGeneratedAt, Comparator.nullsLast(LocalDateTime::compareTo)))
                .limit(10)
                .map(report -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("date", report.getGeneratedAt());
                    item.put("time", Math.max(ChronoUnit.MINUTES.between(report.getCreatedAt(), report.getGeneratedAt()), 0));
                    return item;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> userActivity = reports.stream()
                .filter(report -> report.getCounselorId() != null)
                .collect(Collectors.groupingBy(Report::getCounselorId, LinkedHashMap::new, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("userId", String.valueOf(entry.getKey()));
                    item.put("userName", userRepository.findById(entry.getKey())
                            .map(User::getFullName)
                            .orElse("Unknown User"));
                    item.put("downloads", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("downloadsByType", downloadsByType);
        analytics.put("mostPopularReports", mostPopularReports);
        analytics.put("generationTimes", generationTimes);
        analytics.put("userActivity", userActivity);
        return analytics;
    }

    @Override
    public Report duplicateReport(Long id) {
        Report source = getReportById(id);
        Report duplicate = new Report();
        duplicate.setTitle(source.getTitle() + " Copy");
        duplicate.setDescription(source.getDescription());
        duplicate.setType(source.getType());
        duplicate.setFormat(source.getFormat());
        duplicate.setStatus(Report.ReportStatus.PENDING.name());
        duplicate.setGeneratedAt(null);
        duplicate.setFilePath(source.getFilePath());
        duplicate.setFileSize(source.getFileSize());
        duplicate.setClientId(source.getClientId());
        duplicate.setCounselorId(source.getCounselorId());
        duplicate.setCaseId(source.getCaseId());
        duplicate.setAppointmentId(source.getAppointmentId());
        duplicate.setSessionId(source.getSessionId());
        duplicate.setReportDate(LocalDateTime.now());
        duplicate.setReportData(source.getReportData());
        duplicate.setReportDataJson(source.getReportDataJson());
        duplicate.setCreatedAt(LocalDateTime.now());
        duplicate.setUpdatedAt(LocalDateTime.now());
        Report savedDuplicate = reportRepository.save(duplicate);
        auditReport("REPORT_DUPLICATED", savedDuplicate, "Report duplicated");
        return hydrateReport(savedDuplicate);
    }

    @Override
    public void archiveReport(Long id) {
        Report report = getReportById(id);
        report.setStatus("ARCHIVED");
        report.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);
        auditReport("REPORT_ARCHIVED", report, "Report archived");
    }

    @Override
    public void restoreReport(Long id) {
        Report report = getReportById(id);
        report.setStatus(Report.ReportStatus.COMPLETED.name());
        report.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);
        auditReport("REPORT_RESTORED", report, "Report restored");
    }

    @Override
    public List<Report> getArchivedReports() {
        return reportRepository.findByStatus("ARCHIVED").stream().map(this::hydrateReport).collect(Collectors.toList());
    }

    @Override
    public Object getReportSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalReports", reportRepository.count());
        summary.put("scheduledReports", reportRepository.findByStatus("SCHEDULED").size());
        summary.put("archivedReports", reportRepository.findByStatus("ARCHIVED").size());
        summary.put("lastGeneratedAt", reportRepository.findAll().stream()
                .map(Report::getGeneratedAt)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        return summary;
    }

    @Override
    public Object getAppointmentTrends() {
        List<Map<String, Object>> trends = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int offset = 5; offset >= 0; offset--) {
            YearMonth month = YearMonth.from(now.minusMonths(offset));
            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);
            List<Appointment> monthAppointments = appointmentRepository
                    .findByAppointmentDateBetween(start, end, Pageable.unpaged())
                    .getContent();

            Map<String, Object> monthData = new LinkedHashMap<>();
            monthData.put("month", month.toString());
            monthData.put("scheduled", monthAppointments.stream().filter(item -> item.getStatus() == Appointment.AppointmentStatus.SCHEDULED).count());
            monthData.put("completed", monthAppointments.stream().filter(item -> item.getStatus() == Appointment.AppointmentStatus.COMPLETED).count());
            monthData.put("cancelled", monthAppointments.stream().filter(item -> item.getStatus() == Appointment.AppointmentStatus.CANCELLED).count());
            trends.add(monthData);
        }
        return trends;
    }

    @Override
    public Object getPresentingConcerns() {
        String[] colors = {"#2E7D32", "#0288D1", "#ED6C02", "#9C27B0", "#D32F2F", "#455A64"};

        Map<String, Long> concernCounts = reportRepository.findAll().stream()
                .map(this::hydrateReport)
                .map(Report::getReportData)
                .filter(java.util.Objects::nonNull)
                .map(data -> String.valueOf(data.getOrDefault("presentingProblem", "General support")))
                .filter(concern -> concern != null && !concern.isBlank())
                .collect(Collectors.groupingBy(
                        concern -> concern,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        List<Map<String, Object>> concerns = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Long> entry : concernCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList())) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            item.put("color", colors[index % colors.length]);
            concerns.add(item);
            index++;
        }
        return concerns;
    }

    @Override
    public Object getRecentSessions() {
        return sessionRepository.findAll(PageRequest.of(0, 5)).getContent().stream()
                .map(session -> {
                    String clientName = session.getClient() != null
                            ? session.getClient().getFullName()
                            : session.getStudent() != null ? session.getStudent().getFullName() : "Unknown Client";
                    String riskLevel = session.getClient() != null && session.getClient().getRiskLevel() != null
                            ? toTitleCase(session.getClient().getRiskLevel().name())
                            : "Low";
                    String followUp = Boolean.TRUE.equals(session.getFollowUpRequired())
                            ? session.getFollowUpDate() != null ? session.getFollowUpDate().toString() : "Required"
                            : "Not required";

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("client", clientName);
                    item.put("date", session.getSessionDate());
                    item.put("type", session.getType() != null ? session.getType().getDisplayName() : "Unknown");
                    item.put("riskLevel", riskLevel);
                    item.put("followUp", followUp);
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Object getAllReportData() {
        return Map.of(
                "reports", getAllReports(),
                "statistics", getReportStatistics(),
                "appointmentTrends", getAppointmentTrends(),
                "recentSessions", getRecentSessions()
        );
    }

    @Override
    public byte[] exportReportLegacy(String format) {
        StringBuilder csv = new StringBuilder();
        csv.append("id,title,type,status,createdAt\n");
        for (Report report : reportRepository.findAll()) {
            csv.append(report.getId()).append(",")
                    .append(escapeCsv(report.getTitle())).append(",")
                    .append(escapeCsv(report.getType())).append(",")
                    .append(escapeCsv(report.getStatus())).append(",")
                    .append(report.getCreatedAt()).append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String normalizeExportFormat(String format) {
        String normalized = format == null ? "pdf" : format.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "csv" -> "csv";
            case "excel", "xls", "xlsx" -> "excel";
            case "json" -> "json";
            default -> "pdf";
        };
    }

    private Map<String, Object> buildStructuredExportData(Report report) {
        Report hydratedReport = hydrateReport(report);
        Map<String, Object> reportData = hydratedReport.getReportData() != null
                ? new LinkedHashMap<>(hydratedReport.getReportData())
                : new LinkedHashMap<>();

        Client client = hydratedReport.getClientId() != null
                ? clientRepository.findById(hydratedReport.getClientId()).orElse(null)
                : null;
        User counselor = hydratedReport.getCounselorId() != null
                ? userRepository.findById(hydratedReport.getCounselorId()).orElse(null)
                : null;
        Case caseEntity = hydratedReport.getCaseId() != null
                ? caseRepository.findById(hydratedReport.getCaseId()).orElse(null)
                : null;
        Appointment appointment = hydratedReport.getAppointmentId() != null
                ? appointmentRepository.findById(hydratedReport.getAppointmentId()).orElse(null)
                : null;
        Session session = hydratedReport.getSessionId() != null
                ? sessionRepository.findById(hydratedReport.getSessionId()).orElse(null)
                : null;

        Map<String, Object> exportData = new LinkedHashMap<>();
        Map<String, Object> reportSection = new LinkedHashMap<>();
        putIfMeaningful(reportSection, "id", hydratedReport.getId());
        putIfMeaningful(reportSection, "title", hydratedReport.getTitle());
        putIfMeaningful(reportSection, "description", hydratedReport.getDescription());
        putIfMeaningful(reportSection, "type", hydratedReport.getType());
        putIfMeaningful(reportSection, "storedFormat", hydratedReport.getFormat());
        putIfMeaningful(reportSection, "status", hydratedReport.getStatus());
        putIfMeaningful(reportSection, "reportDate", hydratedReport.getReportDate());
        putIfMeaningful(reportSection, "generatedAt", hydratedReport.getGeneratedAt());
        putIfMeaningful(reportSection, "createdAt", hydratedReport.getCreatedAt());
        putIfMeaningful(reportSection, "updatedAt", hydratedReport.getUpdatedAt());
        putIfMeaningful(reportSection, "filePath", hydratedReport.getFilePath());
        putIfMeaningful(reportSection, "fileSize", hydratedReport.getFileSize());
        exportData.put("report", reportSection);

        Map<String, Object> clientSection = new LinkedHashMap<>();
        putIfMeaningful(clientSection, "id", hydratedReport.getClientId());
        putIfMeaningful(clientSection, "name", client != null ? client.getFullName() : reportData.get("clientName"));
        putIfMeaningful(clientSection, "email", client != null ? client.getEmail() : null);
        putIfMeaningful(clientSection, "studentId", client != null ? client.getStudentId() : null);
        putIfMeaningful(clientSection, "programme", client != null ? client.getProgramme() : null);
        putIfMeaningful(clientSection, "faculty", client != null ? client.getFaculty() : null);
        putIfMeaningful(clientSection, "yearOfStudy", client != null ? client.getYearOfStudy() : null);
        putIfMeaningful(clientSection, "riskLevel", client != null && client.getRiskLevel() != null ? client.getRiskLevel().name() : null);
        putIfMeaningful(clientSection, "clientStatus", client != null && client.getClientStatus() != null ? client.getClientStatus().name() : null);
        exportData.put("client", clientSection);

        Map<String, Object> counselorSection = new LinkedHashMap<>();
        putIfMeaningful(counselorSection, "id", hydratedReport.getCounselorId());
        putIfMeaningful(counselorSection, "name", counselor != null ? counselor.getFullName() : reportData.get("counselorName"));
        putIfMeaningful(counselorSection, "email", counselor != null ? counselor.getEmail() : null);
        putIfMeaningful(counselorSection, "phoneNumber", counselor != null ? counselor.getPhoneNumber() : null);
        putIfMeaningful(counselorSection, "specialization", counselor != null ? counselor.getSpecialization() : null);
        exportData.put("counselor", counselorSection);

        Map<String, Object> caseSection = new LinkedHashMap<>();
        putIfMeaningful(caseSection, "id", hydratedReport.getCaseId());
        putIfMeaningful(caseSection, "caseNumber", caseEntity != null ? caseEntity.getCaseNumber() : reportData.get("caseNumber"));
        putIfMeaningful(caseSection, "status", caseEntity != null && caseEntity.getStatus() != null ? caseEntity.getStatus().name() : reportData.get("caseStatus"));
        putIfMeaningful(caseSection, "priority", caseEntity != null && caseEntity.getPriority() != null ? caseEntity.getPriority().name() : reportData.get("casePriority"));
        putIfMeaningful(caseSection, "subject", caseEntity != null ? caseEntity.getSubject() : reportData.get("caseSubject"));
        putIfMeaningful(caseSection, "description", caseEntity != null ? caseEntity.getDescription() : null);
        putIfMeaningful(caseSection, "notes", caseEntity != null ? caseEntity.getNotes() : null);
        putIfMeaningful(caseSection, "assignedBy", caseEntity != null ? caseEntity.getAssignedBy() : null);
        putIfMeaningful(caseSection, "assignedAt", caseEntity != null ? caseEntity.getAssignedAt() : null);
        putIfMeaningful(caseSection, "lastActivityAt", caseEntity != null ? caseEntity.getLastActivityAt() : null);
        putIfMeaningful(caseSection, "expectedResolutionDate", caseEntity != null ? caseEntity.getExpectedResolutionDate() : null);
        putIfMeaningful(caseSection, "actualResolutionDate", caseEntity != null ? caseEntity.getActualResolutionDate() : null);
        putIfMeaningful(caseSection, "closedAt", caseEntity != null ? caseEntity.getClosedAt() : null);
        putIfMeaningful(caseSection, "escalationLevel", caseEntity != null ? caseEntity.getEscalationLevel() : null);
        putIfMeaningful(caseSection, "tags", caseEntity != null ? caseEntity.getTags() : null);
        putIfMeaningful(caseSection, "customFields", caseEntity != null ? caseEntity.getCustomFields() : null);
        putIfMeaningful(caseSection, "appointmentCount", caseEntity != null ? appointmentRepository.countByCaseEntity(caseEntity) : null);
        exportData.put("case", caseSection);

        Map<String, Object> appointmentSection = new LinkedHashMap<>();
        putIfMeaningful(appointmentSection, "id", hydratedReport.getAppointmentId());
        putIfMeaningful(appointmentSection, "title", appointment != null ? appointment.getTitle() : null);
        putIfMeaningful(appointmentSection, "appointmentDate", appointment != null ? appointment.getAppointmentDate() : reportData.get("appointmentDate"));
        putIfMeaningful(appointmentSection, "durationMinutes", appointment != null ? appointment.getDuration() : null);
        putIfMeaningful(appointmentSection, "type", appointment != null && appointment.getType() != null ? appointment.getType().name() : reportData.get("appointmentType"));
        putIfMeaningful(appointmentSection, "status", appointment != null && appointment.getStatus() != null ? appointment.getStatus().name() : reportData.get("appointmentStatus"));
        putIfMeaningful(appointmentSection, "sessionMode", appointment != null && appointment.getSessionMode() != null ? appointment.getSessionMode().name() : null);
        putIfMeaningful(appointmentSection, "location", appointment != null ? appointment.getLocation() : null);
        putIfMeaningful(appointmentSection, "meetingProvider", appointment != null ? appointment.getMeetingProvider() : null);
        putIfMeaningful(appointmentSection, "description", appointment != null ? appointment.getDescription() : null);
        exportData.put("appointment", appointmentSection);

        Map<String, Object> sessionSection = new LinkedHashMap<>();
        putIfMeaningful(sessionSection, "id", hydratedReport.getSessionId());
        putIfMeaningful(sessionSection, "title", session != null ? session.getTitle() : null);
        putIfMeaningful(sessionSection, "sessionDate", session != null ? session.getSessionDate() : reportData.get("sessionDate"));
        putIfMeaningful(sessionSection, "type", session != null && session.getType() != null ? session.getType().name() : null);
        putIfMeaningful(sessionSection, "status", session != null && session.getStatus() != null ? session.getStatus().name() : null);
        putIfMeaningful(sessionSection, "outcome", session != null && session.getOutcome() != null ? session.getOutcome().name() : reportData.get("sessionOutcome"));
        putIfMeaningful(sessionSection, "presentingIssue", session != null ? session.getPresentingIssue() : reportData.get("sessionPresentingIssue"));
        putIfMeaningful(sessionSection, "sessionNotes", session != null ? session.getSessionNotes() : null);
        putIfMeaningful(sessionSection, "interventions", session != null ? session.getInterventions() : null);
        putIfMeaningful(sessionSection, "recommendations", session != null ? session.getRecommendations() : null);
        putIfMeaningful(sessionSection, "treatmentPlan", session != null ? session.getTreatmentPlan() : null);
        putIfMeaningful(sessionSection, "followUpRequired", session != null ? session.getFollowUpRequired() : null);
        putIfMeaningful(sessionSection, "followUpDate", session != null ? session.getFollowUpDate() : null);
        putIfMeaningful(sessionSection, "followUpNotes", session != null ? session.getFollowUpNotes() : null);
        exportData.put("session", sessionSection);

        exportData.put("reportData", reportData);
        return exportData;
    }

    private LinkedHashMap<String, String> flattenExportData(Map<String, Object> structuredExportData) {
        LinkedHashMap<String, String> flat = new LinkedHashMap<>();
        flattenExportValue(null, structuredExportData, flat);
        return flat;
    }

    @SuppressWarnings("unchecked")
    private void flattenExportValue(String prefix, Object value, Map<String, String> target) {
        if (value == null) {
            return;
        }

        if (value instanceof Map<?, ?> mapValue) {
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                String childKey = prefix == null
                        ? String.valueOf(entry.getKey())
                        : prefix + "." + entry.getKey();
                flattenExportValue(childKey, entry.getValue(), target);
            }
            return;
        }

        if (prefix == null || prefix.isBlank()) {
            return;
        }

        target.put(prefix, formatExportValue(value));
    }

    private String buildDelimitedExport(Map<String, String> flatExportData, char delimiter) {
        String separator = String.valueOf(delimiter);
        String headerRow = flatExportData.keySet().stream()
                .map(value -> escapeDelimitedValue(value, delimiter))
                .collect(Collectors.joining(separator));
        String valueRow = flatExportData.values().stream()
                .map(value -> escapeDelimitedValue(value, delimiter))
                .collect(Collectors.joining(separator));
        return headerRow + "\n" + valueRow + "\n";
    }

    private List<String> buildPdfLines(Report report, Map<String, Object> structuredExportData) {
        List<String> lines = new ArrayList<>();
        lines.add(report.getTitle() != null && !report.getTitle().isBlank()
                ? report.getTitle()
                : "Counseling Report Export");
        lines.add("Downloaded At: " + LocalDateTime.now());
        lines.add("");
        appendPdfSection(lines, "Report", structuredExportData.get("report"));
        appendPdfSection(lines, "Client", structuredExportData.get("client"));
        appendPdfSection(lines, "Counselor", structuredExportData.get("counselor"));
        appendPdfSection(lines, "Case File", structuredExportData.get("case"));
        appendPdfSection(lines, "Appointment", structuredExportData.get("appointment"));
        appendPdfSection(lines, "Session", structuredExportData.get("session"));
        appendPdfSection(lines, "Report Details", structuredExportData.get("reportData"));
        return lines;
    }

    @SuppressWarnings("unchecked")
    private void appendPdfSection(List<String> lines, String title, Object sectionValue) {
        if (!(sectionValue instanceof Map<?, ?> rawSection)) {
            return;
        }

        Map<String, Object> section = (Map<String, Object>) rawSection;
        if (!hasMeaningfulValues(section)) {
            return;
        }

        lines.add(title);
        for (Map.Entry<String, Object> entry : section.entrySet()) {
            if (!isMeaningfulValue(entry.getValue())) {
                continue;
            }

            List<String> wrapped = wrapText(
                    toDisplayLabel(entry.getKey()) + ": " + formatExportValue(entry.getValue()),
                    92
            );
            lines.addAll(wrapped);
        }
        lines.add("");
    }

    private byte[] renderSimplePdf(List<String> lines) {
        List<List<String>> pages = paginateLines(lines.isEmpty() ? List.of("Counseling Report Export") : lines, 45);
        int pageCount = pages.size();
        int lastObjectNumber = 3 + (pageCount * 2);
        int[] offsets = new int[lastObjectNumber + 1];
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        writePdfBytes(output, "%PDF-1.4\n");
        writePdfObject(output, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");

        StringBuilder pageReferences = new StringBuilder();
        for (int index = 0; index < pageCount; index++) {
            pageReferences.append(4 + (index * 2)).append(" 0 R ");
        }
        writePdfObject(output, offsets, 2,
                "<< /Type /Pages /Kids [" + pageReferences + "] /Count " + pageCount + " >>");
        writePdfObject(output, offsets, 3,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");

        for (int index = 0; index < pageCount; index++) {
            int pageObjectNumber = 4 + (index * 2);
            int contentObjectNumber = 5 + (index * 2);
            String pageContent = buildPdfPageContent(pages.get(index));
            int contentLength = pageContent.getBytes(StandardCharsets.US_ASCII).length;
            writePdfObject(output, offsets, contentObjectNumber,
                    "<< /Length " + contentLength + " >>\nstream\n" + pageContent + "endstream");
            writePdfObject(output, offsets, pageObjectNumber,
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] " +
                            "/Resources << /Font << /F1 3 0 R >> >> /Contents " +
                            contentObjectNumber + " 0 R >>");
        }

        int startXref = output.size();
        writePdfBytes(output, "xref\n0 " + (lastObjectNumber + 1) + "\n");
        writePdfBytes(output, "0000000000 65535 f \n");
        for (int objectNumber = 1; objectNumber <= lastObjectNumber; objectNumber++) {
            writePdfBytes(output, String.format(Locale.ROOT, "%010d 00000 n \n", offsets[objectNumber]));
        }

        writePdfBytes(output,
                "trailer\n<< /Size " + (lastObjectNumber + 1) + " /Root 1 0 R >>\nstartxref\n" +
                        startXref + "\n%%EOF");
        return output.toByteArray();
    }

    private String buildPdfPageContent(List<String> pageLines) {
        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 12 Tf\n50 790 Td\n");
        for (int index = 0; index < pageLines.size(); index++) {
            if (index > 0) {
                content.append("0 -16 Td\n");
            }
            content.append("(")
                    .append(sanitizePdfText(pageLines.get(index)))
                    .append(") Tj\n");
        }
        content.append("ET\n");
        return content.toString();
    }

    private List<List<String>> paginateLines(List<String> lines, int linesPerPage) {
        List<List<String>> pages = new ArrayList<>();
        for (int start = 0; start < lines.size(); start += linesPerPage) {
            int end = Math.min(start + linesPerPage, lines.size());
            pages.add(new ArrayList<>(lines.subList(start, end)));
        }
        return pages.isEmpty() ? List.of(new ArrayList<>()) : pages;
    }

    private void writePdfObject(ByteArrayOutputStream output, int[] offsets, int objectNumber, String body) {
        offsets[objectNumber] = output.size();
        writePdfBytes(output, objectNumber + " 0 obj\n" + body + "\nendobj\n");
    }

    private void writePdfBytes(ByteArrayOutputStream output, String value) {
        output.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }

    private String sanitizePdfText(String value) {
        String normalized = value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("\t", " ");
        StringBuilder sanitized = new StringBuilder();
        for (char character : normalized.toCharArray()) {
            sanitized.append(character >= 32 && character <= 126 ? character : '?');
        }
        return sanitized.toString();
    }

    private List<String> wrapText(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return List.of("");
        }

        List<String> wrapped = new ArrayList<>();
        String remaining = text.trim();
        while (remaining.length() > maxLength) {
            int breakIndex = remaining.lastIndexOf(' ', maxLength);
            if (breakIndex <= 0) {
                breakIndex = maxLength;
            }
            wrapped.add(remaining.substring(0, breakIndex).trim());
            remaining = remaining.substring(breakIndex).trim();
        }

        if (!remaining.isEmpty()) {
            wrapped.add(remaining);
        }
        return wrapped;
    }

    private String formatExportValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::formatExportValue)
                    .filter(item -> item != null && !item.isBlank())
                    .collect(Collectors.joining("; "));
        }
        if (value instanceof Map<?, ?> mapValue) {
            return writeJson(mapValue);
        }
        return String.valueOf(value);
    }

    private String escapeDelimitedValue(String value, char delimiter) {
        String safeValue = value == null ? "" : value;
        boolean needsQuotes = safeValue.indexOf(delimiter) >= 0
                || safeValue.contains("\"")
                || safeValue.contains("\n")
                || safeValue.contains("\r");
        String escaped = safeValue.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }

    private void putIfMeaningful(Map<String, Object> target, String key, Object value) {
        if (isMeaningfulValue(value)) {
            target.put(key, value);
        }
    }

    private boolean hasMeaningfulValues(Map<String, Object> values) {
        return values.values().stream().anyMatch(this::isMeaningfulValue);
    }

    private boolean isMeaningfulValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String stringValue) {
            return !stringValue.isBlank();
        }
        if (value instanceof Collection<?> collectionValue) {
            return !collectionValue.isEmpty();
        }
        if (value instanceof Map<?, ?> mapValue) {
            return !mapValue.isEmpty();
        }
        return true;
    }

    private String toDisplayLabel(String key) {
        if (key == null || key.isBlank()) {
            return "Field";
        }
        String normalized = key
                .replace('.', ' ')
                .replace('_', ' ')
                .replaceAll("([a-z])([A-Z])", "$1 $2");
        return toTitleCase(normalized);
    }

    private void populateCounselorReport(
            Report report,
            CounselorReportRequest request,
            Client client,
            User counselor,
            Case caseEntity,
            Appointment appointment,
            Session session
    ) {
        LocalDateTime now = LocalDateTime.now();
        if (report.getCreatedAt() == null) {
            report.setCreatedAt(now);
        }
        report.setTitle(buildReportTitle("COUNSELOR_CASE_REPORT", client, caseEntity));
        report.setDescription(request.getPresentingProblem());
        report.setType("COUNSELOR_CASE_REPORT");
        report.setFormat(Report.ReportFormat.JSON.name());
        report.setStatus(Report.ReportStatus.COMPLETED.name());
        report.setGeneratedAt(now);
        report.setReportDate(now);
        report.setClientId(client.getId());
        report.setCounselorId(counselor != null ? counselor.getId() : null);
        report.setCaseId(caseEntity != null ? caseEntity.getId() : null);
        report.setAppointmentId(appointment != null ? appointment.getId() : null);
        report.setSessionId(session != null ? session.getId() : null);

        Map<String, Object> reportData = buildCounselorReportData(request, client, counselor, caseEntity, appointment, session);
        report.setReportData(reportData);
        report.setReportDataJson(writeJson(reportData));
        report.setUpdatedAt(now);
    }

    private Map<String, Object> buildCounselorReportData(
            CounselorReportRequest request,
            Client client,
            User counselor,
            Case caseEntity,
            Appointment appointment,
            Session session
    ) {
        Map<String, Object> reportData = new LinkedHashMap<>();
        reportData.put("clientId", client.getId());
        reportData.put("clientName", client.getFullName());
        reportData.put("counselorId", counselor != null ? counselor.getId() : null);
        reportData.put("counselorName", counselor != null ? counselor.getFullName() : null);
        reportData.put("reportDate", LocalDateTime.now());
        reportData.put("caseId", caseEntity != null ? caseEntity.getId() : null);
        reportData.put("caseNumber", caseEntity != null ? caseEntity.getCaseNumber() : request.getCaseNumber());
        reportData.put("appointmentId", appointment != null ? appointment.getId() : null);
        reportData.put("sessionId", session != null ? session.getId() : null);
        reportData.put("descriptionBackground", request.getDescriptionBackground());
        reportData.put("presentingProblem", request.getPresentingProblem());
        reportData.put("assessment", request.getAssessment());
        reportData.put("conclusion", request.getConclusion());
        reportData.put("recommendations", request.getRecommendations());
        reportData.put("sessionSummary", request.getSessionSummary());
        reportData.put("progressNotes", request.getProgressNotes());
        reportData.put("followUpPlan", request.getFollowUpPlan());
        reportData.put("riskAssessment", request.getRiskAssessment());
        if (appointment != null) {
            reportData.put("appointmentDate", appointment.getAppointmentDate());
            reportData.put("appointmentType", appointment.getType().name());
            reportData.put("appointmentStatus", appointment.getStatus().name());
        }
        if (session != null) {
            reportData.put("sessionDate", session.getSessionDate());
            reportData.put("sessionOutcome", session.getOutcome() != null ? session.getOutcome().name() : null);
            reportData.put("sessionPresentingIssue", session.getPresentingIssue());
        }
        if (caseEntity != null) {
            reportData.put("caseStatus", caseEntity.getStatus().name());
            reportData.put("casePriority", caseEntity.getPriority().name());
            reportData.put("caseSubject", caseEntity.getSubject());
        }
        return reportData;
    }

    private Case resolveCase(CounselorReportRequest request, Client client) {
        if (request.getCaseId() != null) {
            Case caseEntity = caseRepository.findById(request.getCaseId())
                    .orElseThrow(() -> new NoSuchElementException("Case not found with id: " + request.getCaseId()));
            validateCaseClient(caseEntity, client);
            return caseEntity;
        }
        if (request.getCaseNumber() != null && !request.getCaseNumber().isBlank()) {
            Case caseEntity = caseRepository.findByCaseNumber(request.getCaseNumber())
                    .orElseThrow(() -> new NoSuchElementException("Case not found with number: " + request.getCaseNumber()));
            validateCaseClient(caseEntity, client);
            return caseEntity;
        }

        List<Case> activeCases = caseRepository.findRecentCasesByClientAndStatuses(
                client,
                List.of(Case.CaseStatus.OPEN, Case.CaseStatus.IN_PROGRESS, Case.CaseStatus.ON_HOLD)
        );
        return activeCases.isEmpty() ? null : activeCases.get(0);
    }

    private Appointment resolveAppointment(CounselorReportRequest request, Case caseEntity, Client client) {
        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + request.getAppointmentId()));
            validateAppointmentClient(appointment, client);
            return appointment;
        }
        if (caseEntity != null) {
            List<Appointment> appointments = appointmentRepository.findByCaseEntity(caseEntity);
            if (!appointments.isEmpty()) {
                appointments.sort((left, right) -> right.getAppointmentDate().compareTo(left.getAppointmentDate()));
                return appointments.get(0);
            }
        }
        return null;
    }

    private Session resolveSession(CounselorReportRequest request, Appointment appointment, Client client) {
        if (request.getSessionId() != null) {
            return sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new NoSuchElementException("Session not found with id: " + request.getSessionId()));
        }
        if (appointment != null) {
            return sessionRepository.findFirstByAppointmentIdOrderBySessionDateDesc(appointment.getId()).orElse(null);
        }
        List<Session> sessions = sessionRepository.findByStudentIdOrderBySessionDateDesc(client.getId());
        return sessions.isEmpty() ? null : sessions.get(0);
    }

    private User resolveCounselor(CounselorReportRequest request, Case caseEntity, Appointment appointment, Session session) {
        if (request.getCounselorId() != null) {
            return userRepository.findById(request.getCounselorId())
                    .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + request.getCounselorId()));
        }
        if (session != null) {
            return session.getCounselor();
        }
        if (appointment != null && appointment.getCounselor() != null) {
            return appointment.getCounselor();
        }
        if (caseEntity != null && caseEntity.getCounselor() != null) {
            return caseEntity.getCounselor();
        }
        return null;
    }

    private void validateCaseClient(Case caseEntity, Client client) {
        if (caseEntity.getClient() == null || !caseEntity.getClient().getId().equals(client.getId())) {
            throw new IllegalStateException("Selected case does not belong to the requested client");
        }
    }

    private void validateAppointmentClient(Appointment appointment, Client client) {
        Long appointmentClientId = appointment.getClient() != null ? appointment.getClient().getId() : null;
        Long appointmentStudentId = appointment.getStudent() != null ? appointment.getStudent().getId() : null;
        if ((appointmentClientId != null && appointmentClientId.equals(client.getId()))
                || (appointmentStudentId != null && appointmentStudentId.equals(client.getId()))) {
            return;
        }
        throw new IllegalStateException("Selected appointment does not belong to the requested client");
    }

    private String buildReportTitle(String reportType, Client client, Case caseEntity) {
        if ("COUNSELOR_CASE_REPORT".equalsIgnoreCase(reportType) && client != null) {
            String casePart = caseEntity != null ? " - " + caseEntity.getCaseNumber() : "";
            return "Counselor Report - " + client.getFullName() + casePart;
        }
        return reportType + " Report";
    }

    private Map<String, Object> buildGenericReportData(String type, String format, String period) {
        return new LinkedHashMap<>(Map.of(
                "type", type,
                "format", format != null ? format : Report.ReportFormat.JSON.name(),
                "period", period != null ? period : "all-time",
                "generatedAt", LocalDateTime.now()
        ));
    }

    private Report hydrateReport(Report report) {
        if (report.getReportData() == null || report.getReportData().isEmpty()) {
            report.setReportData(readJson(report.getReportDataJson()));
        }
        return report;
    }

    private Map<String, Object> readJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to parse report data json", exception);
            return Collections.emptyMap();
        }
    }

    private String writeJson(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to write report data json", exception);
            return "{}";
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String toTitleCase(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT).replace('_', ' ');
        if (normalized.isBlank()) {
            return "Unknown";
        }

        StringBuilder builder = new StringBuilder();
        for (String part : normalized.split(" ")) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private void auditReport(String action, Report report, String details) {
        auditLogService.logAction(
                action,
                "REPORT",
                String.valueOf(report.getId()),
                details,
                "system",
                "system",
                true
        );
    }
}
