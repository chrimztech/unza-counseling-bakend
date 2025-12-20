package zm.unza.counseling.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity - Represents all system users (counselors, admins, students)
 */
@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email"),
           @UniqueConstraint(columnNames = "studentId")
       })
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Size(max = 100)
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(min = 6, max = 120)
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String lastName;

    @Column(unique = true)
    private String studentId; // For students only

    @Size(max = 20)
    private String phoneNumber;

    private String profilePicture;

    @Column(length = 500)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private LocalDateTime dateOfBirth;

    private String department; // Academic department

    private String program; // Degree program

    private Integer yearOfStudy;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    private LocalDateTime lastLogin;

    private String resetPasswordToken;

    private LocalDateTime resetPasswordExpiry;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Counselor-specific fields
    private String licenseNumber;
    
    private String specialization;
    
    @Column(length = 1000)
    private String qualifications;
    
    private Integer yearsOfExperience;
    
    private Boolean availableForAppointments = true;

    // Default constructor
    public User() {}

    // All args constructor
    public User(Long id, String username, String email, String password, String firstName, String lastName,
                String studentId, String phoneNumber, String profilePicture, String bio, Gender gender,
                LocalDateTime dateOfBirth, String department, String program, Integer yearOfStudy,
                Boolean active, Boolean emailVerified, LocalDateTime lastLogin, String resetPasswordToken,
                LocalDateTime resetPasswordExpiry, Set<Role> roles, LocalDateTime createdAt, LocalDateTime updatedAt,
                String licenseNumber, String specialization, String qualifications, Integer yearsOfExperience,
                Boolean availableForAppointments) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.department = department;
        this.program = program;
        this.yearOfStudy = yearOfStudy;
        this.active = active;
        this.emailVerified = emailVerified;
        this.lastLogin = lastLogin;
        this.resetPasswordToken = resetPasswordToken;
        this.resetPasswordExpiry = resetPasswordExpiry;
        this.roles = roles != null ? roles : new HashSet<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.licenseNumber = licenseNumber;
        this.specialization = specialization;
        this.qualifications = qualifications;
        this.yearsOfExperience = yearsOfExperience;
        this.availableForAppointments = availableForAppointments;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public String getResetPasswordToken() { return resetPasswordToken; }
    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }

    public LocalDateTime getResetPasswordExpiry() { return resetPasswordExpiry; }
    public void setResetPasswordExpiry(LocalDateTime resetPasswordExpiry) { this.resetPasswordExpiry = resetPasswordExpiry; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles != null ? roles : new HashSet<>(); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getQualifications() { return qualifications; }
    public void setQualifications(String qualifications) { this.qualifications = qualifications; }

    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public Boolean getAvailableForAppointments() { return availableForAppointments; }
    public void setAvailableForAppointments(Boolean availableForAppointments) { this.availableForAppointments = availableForAppointments; }

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().name().equals(roleName));
    }

    public boolean isCounselor() {
        return hasRole("ROLE_COUNSELOR") || hasRole("ROLE_ADMIN");
    }

    public boolean isStudent() {
        return hasRole("ROLE_STUDENT");
    }

    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User user = new User();

        public Builder id(Long id) {
            user.setId(id);
            return this;
        }

        public Builder username(String username) {
            user.setUsername(username);
            return this;
        }

        public Builder email(String email) {
            user.setEmail(email);
            return this;
        }

        public Builder password(String password) {
            user.setPassword(password);
            return this;
        }

        public Builder firstName(String firstName) {
            user.setFirstName(firstName);
            return this;
        }

        public Builder lastName(String lastName) {
            user.setLastName(lastName);
            return this;
        }

        public Builder studentId(String studentId) {
            user.setStudentId(studentId);
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            user.setPhoneNumber(phoneNumber);
            return this;
        }

        public Builder profilePicture(String profilePicture) {
            user.setProfilePicture(profilePicture);
            return this;
        }

        public Builder bio(String bio) {
            user.setBio(bio);
            return this;
        }

        public Builder gender(Gender gender) {
            user.setGender(gender);
            return this;
        }

        public Builder dateOfBirth(LocalDateTime dateOfBirth) {
            user.setDateOfBirth(dateOfBirth);
            return this;
        }

        public Builder department(String department) {
            user.setDepartment(department);
            return this;
        }

        public Builder program(String program) {
            user.setProgram(program);
            return this;
        }

        public Builder yearOfStudy(Integer yearOfStudy) {
            user.setYearOfStudy(yearOfStudy);
            return this;
        }

        public Builder active(Boolean active) {
            user.setActive(active);
            return this;
        }

        public Builder emailVerified(Boolean emailVerified) {
            user.setEmailVerified(emailVerified);
            return this;
        }

        public Builder lastLogin(LocalDateTime lastLogin) {
            user.setLastLogin(lastLogin);
            return this;
        }

        public Builder roles(Set<Role> roles) {
            user.setRoles(roles);
            return this;
        }

        public Builder licenseNumber(String licenseNumber) {
            user.setLicenseNumber(licenseNumber);
            return this;
        }

        public Builder specialization(String specialization) {
            user.setSpecialization(specialization);
            return this;
        }

        public Builder qualifications(String qualifications) {
            user.setQualifications(qualifications);
            return this;
        }

        public Builder yearsOfExperience(Integer yearsOfExperience) {
            user.setYearsOfExperience(yearsOfExperience);
            return this;
        }

        public Builder availableForAppointments(Boolean availableForAppointments) {
            user.setAvailableForAppointments(availableForAppointments);
            return this;
        }

        public User build() {
            return user;
        }
    }
}