package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zm.unza.counseling.entity.PersonalDataForm;

import java.util.List;

/**
 * Request DTO for creating or updating a Personal Data Form (Client Intake Form)
 * Matches the frontend form structure exactly
 */
public class PersonalDataFormRequest {

    // Basic Information
    @NotBlank(message = "Client file number is required")
    private String clientFileNo;

    @NotBlank(message = "Computer number is required")
    private String computerNo;

    @NotBlank(message = "Occupation is required")
    private String occupation;

    @NotBlank(message = "Contact address is required")
    private String contactAddress;

    @NotNull(message = "Marital status is required")
    private PersonalDataForm.MaritalStatus maritalStatus;

    // Previous Counselling
    @NotNull(message = "Previous counselling information is required")
    private List<PersonalDataForm.PreviousCounselling> previousCounselling;

    private String previousCounsellingOther;

    // Referral Source
    @NotNull(message = "Referral source is required")
    private List<PersonalDataForm.ReferralSource> referralSource;

    private String referralSourceOther;

    // Reasons for Counselling (nested object)
    private ReasonsForCounsellingRequest reasonsForCounselling;

    // Family History
    private List<FamilyMemberRequest> familyHistory;

    // Health Information
    @NotNull(message = "Health status is required")
    private PersonalDataForm.HealthStatus healthStatus;

    private String healthCondition;

    @NotNull(message = "Medication status is required")
    private PersonalDataForm.MedicationStatus takingMedication;

    private String medicationDetails;

    // Additional Information
    private String additionalInfo;

    // Client ID for linking
    private Long clientId;

    // Case ID for linking (optional - to link form to a case)
    private Long caseId;

    /**
     * Nested DTO for Reasons for Counselling
     */
    public static class ReasonsForCounsellingRequest {
        private List<PersonalDataForm.PersonalReason> personal;
        private String personalOther;
        private List<PersonalDataForm.HealthReason> health;
        private String healthOther;
        private List<PersonalDataForm.EducationalReason> educational;
        private String educationalOther;
        private List<PersonalDataForm.CareerReason> career;
        private String careerOther;
        private List<PersonalDataForm.FinancialReason> financial;
        private String financialOther;

        // Getters and Setters
        public List<PersonalDataForm.PersonalReason> getPersonal() {
            return personal;
        }

        public void setPersonal(List<PersonalDataForm.PersonalReason> personal) {
            this.personal = personal;
        }

        public String getPersonalOther() {
            return personalOther;
        }

        public void setPersonalOther(String personalOther) {
            this.personalOther = personalOther;
        }

        public List<PersonalDataForm.HealthReason> getHealth() {
            return health;
        }

        public void setHealth(List<PersonalDataForm.HealthReason> health) {
            this.health = health;
        }

        public String getHealthOther() {
            return healthOther;
        }

        public void setHealthOther(String healthOther) {
            this.healthOther = healthOther;
        }

        public List<PersonalDataForm.EducationalReason> getEducational() {
            return educational;
        }

        public void setEducational(List<PersonalDataForm.EducationalReason> educational) {
            this.educational = educational;
        }

        public String getEducationalOther() {
            return educationalOther;
        }

        public void setEducationalOther(String educationalOther) {
            this.educationalOther = educationalOther;
        }

        public List<PersonalDataForm.CareerReason> getCareer() {
            return career;
        }

        public void setCareer(List<PersonalDataForm.CareerReason> career) {
            this.career = career;
        }

        public String getCareerOther() {
            return careerOther;
        }

        public void setCareerOther(String careerOther) {
            this.careerOther = careerOther;
        }

        public List<PersonalDataForm.FinancialReason> getFinancial() {
            return financial;
        }

        public void setFinancial(List<PersonalDataForm.FinancialReason> financial) {
            this.financial = financial;
        }

        public String getFinancialOther() {
            return financialOther;
        }

        public void setFinancialOther(String financialOther) {
            this.financialOther = financialOther;
        }
    }

    /**
     * Nested DTO for Family Member
     */
    public static class FamilyMemberRequest {
        private String name;
        private String relationship;
        private String age;
        private String education;
        private String occupation;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

        public String getEducation() {
            return education;
        }

        public void setEducation(String education) {
            this.education = education;
        }

        public String getOccupation() {
            return occupation;
        }

        public void setOccupation(String occupation) {
            this.occupation = occupation;
        }
    }

    // Getters and Setters
    public String getClientFileNo() {
        return clientFileNo;
    }

    public void setClientFileNo(String clientFileNo) {
        this.clientFileNo = clientFileNo;
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

    public PersonalDataForm.MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(PersonalDataForm.MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public List<PersonalDataForm.PreviousCounselling> getPreviousCounselling() {
        return previousCounselling;
    }

    public void setPreviousCounselling(List<PersonalDataForm.PreviousCounselling> previousCounselling) {
        this.previousCounselling = previousCounselling;
    }

    public String getPreviousCounsellingOther() {
        return previousCounsellingOther;
    }

    public void setPreviousCounsellingOther(String previousCounsellingOther) {
        this.previousCounsellingOther = previousCounsellingOther;
    }

    public List<PersonalDataForm.ReferralSource> getReferralSource() {
        return referralSource;
    }

    public void setReferralSource(List<PersonalDataForm.ReferralSource> referralSource) {
        this.referralSource = referralSource;
    }

    public String getReferralSourceOther() {
        return referralSourceOther;
    }

    public void setReferralSourceOther(String referralSourceOther) {
        this.referralSourceOther = referralSourceOther;
    }

    public ReasonsForCounsellingRequest getReasonsForCounselling() {
        return reasonsForCounselling;
    }

    public void setReasonsForCounselling(ReasonsForCounsellingRequest reasonsForCounselling) {
        this.reasonsForCounselling = reasonsForCounselling;
    }

    public List<FamilyMemberRequest> getFamilyHistory() {
        return familyHistory;
    }

    public void setFamilyHistory(List<FamilyMemberRequest> familyHistory) {
        this.familyHistory = familyHistory;
    }

    public PersonalDataForm.HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(PersonalDataForm.HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getHealthCondition() {
        return healthCondition;
    }

    public void setHealthCondition(String healthCondition) {
        this.healthCondition = healthCondition;
    }

    public PersonalDataForm.MedicationStatus getTakingMedication() {
        return takingMedication;
    }

    public void setTakingMedication(PersonalDataForm.MedicationStatus takingMedication) {
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

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }
}
