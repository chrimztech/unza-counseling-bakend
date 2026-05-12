package zm.unza.counseling.dto.response;

import zm.unza.counseling.entity.Scholarship;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ScholarshipResponse {

    private Long id;
    private String name;
    private String description;
    private String sponsor;
    private BigDecimal amount;
    private Scholarship.ScholarshipType type;
    private LocalDate deadline;
    private String academicYear;
    private Integer maxRecipients;
    private String eligibilityCriteria;
    private Double requiredMinGpa;
    private Scholarship.ScholarshipStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long recommendationCount;
    private Long awardedCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSponsor() { return sponsor; }
    public void setSponsor(String sponsor) { this.sponsor = sponsor; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Scholarship.ScholarshipType getType() { return type; }
    public void setType(Scholarship.ScholarshipType type) { this.type = type; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getMaxRecipients() { return maxRecipients; }
    public void setMaxRecipients(Integer maxRecipients) { this.maxRecipients = maxRecipients; }

    public String getEligibilityCriteria() { return eligibilityCriteria; }
    public void setEligibilityCriteria(String eligibilityCriteria) { this.eligibilityCriteria = eligibilityCriteria; }

    public Double getRequiredMinGpa() { return requiredMinGpa; }
    public void setRequiredMinGpa(Double requiredMinGpa) { this.requiredMinGpa = requiredMinGpa; }

    public Scholarship.ScholarshipStatus getStatus() { return status; }
    public void setStatus(Scholarship.ScholarshipStatus status) { this.status = status; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getRecommendationCount() { return recommendationCount; }
    public void setRecommendationCount(Long recommendationCount) { this.recommendationCount = recommendationCount; }

    public Long getAwardedCount() { return awardedCount; }
    public void setAwardedCount(Long awardedCount) { this.awardedCount = awardedCount; }
}
