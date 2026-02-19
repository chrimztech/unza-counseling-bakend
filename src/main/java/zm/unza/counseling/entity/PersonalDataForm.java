package zm.unza.counseling.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Personal Data Form Entity - Represents the personal data form filled out by clients
 * This is the client intake form that should be linked to cases
 */
@Entity
@Table(name = "personal_data_forms")
@EntityListeners(AuditingEntityListener.class)
public class PersonalDataForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_file_no", unique = true)
    private String clientFileNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Link to Case - one form can be associated with a case
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case caseEntity;

    @Column(name = "computer_no")
    private String computerNo;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "contact_address", length = 500)
    private String contactAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status")
    private MaritalStatus maritalStatus;

    // Previous Counselling - stored as enum array
    @ElementCollection
    @CollectionTable(name = "form_previous_counselling", joinColumns = @JoinColumn(name = "form_id"))
    @Column(name = "counselling_type")
    @Enumerated(EnumType.STRING)
    private List<PreviousCounselling> previousCounselling = new ArrayList<>();

    @Column(name = "previous_counselling_other")
    private String previousCounsellingOther;

    // Referral Source - stored as enum array
    @ElementCollection
    @CollectionTable(name = "form_referral_sources", joinColumns = @JoinColumn(name = "form_id"))
    @Column(name = "referral_source")
    @Enumerated(EnumType.STRING)
    private List<ReferralSource> referralSource = new ArrayList<>();

    @Column(name = "referral_source_other")
    private String referralSourceOther;

    // Reasons for Counselling - nested object stored as separate collections
    @Embedded
    private ReasonsForCounselling reasonsForCounselling;

    // Family History - stored as JSON or separate entity
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "form_id")
    private List<FamilyMember> familyHistory = new ArrayList<>();

    // Health Information
    @Enumerated(EnumType.STRING)
    @Column(name = "health_status")
    private HealthStatus healthStatus;

    @Column(name = "health_condition", length = 1000)
    private String healthCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "taking_medication")
    private MedicationStatus takingMedication;

    @Column(name = "medication_details", length = 1000)
    private String medicationDetails;

    @Column(name = "additional_info", length = 2000)
    private String additionalInfo;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Enums matching frontend exactly
    public enum MaritalStatus {
        SINGLE, MARRIED, DIVORCED, SEPARATED, WIDOWED, LIVING_WITH_PARTNER
    }

    public enum PreviousCounselling {
        UNIVERSITY, SUBJECT_COUNSELLOR, OTHER
    }

    public enum ReferralSource {
        SELF, SUBJECT_COUNSELLOR, FRIEND, PARTNER, FAMILY, HEALTH_WORKER, OTHER
    }

    public enum HealthStatus {
        YES, NO
    }

    public enum MedicationStatus {
        YES, NO
    }

    // Personal reasons enum
    public enum PersonalReason {
        TRANSITIONS, RELATIONSHIPS, WORRY, FAMILY_ISSUES, ALCOHOL_DRUGS_ABUSE, 
        GUILTY_FEELINGS, BEREAVEMENT, COMMUNICATION_CONFLICT, OTHER
    }

    // Health reasons enum
    public enum HealthReason {
        HIV_STI, FAMILY_PLANNING, PREGNANT, UNWELL, OTHER
    }

    // Educational reasons enum
    public enum EducationalReason {
        CHANGE_COURSE, WITHDRAWAL, EXAM_ANXIETY, MISSED_ASSIGNMENTS, EXCLUDE, 
        LEAVE_ABSENCE, ACADEMIC_RESEARCH, LACK_CONCENTRATION, OTHER
    }

    // Career reasons enum
    public enum CareerReason {
        CAREER_PLANNING, EMPLOYMENT_SEARCH, INTERVIEW_TECHNIQUES, CV_WRITING, 
        LACK_GOALS, INTERNSHIP, OTHER
    }

    // Financial reasons enum
    public enum FinancialReason {
        GRZ_BURSARY, BANKING, CAMPUS_WORK, HARDSHIP_LOAN, OTHER
    }

    /**
     * Embedded class for Reasons for Counselling (nested object from frontend)
     */
    @Embeddable
    public static class ReasonsForCounselling {
        @ElementCollection
        @CollectionTable(name = "reasons_personal", joinColumns = @JoinColumn(name = "form_id"))
        @Column(name = "reason")
        @Enumerated(EnumType.STRING)
        private List<PersonalReason> personal = new ArrayList<>();

        @Column(name = "personal_other")
        private String personalOther;

        @ElementCollection
        @CollectionTable(name = "reasons_health", joinColumns = @JoinColumn(name = "form_id"))
        @Column(name = "reason")
        @Enumerated(EnumType.STRING)
        private List<HealthReason> health = new ArrayList<>();

        @Column(name = "health_other")
        private String healthOther;

        @ElementCollection
        @CollectionTable(name = "reasons_educational", joinColumns = @JoinColumn(name = "form_id"))
        @Column(name = "reason")
        @Enumerated(EnumType.STRING)
        private List<EducationalReason> educational = new ArrayList<>();

        @Column(name = "educational_other")
        private String educationalOther;

        @ElementCollection
        @CollectionTable(name = "reasons_career", joinColumns = @JoinColumn(name = "form_id"))
        @Column(name = "reason")
        @Enumerated(EnumType.STRING)
        private List<CareerReason> career = new ArrayList<>();

        @Column(name = "career_other")
        private String careerOther;

        @ElementCollection
        @CollectionTable(name = "reasons_financial", joinColumns = @JoinColumn(name = "form_id"))
        @Column(name = "reason")
        @Enumerated(EnumType.STRING)
        private List<FinancialReason> financial = new ArrayList<>();

        @Column(name = "financial_other")
        private String financialOther;

        // Getters and Setters
        public List<PersonalReason> getPersonal() {
            return personal;
        }

        public void setPersonal(List<PersonalReason> personal) {
            this.personal = personal;
        }

        public String getPersonalOther() {
            return personalOther;
        }

        public void setPersonalOther(String personalOther) {
            this.personalOther = personalOther;
        }

        public List<HealthReason> getHealth() {
            return health;
        }

        public void setHealth(List<HealthReason> health) {
            this.health = health;
        }

        public String getHealthOther() {
            return healthOther;
        }

        public void setHealthOther(String healthOther) {
            this.healthOther = healthOther;
        }

        public List<EducationalReason> getEducational() {
            return educational;
        }

        public void setEducational(List<EducationalReason> educational) {
            this.educational = educational;
        }

        public String getEducationalOther() {
            return educationalOther;
        }

        public void setEducationalOther(String educationalOther) {
            this.educationalOther = educationalOther;
        }

        public List<CareerReason> getCareer() {
            return career;
        }

        public void setCareer(List<CareerReason> career) {
            this.career = career;
        }

        public String getCareerOther() {
            return careerOther;
        }

        public void setCareerOther(String careerOther) {
            this.careerOther = careerOther;
        }

        public List<FinancialReason> getFinancial() {
            return financial;
        }

        public void setFinancial(List<FinancialReason> financial) {
            this.financial = financial;
        }

        public String getFinancialOther() {
            return financialOther;
        }

        public void setFinancialOther(String financialOther) {
            this.financialOther = financialOther;
        }
    }

    // Default constructor
    public PersonalDataForm() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientFileNo() {
        return clientFileNo;
    }

    public void setClientFileNo(String clientFileNo) {
        this.clientFileNo = clientFileNo;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Case getCaseEntity() {
        return caseEntity;
    }

    public void setCaseEntity(Case caseEntity) {
        this.caseEntity = caseEntity;
    }

    public String getComputerNo() {
        return computerNo;
    }

    public void setComputerNo(String computerNo) {
        this.computerNo = computerNo;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public List<PreviousCounselling> getPreviousCounselling() {
        return previousCounselling;
    }

    public void setPreviousCounselling(List<PreviousCounselling> previousCounselling) {
        this.previousCounselling = previousCounselling;
    }

    public String getPreviousCounsellingOther() {
        return previousCounsellingOther;
    }

    public void setPreviousCounsellingOther(String previousCounsellingOther) {
        this.previousCounsellingOther = previousCounsellingOther;
    }

    public List<ReferralSource> getReferralSource() {
        return referralSource;
    }

    public void setReferralSource(List<ReferralSource> referralSource) {
        this.referralSource = referralSource;
    }

    public String getReferralSourceOther() {
        return referralSourceOther;
    }

    public void setReferralSourceOther(String referralSourceOther) {
        this.referralSourceOther = referralSourceOther;
    }

    public ReasonsForCounselling getReasonsForCounselling() {
        return reasonsForCounselling;
    }

    public void setReasonsForCounselling(ReasonsForCounselling reasonsForCounselling) {
        this.reasonsForCounselling = reasonsForCounselling;
    }

    public List<FamilyMember> getFamilyHistory() {
        return familyHistory;
    }

    public void setFamilyHistory(List<FamilyMember> familyHistory) {
        this.familyHistory = familyHistory;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getHealthCondition() {
        return healthCondition;
    }

    public void setHealthCondition(String healthCondition) {
        this.healthCondition = healthCondition;
    }

    public MedicationStatus getTakingMedication() {
        return takingMedication;
    }

    public void setTakingMedication(MedicationStatus takingMedication) {
        this.takingMedication = takingMedication;
    }

    public String getMedicationDetails() {
        return medicationDetails;
    }

    public void setMedicationDetails(String medicationDetails) {
        this.medicationDetails = medicationDetails;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
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
}
