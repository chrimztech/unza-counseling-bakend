package zm.unza.counseling.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Personal Data Form Entity - Mirrors the UNZA paper personal data form used during intake.
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case caseEntity;

    @Column(name = "date_of_interview")
    private LocalDate dateOfInterview;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex")
    private User.Gender sex;

    @Column(name = "year_of_birth")
    private Integer yearOfBirth;

    @Column(name = "age")
    private Integer age;

    @Column(name = "school")
    private String school;

    @Column(name = "computer_no")
    private String computerNo;

    @Column(name = "year_of_study")
    private Integer yearOfStudy;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "contact_address", length = 500)
    private String contactAddress;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status")
    private MaritalStatus maritalStatus;

    @ElementCollection
    @CollectionTable(name = "form_previous_counselling", joinColumns = @JoinColumn(name = "form_id"))
    @Column(name = "counselling_type")
    @Enumerated(EnumType.STRING)
    private List<PreviousCounselling> previousCounselling = new ArrayList<>();

    @Column(name = "previous_counselling_other")
    private String previousCounsellingOther;

    @ElementCollection
    @CollectionTable(name = "form_referral_sources", joinColumns = @JoinColumn(name = "form_id"))
    @Column(name = "referral_source")
    @Enumerated(EnumType.STRING)
    private List<ReferralSource> referralSource = new ArrayList<>();

    @Column(name = "referral_source_other")
    private String referralSourceOther;

    @Embedded
    private ReasonsForCounselling reasonsForCounselling;

    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "form_id")
    private List<FamilyMember> familyHistory = new ArrayList<>();

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

    public enum MaritalStatus {
        SINGLE,
        MARRIED,
        DIVORCED,
        SEPARATED,
        WIDOWED,
        LIVING_WITH_PARTNER
    }

    public enum PreviousCounselling {
        UNIVERSITY,
        SUBJECT_COUNSELLOR,
        SUBJECT_COUNSELLOR_TUTOR_DEAN,
        OTHER,
        NONE
    }

    public enum ReferralSource {
        SELF,
        SELF_REFERRAL,
        SUBJECT_COUNSELLOR,
        SUBJECT_COUNSELLOR_TUTOR_DEAN,
        FRIEND,
        PARTNER,
        FAMILY,
        HEALTH_WORKER,
        HEALTH_WORKER_CLINIC,
        OTHER
    }

    public enum HealthStatus {
        YES,
        NO
    }

    public enum MedicationStatus {
        YES,
        NO
    }

    public enum PersonalReason {
        TRANSITIONS,
        RELATIONSHIPS,
        RELATIONSHIP_COUNSELLING,
        WORRY,
        WORRY_ABOUT_SELF_PARTNER,
        FAMILY_ISSUES,
        ALCOHOL_DRUGS_ABUSE,
        ALCOHOL_DRUGS_SEXUAL_ABUSE,
        GUILTY_FEELINGS,
        BEREAVEMENT,
        BEREAVEMENT_DEATH,
        COMMUNICATION_CONFLICT,
        INTERPERSONAL_COMMUNICATION_CONFLICT,
        OTHER
    }

    public enum HealthReason {
        HIV_STI,
        HIV_AIDS_STIS_COUNSELLING,
        FAMILY_PLANNING,
        PREGNANT,
        UNWELL,
        OTHER
    }

    public enum EducationalReason {
        CHANGE_COURSE,
        WITHDRAWAL,
        ACADEMIC_WITHDRAWAL_FROM_STUDIES,
        EXAM_ANXIETY,
        EXAM_PANIC_ANXIETY_STUDY_TECHNIQUES,
        MISSED_ASSIGNMENTS,
        MISSED_ASSIGNMENTS_LESSONS_LABS,
        EXCLUDE,
        LEAVE_ABSENCE,
        ACADEMIC_RESEARCH,
        INFORMATION_ON_ACADEMIC_RESEARCH,
        LACK_CONCENTRATION,
        LACK_CONCENTRATION_ON_STUDIES,
        OTHER
    }

    public enum CareerReason {
        CAREER_PLANNING,
        CAREER_CHOICE_PLANNING,
        EMPLOYMENT_SEARCH,
        INTERVIEW_TECHNIQUES,
        CV_WRITING,
        CV_PREPARATION_APPLICATION_LETTER_WRITING,
        LACK_GOALS,
        LACK_OF_OCCUPATIONAL_GOALS,
        INTERNSHIP,
        VACATION_EMPLOYMENT_INTERNSHIP,
        OTHER
    }

    public enum FinancialReason {
        GRZ_BURSARY,
        GRZ_BURSARIES_SCHOLARSHIP_LOAN,
        BANKING,
        CAMPUS_WORK,
        HARDSHIP_LOAN,
        OTHER
    }

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

    public PersonalDataForm() {
    }

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

    public LocalDate getDateOfInterview() {
        return dateOfInterview;
    }

    public void setDateOfInterview(LocalDate dateOfInterview) {
        this.dateOfInterview = dateOfInterview;
    }

    public User.Gender getSex() {
        return sex;
    }

    public void setSex(User.Gender sex) {
        this.sex = sex;
    }

    public Integer getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(Integer yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getComputerNo() {
        return computerNo;
    }

    public void setComputerNo(String computerNo) {
        this.computerNo = computerNo;
    }

    public Integer getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(Integer yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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
