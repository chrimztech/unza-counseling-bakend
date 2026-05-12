package zm.unza.counseling.dto.response;

import zm.unza.counseling.entity.ScholarshipRecommendation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ScholarshipRecommendationResponse {

    private Long id;
    private Long scholarshipId;
    private String scholarshipName;
    private String scholarshipSponsor;

    // Student details
    private Long clientId;
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String faculty;
    private String programme;
    private Integer yearOfStudy;
    private Double gpa;
    private String riskLevel;
    private Integer riskScore;
    private Integer totalSessions;

    // Recommendation details
    private Long recommendedBy;
    private ScholarshipRecommendation.RecommendationStatus status;
    private String justification;
    private ScholarshipRecommendation.FinancialNeedLevel financialNeedLevel;
    private Integer vulnerabilityScore;
    private String academicStanding;
    private String personalStatement;
    private String supportingNotes;

    // Decision details
    private Long approvedBy;
    private String rejectionReason;
    private LocalDate awardedDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getScholarshipId() { return scholarshipId; }
    public void setScholarshipId(Long scholarshipId) { this.scholarshipId = scholarshipId; }

    public String getScholarshipName() { return scholarshipName; }
    public void setScholarshipName(String scholarshipName) { this.scholarshipName = scholarshipName; }

    public String getScholarshipSponsor() { return scholarshipSponsor; }
    public void setScholarshipSponsor(String scholarshipSponsor) { this.scholarshipSponsor = scholarshipSponsor; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getStudentPhone() { return studentPhone; }
    public void setStudentPhone(String studentPhone) { this.studentPhone = studentPhone; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }

    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public Integer getTotalSessions() { return totalSessions; }
    public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }

    public Long getRecommendedBy() { return recommendedBy; }
    public void setRecommendedBy(Long recommendedBy) { this.recommendedBy = recommendedBy; }

    public ScholarshipRecommendation.RecommendationStatus getStatus() { return status; }
    public void setStatus(ScholarshipRecommendation.RecommendationStatus status) { this.status = status; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public ScholarshipRecommendation.FinancialNeedLevel getFinancialNeedLevel() { return financialNeedLevel; }
    public void setFinancialNeedLevel(ScholarshipRecommendation.FinancialNeedLevel financialNeedLevel) { this.financialNeedLevel = financialNeedLevel; }

    public Integer getVulnerabilityScore() { return vulnerabilityScore; }
    public void setVulnerabilityScore(Integer vulnerabilityScore) { this.vulnerabilityScore = vulnerabilityScore; }

    public String getAcademicStanding() { return academicStanding; }
    public void setAcademicStanding(String academicStanding) { this.academicStanding = academicStanding; }

    public String getPersonalStatement() { return personalStatement; }
    public void setPersonalStatement(String personalStatement) { this.personalStatement = personalStatement; }

    public String getSupportingNotes() { return supportingNotes; }
    public void setSupportingNotes(String supportingNotes) { this.supportingNotes = supportingNotes; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDate getAwardedDate() { return awardedDate; }
    public void setAwardedDate(LocalDate awardedDate) { this.awardedDate = awardedDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
