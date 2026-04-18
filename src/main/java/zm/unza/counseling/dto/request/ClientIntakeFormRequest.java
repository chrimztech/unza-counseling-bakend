package zm.unza.counseling.dto.request;

import jakarta.validation.constraints.NotBlank;
import zm.unza.counseling.entity.PersonalDataForm;
import zm.unza.counseling.entity.User;

import java.time.LocalDate;

/**
 * Request DTO for the counselor-completed client intake form.
 */
public class ClientIntakeFormRequest {

    private Long caseId;
    private Long counselorId;
    private String clientFileNo;
    private User.Gender sex;
    private Integer age;
    private PersonalDataForm.MaritalStatus maritalStatus;
    private String computerNo;
    private Integer yearOfStudy;
    @NotBlank(message = "School is required")
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

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public Long getCounselorId() {
        return counselorId;
    }

    public void setCounselorId(Long counselorId) {
        this.counselorId = counselorId;
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
}
