package zm.unza.counseling.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Report entity for system reports and analytics
 */
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String format;

    @Column(nullable = false)
    private String status;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "counselor_id")
    private Long counselorId;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "report_date")
    private LocalDateTime reportDate;

    @Lob
    @Column(name = "report_data_json")
    @JsonIgnore
    private String reportDataJson;

    @Transient
    private Map<String, Object> reportData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Report() {}

    public Report(String title, String description, String type, String format) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.format = format;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getCounselorId() {
        return counselorId;
    }

    public void setCounselorId(Long counselorId) {
        this.counselorId = counselorId;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDateTime reportDate) {
        this.reportDate = reportDate;
    }

    public String getReportDataJson() {
        return reportDataJson;
    }

    public void setReportDataJson(String reportDataJson) {
        this.reportDataJson = reportDataJson;
    }

    public Map<String, Object> getReportData() {
        return reportData;
    }

    public void setReportData(Map<String, Object> reportData) {
        this.reportData = reportData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Enums for report types and formats
    public enum ReportType {
        DASHBOARD, ACADEMIC_PERFORMANCE, MENTAL_HEALTH_ANALYSIS, RISK_ASSESSMENT, APPOINTMENT_SUMMARY
    }

    public enum ReportFormat {
        PDF, EXCEL, CSV, JSON
    }

    public enum ReportStatus {
        PENDING, GENERATING, COMPLETED, FAILED
    }
}
