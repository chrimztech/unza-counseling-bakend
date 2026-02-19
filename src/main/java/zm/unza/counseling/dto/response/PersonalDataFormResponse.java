package zm.unza.counseling.dto.response;

import zm.unza.counseling.entity.FamilyMember;
import zm.unza.counseling.entity.PersonalDataForm;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for Personal Data Form (Client Intake Form)
 * Matches the frontend form structure exactly
 */
public class PersonalDataFormResponse {

    private Long id;
    private String clientFileNo;
    private Long clientId;
    private String clientName;
    private Long caseId;
    private String caseNumber;
    
    // Basic Information
    private String computerNo;
    private String occupation;
    private String contactAddress;
    private PersonalDataForm.MaritalStatus maritalStatus;
    
    // Previous Counselling
    private List<PersonalDataForm.PreviousCounselling> previousCounselling;
    private String previousCounsellingOther;
    
    // Referral Source
    private List<PersonalDataForm.ReferralSource> referralSource;
    private String referralSourceOther;
    
    // Reasons for Counselling (nested object)
    private ReasonsForCounsellingResponse reasonsForCounselling;
    
    // Family History
    private List<FamilyMemberResponse> familyHistory;
    
    // Health Information
    private PersonalDataForm.HealthStatus healthStatus;
    private String healthCondition;
    private PersonalDataForm.MedicationStatus takingMedication;
    private String medicationDetails;
    
    // Additional Information
    private String additionalInfo;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Nested response for Reasons for Counselling
     */
    public static class ReasonsForCounsellingResponse {
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
     * Nested response for Family Member
     */
    public static class FamilyMemberResponse {
        private Long id;
        private String name;
        private String relationship;
        private String age;
        private String education;
        private String occupation;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

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

    // Default constructor
    public PersonalDataFormResponse() {}

    // From entity
    public static PersonalDataFormResponse fromEntity(PersonalDataForm form) {
        PersonalDataFormResponse response = new PersonalDataFormResponse();
        response.setId(form.getId());
        response.setClientFileNo(form.getClientFileNo());
        
        if (form.getClient() != null) {
            response.setClientId(form.getClient().getId());
            response.setClientName(form.getClient().getFullName());
        }
        
        if (form.getCaseEntity() != null) {
            response.setCaseId(form.getCaseEntity().getId());
            response.setCaseNumber(form.getCaseEntity().getCaseNumber());
        }
        
        // Basic Information
        response.setComputerNo(form.getComputerNo());
        response.setOccupation(form.getOccupation());
        response.setContactAddress(form.getContactAddress());
        response.setMaritalStatus(form.getMaritalStatus());
        
        // Previous Counselling
        response.setPreviousCounselling(form.getPreviousCounselling());
        response.setPreviousCounsellingOther(form.getPreviousCounsellingOther());
        
        // Referral Source
        response.setReferralSource(form.getReferralSource());
        response.setReferralSourceOther(form.getReferralSourceOther());
        
        // Reasons for Counselling
        if (form.getReasonsForCounselling() != null) {
            ReasonsForCounsellingResponse reasons = new ReasonsForCounsellingResponse();
            PersonalDataForm.ReasonsForCounselling formReasons = form.getReasonsForCounselling();
            
            reasons.setPersonal(formReasons.getPersonal());
            reasons.setPersonalOther(formReasons.getPersonalOther());
            reasons.setHealth(formReasons.getHealth());
            reasons.setHealthOther(formReasons.getHealthOther());
            reasons.setEducational(formReasons.getEducational());
            reasons.setEducationalOther(formReasons.getEducationalOther());
            reasons.setCareer(formReasons.getCareer());
            reasons.setCareerOther(formReasons.getCareerOther());
            reasons.setFinancial(formReasons.getFinancial());
            reasons.setFinancialOther(formReasons.getFinancialOther());
            
            response.setReasonsForCounselling(reasons);
        }
        
        // Family History
        if (form.getFamilyHistory() != null) {
            List<FamilyMemberResponse> familyHistory = form.getFamilyHistory().stream()
                    .map(fm -> {
                        FamilyMemberResponse fmResp = new FamilyMemberResponse();
                        fmResp.setId(fm.getId());
                        fmResp.setName(fm.getName());
                        fmResp.setRelationship(fm.getRelationship());
                        fmResp.setAge(fm.getAge());
                        fmResp.setEducation(fm.getEducation());
                        fmResp.setOccupation(fm.getOccupation());
                        return fmResp;
                    })
                    .collect(Collectors.toList());
            response.setFamilyHistory(familyHistory);
        }
        
        // Health Information
        response.setHealthStatus(form.getHealthStatus());
        response.setHealthCondition(form.getHealthCondition());
        response.setTakingMedication(form.getTakingMedication());
        response.setMedicationDetails(form.getMedicationDetails());
        
        // Additional Information
        response.setAdditionalInfo(form.getAdditionalInfo());
        
        response.setCreatedAt(form.getCreatedAt());
        response.setUpdatedAt(form.getUpdatedAt());
        
        return response;
    }

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

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
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

    public ReasonsForCounsellingResponse getReasonsForCounselling() {
        return reasonsForCounselling;
    }

    public void setReasonsForCounselling(ReasonsForCounsellingResponse reasonsForCounselling) {
        this.reasonsForCounselling = reasonsForCounselling;
    }

    public List<FamilyMemberResponse> getFamilyHistory() {
        return familyHistory;
    }

    public void setFamilyHistory(List<FamilyMemberResponse> familyHistory) {
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
