package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zm.unza.counseling.entity.Scholarship;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ScholarshipRequest {

    @NotBlank(message = "Scholarship name is required")
    private String name;

    private String description;

    @NotBlank(message = "Sponsor is required")
    private String sponsor;

    private BigDecimal amount;

    @NotNull(message = "Scholarship type is required")
    private Scholarship.ScholarshipType type;

    private LocalDate deadline;

    private String academicYear;

    private Integer maxRecipients;

    private String eligibilityCriteria;

    private Double requiredMinGpa;

    private Scholarship.ScholarshipStatus status;

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
}
