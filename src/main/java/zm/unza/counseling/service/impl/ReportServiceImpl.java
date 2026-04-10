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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
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
        String normalizedFormat = format == null ? "json" : format.toLowerCase(Locale.ROOT);
        String payload;

        if ("csv".equals(normalizedFormat)) {
            payload = "id,title,type,status,reportDate\n" +
                    report.getId() + "," +
                    escapeCsv(report.getTitle()) + "," +
                    escapeCsv(report.getType()) + "," +
                    escapeCsv(report.getStatus()) + "," +
                    (report.getReportDate() != null ? report.getReportDate() : "");
        } else {
            Map<String, Object> exportData = new LinkedHashMap<>();
            exportData.put("id", report.getId());
            exportData.put("title", report.getTitle());
            exportData.put("description", report.getDescription());
            exportData.put("type", report.getType());
            exportData.put("format", report.getFormat());
            exportData.put("status", report.getStatus());
            exportData.put("reportDate", report.getReportDate());
            exportData.put("reportData", report.getReportData() != null ? report.getReportData() : Collections.emptyMap());
            payload = writeJson(exportData);
        }

        return payload.getBytes(StandardCharsets.UTF_8);
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
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalReports", reportRepository.count());
        stats.put("completedReports", reportRepository.countByStatus(Report.ReportStatus.COMPLETED.name()));
        stats.put("pendingReports", reportRepository.countByStatus(Report.ReportStatus.PENDING.name()));
        stats.put("failedReports", reportRepository.countByStatus(Report.ReportStatus.FAILED.name()));
        stats.put("archivedReports", reportRepository.countByStatus("ARCHIVED"));
        stats.put("reportsByType", getReportTypes().stream()
                .collect(Collectors.toMap(type -> type, type -> Optional.ofNullable(reportRepository.countByType(type)).orElse(0L))));
        return stats;
    }

    @Override
    public Object getReportAnalytics() {
        return Map.of(
                "summary", getReportSummary(),
                "appointmentTrends", getAppointmentTrends(),
                "recentSessions", getRecentSessions()
        );
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
        return reportRepository.findAll().stream()
                .map(this::hydrateReport)
                .map(Report::getReportData)
                .filter(java.util.Objects::nonNull)
                .map(data -> String.valueOf(data.getOrDefault("presentingProblem", "General support")))
                .filter(concern -> concern != null && !concern.isBlank())
                .limit(10)
                .map(concern -> Map.of("concern", concern, "count", 1))
                .collect(Collectors.toList());
    }

    @Override
    public Object getRecentSessions() {
        return sessionRepository.findAll(PageRequest.of(0, 5)).getContent().stream()
                .map(session -> Map.of(
                        "sessionId", session.getId(),
                        "clientName", session.getStudent().getFullName(),
                        "counselorName", session.getCounselor().getFullName(),
                        "sessionDate", session.getSessionDate(),
                        "sessionType", session.getType().name(),
                        "outcome", session.getOutcome() != null ? session.getOutcome().name() : "UNKNOWN"
                ))
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
