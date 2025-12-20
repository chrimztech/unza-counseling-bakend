package zm.unza.counseling.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Set;
import java.util.HashSet;
import java.time.LocalDateTime;

/**
 * Client Entity - Extends User to represent students/clients receiving counseling services
 */
@Entity
@Table(name = "clients")
@EntityListeners(AuditingEntityListener.class)
public class Client extends User {

    @Column(name = "student_id", unique = true)
    private String studentId;

    @Column(name = "programme")
    private String programme;

    @Column(name = "faculty")
    private String faculty;

    @Column(name = "year_of_study")
    private Integer yearOfStudy;

    @Column(name = "gpa")
    private Double gpa;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_status", nullable = false)
    private ClientStatus clientStatus = ClientStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column(name = "risk_score")
    private Integer riskScore = 0;

    @Column(name = "total_sessions")
    private Integer totalSessions = 0;

    @Column(name = "registration_date")
    @CreatedDate
    private LocalDateTime registrationDate;

    @Column(name = "last_session_date")
    private LocalDateTime lastSessionDate;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relationship")
    private String emergencyContactRelationship;

    @Column(name = "medical_history", length = 2000)
    private String medicalHistory;

    @Column(name = "counseling_history", length = 2000)
    private String counselingHistory;

    @Column(name = "referral_source")
    private String referralSource;

    @Column(name = "consent_to_treatment")
    private Boolean consentToTreatment = false;

    @Column(name = "notes", length = 5000)
    private String notes;

    // Default constructor
    public Client() {
        super();
    }

    // All args constructor
    public Client(Long id, String username, String email, String password, String firstName, String lastName,
                  String studentId, String phoneNumber, String profilePicture, String bio, Gender gender,
                  LocalDateTime dateOfBirth, String department, String program, Integer yearOfStudy,
                  Boolean active, Boolean emailVerified, LocalDateTime lastLogin, String resetPasswordToken,
                  LocalDateTime resetPasswordExpiry, Set<Role> roles, LocalDateTime createdAt, LocalDateTime updatedAt,
                  String licenseNumber, String specialization, String qualifications, Integer yearsOfExperience,
                  Boolean availableForAppointments, String programme, String faculty, Integer yearOfStudy1,
                  Double gpa, ClientStatus clientStatus, RiskLevel riskLevel, Integer riskScore,
                  Integer totalSessions, LocalDateTime registrationDate, LocalDateTime lastSessionDate,
                  String emergencyContactName, String emergencyContactPhone, String emergencyContactRelationship,
                  String medicalHistory, String counselingHistory, String referralSource,
                  Boolean consentToTreatment, String notes) {
        super(id, username, email, password, firstName, lastName, studentId, phoneNumber, profilePicture,
              bio, gender, dateOfBirth, department, program, yearOfStudy, active, emailVerified,
              lastLogin, resetPasswordToken, resetPasswordExpiry, roles, createdAt, updatedAt,
              licenseNumber, specialization, qualifications, yearsOfExperience, availableForAppointments);
        this.programme = programme;
        this.faculty = faculty;
        this.yearOfStudy = yearOfStudy1;
        this.gpa = gpa;
        this.clientStatus = clientStatus;
        this.riskLevel = riskLevel;
        this.riskScore = riskScore;
        this.totalSessions = totalSessions;
        this.registrationDate = registrationDate;
        this.lastSessionDate = lastSessionDate;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.emergencyContactRelationship = emergencyContactRelationship;
        this.medicalHistory = medicalHistory;
        this.counselingHistory = counselingHistory;
        this.referralSource = referralSource;
        this.consentToTreatment = consentToTreatment;
        this.notes = notes;
    }

    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }

    public ClientStatus getClientStatus() { return clientStatus; }
    public void setClientStatus(ClientStatus clientStatus) { this.clientStatus = clientStatus; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public Integer getTotalSessions() { return totalSessions; }
    public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public LocalDateTime getLastSessionDate() { return lastSessionDate; }
    public void setLastSessionDate(LocalDateTime lastSessionDate) { this.lastSessionDate = lastSessionDate; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getEmergencyContactRelationship() { return emergencyContactRelationship; }
    public void setEmergencyContactRelationship(String emergencyContactRelationship) { this.emergencyContactRelationship = emergencyContactRelationship; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public String getCounselingHistory() { return counselingHistory; }
    public void setCounselingHistory(String counselingHistory) { this.counselingHistory = counselingHistory; }

    public String getReferralSource() { return referralSource; }
    public void setReferralSource(String referralSource) { this.referralSource = referralSource; }

    public Boolean getConsentToTreatment() { return consentToTreatment; }
    public void setConsentToTreatment(Boolean consentToTreatment) { this.consentToTreatment = consentToTreatment; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /**
     * Client Status Enum - Represents the current status of a client
     */
    public enum ClientStatus {
        ACTIVE,          // Currently receiving counseling
        INACTIVE,        // Not currently active
        COMPLETED,       // Completed treatment
        REFERRED,        // Referred to external services
        ON_HOLD,         // Temporarily paused
        WITHDRAWN        // Withdrew from services
    }

    /**
     * Risk Level Enum - Represents the assessed risk level of a client
     */
    public enum RiskLevel {
        LOW,             // No significant risk factors
        MODERATE,        // Some risk factors present
        HIGH,            // Significant risk factors
        CRITICAL         // Immediate intervention needed
    }

    /**
     * Helper method to check if client is actively receiving services
     */
    public boolean isActive() {
        return clientStatus == ClientStatus.ACTIVE;
    }

    /**
     * Helper method to check if client is high risk
     */
    public boolean isHighRisk() {
        return riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL;
    }

    /**
     * Helper method to increment session count
     */
    public void incrementSessionCount() {
        this.totalSessions++;
        this.lastSessionDate = LocalDateTime.now();
    }
}