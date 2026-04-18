package zm.unza.counseling.entity;

import jakarta.persistence.Column;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Counselor-completed client intake form that mirrors the paper intake sheet.
 */
@Entity
@Table(name = "client_intake_forms")
@EntityListeners(AuditingEntityListener.class)
public class ClientIntakeForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case caseEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id")
    private Counselor counselor;

    @Column(name = "client_file_no")
    private String clientFileNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex")
    private User.Gender sex;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status")
    private PersonalDataForm.MaritalStatus maritalStatus;

    @Column(name = "computer_no")
    private String computerNo;

    @Column(name = "year_of_study")
    private Integer yearOfStudy;

    @Column(name = "school")
    private String school;

    @Column(name = "hall_room_no")
    private String hallRoomNo;

    @Column(name = "contact_phone_no")
    private String contactPhoneNo;

    @Column(name = "presenting_concern", columnDefinition = "TEXT")
    private String presentingConcern;

    @Column(name = "problem_conceptualization", columnDefinition = "TEXT")
    private String problemConceptualization;

    @Column(name = "tentative_goals_directions", columnDefinition = "TEXT")
    private String tentativeGoalsDirections;

    @Column(name = "coping_strategies", columnDefinition = "TEXT")
    private String copingStrategies;

    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    @Column(name = "time_taken_counselling")
    private String timeTakenCounselling;

    @Column(name = "next_contact_appointment_date")
    private LocalDate nextContactAppointmentDate;

    @Column(name = "number_of_contact_sessions")
    private Integer numberOfContactSessions;

    @Column(name = "counsellor_name")
    private String counsellorName;

    @Column(name = "form_date")
    private LocalDate formDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Counselor getCounselor() {
        return counselor;
    }

    public void setCounselor(Counselor counselor) {
        this.counselor = counselor;
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
