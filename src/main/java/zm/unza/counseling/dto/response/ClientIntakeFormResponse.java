package zm.unza.counseling.dto.response;

import zm.unza.counseling.entity.ClientIntakeForm;
import zm.unza.counseling.entity.PersonalDataForm;
import zm.unza.counseling.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for the counselor-completed client intake form.
 */
public class ClientIntakeFormResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private Long caseId;
    private String caseNumber;
    private Long counselorId;
    private String counselorName;
    private String clientFileNo;
    private User.Gender sex;
    private Integer age;
    private PersonalDataForm.MaritalStatus maritalStatus;
    private String computerNo;
    private Integer yearOfStudy;
    private String school;
    private String hallRoomNo;
    private String contactPhoneNo;
    private String presentingConcern;
    private String problemConceptualization;
    private String tentativeGoalsDirections;
    private String copingStrategies;
    private String actionTaken;
    private String timeTakenCounselling;
    private LocalDate nextContactAppointmentDate;
    private Integer numberOfContactSessions;
    private String counsellorName;
    private LocalDate formDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ClientIntakeFormResponse fromEntity(ClientIntakeForm form) {
        ClientIntakeFormResponse response = new ClientIntakeFormResponse();
        String fallbackCounsellorName = form.getCounsellorName();
        response.setId(form.getId());
        response.setClientFileNo(form.getClientFileNo());
        response.setSex(form.getSex());
        response.setAge(form.getAge());
        response.setMaritalStatus(form.getMaritalStatus());
        response.setComputerNo(form.getComputerNo());
        response.setYearOfStudy(form.getYearOfStudy());
        response.setSchool(form.getSchool());
        response.setHallRoomNo(form.getHallRoomNo());
        response.setContactPhoneNo(form.getContactPhoneNo());
        response.setPresentingConcern(form.getPresentingConcern());
        response.setProblemConceptualization(form.getProblemConceptualization());
        response.setTentativeGoalsDirections(form.getTentativeGoalsDirections());
        response.setCopingStrategies(form.getCopingStrategies());
        response.setActionTaken(form.getActionTaken());
        response.setTimeTakenCounselling(form.getTimeTakenCounselling());
        response.setNextContactAppointmentDate(form.getNextContactAppointmentDate());
        response.setNumberOfContactSessions(form.getNumberOfContactSessions());
        response.setCounsellorName(fallbackCounsellorName);
        response.setFormDate(form.getFormDate());
        response.setCreatedAt(form.getCreatedAt());
        response.setUpdatedAt(form.getUpdatedAt());

        if (form.getClient() != null) {
            response.setClientId(form.getClient().getId());
            response.setClientName(form.getClient().getFullName());
        }

        if (form.getCaseEntity() != null) {
            response.setCaseId(form.getCaseEntity().getId());
            response.setCaseNumber(form.getCaseEntity().getCaseNumber());
        }

        if (form.getCounselor() != null) {
            response.setCounselorId(form.getCounselor().getId());
            response.setCounselorName(form.getCounselor().getFullName());
            if (fallbackCounsellorName == null || fallbackCounsellorName.isBlank()) {
                response.setCounsellorName(form.getCounselor().getFullName());
            }
        } else {
            response.setCounselorName(form.getCounsellorName());
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getCounselorId() {
        return counselorId;
    }

    public void setCounselorId(Long counselorId) {
        this.counselorId = counselorId;
    }

    public String getCounselorName() {
        return counselorName;
    }

    public void setCounselorName(String counselorName) {
        this.counselorName = counselorName;
    }

    public String getClientFileNo() {
        return clientFileNo;
    }

    public void setClientFileNo(String clientFileNo) {
        this.clientFileNo = clientFileNo;
    }

    public User.Gender getSex() {
        return sex;
    }

    public void setSex(User.Gender sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public PersonalDataForm.MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(PersonalDataForm.MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
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

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getHallRoomNo() {
        return hallRoomNo;
    }

    public void setHallRoomNo(String hallRoomNo) {
        this.hallRoomNo = hallRoomNo;
    }

    public String getContactPhoneNo() {
        return contactPhoneNo;
    }

    public void setContactPhoneNo(String contactPhoneNo) {
        this.contactPhoneNo = contactPhoneNo;
    }

    public String getPresentingConcern() {
        return presentingConcern;
    }

    public void setPresentingConcern(String presentingConcern) {
        this.presentingConcern = presentingConcern;
    }

    public String getProblemConceptualization() {
        return problemConceptualization;
    }

    public void setProblemConceptualization(String problemConceptualization) {
        this.problemConceptualization = problemConceptualization;
    }

    public String getTentativeGoalsDirections() {
        return tentativeGoalsDirections;
    }

    public void setTentativeGoalsDirections(String tentativeGoalsDirections) {
        this.tentativeGoalsDirections = tentativeGoalsDirections;
    }

    public String getCopingStrategies() {
        return copingStrategies;
    }

    public void setCopingStrategies(String copingStrategies) {
        this.copingStrategies = copingStrategies;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getTimeTakenCounselling() {
        return timeTakenCounselling;
    }

    public void setTimeTakenCounselling(String timeTakenCounselling) {
        this.timeTakenCounselling = timeTakenCounselling;
    }

    public LocalDate getNextContactAppointmentDate() {
        return nextContactAppointmentDate;
    }

    public void setNextContactAppointmentDate(LocalDate nextContactAppointmentDate) {
        this.nextContactAppointmentDate = nextContactAppointmentDate;
    }

    public Integer getNumberOfContactSessions() {
        return numberOfContactSessions;
    }

    public void setNumberOfContactSessions(Integer numberOfContactSessions) {
        this.numberOfContactSessions = numberOfContactSessions;
    }

    public String getCounsellorName() {
        return counsellorName;
    }

    public void setCounsellorName(String counsellorName) {
        this.counsellorName = counsellorName;
    }

    public LocalDate getFormDate() {
        return formDate;
    }

    public void setFormDate(LocalDate formDate) {
        this.formDate = formDate;
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
