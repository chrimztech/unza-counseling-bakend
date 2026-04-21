package zm.unza.counseling.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Family Member Entity - Represents a family member in the client's family history
 * Part of the Personal Data Form (Client Intake Form)
 */
@Entity
@Table(name = "family_members")
@EntityListeners(AuditingEntityListener.class)
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "relationship")
    private String relationship;

    @Column(name = "age")
    private String age;

    @Column(name = "education")
    private String education;

    @Column(name = "occupation")
    private String occupation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
    private PersonalDataForm personalDataForm;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor
    public FamilyMember() {}

    // Convenience constructor
    public FamilyMember(String name, String relationship, String age, String education, String occupation) {
        this.name = name;
        this.relationship = relationship;
        this.age = age;
        this.education = education;
        this.occupation = occupation;
    }

    // Getters and Setters
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

    public PersonalDataForm getPersonalDataForm() {
        return personalDataForm;
    }

    public void setPersonalDataForm(PersonalDataForm personalDataForm) {
        this.personalDataForm = personalDataForm;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
