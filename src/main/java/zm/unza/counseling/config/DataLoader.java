package zm.unza.counseling.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.*;
import zm.unza.counseling.repository.*;
import zm.unza.counseling.security.AuthenticationSource;

import java.time.LocalDateTime;
import java.util.Set;

// Seeds only the two accounts needed to bootstrap a fresh system: a super
// admin (to configure everything and assign roles) and a security officer
// (so the security dashboard has someone to log in as). Everything else —
// counselors, students, appointments, etc. — is real operational data and
// should be created through the app itself, not seeded.
@Configuration
public class DataLoader {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConsentFormRepository consentFormRepository;

    public DataLoader(UserRepository userRepository, RoleRepository roleRepository,
                     PasswordEncoder passwordEncoder, ConsentFormRepository consentFormRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.consentFormRepository = consentFormRepository;
    }

    @PostConstruct
    @Transactional
    public void loadData() {
        System.out.println("Starting data loading process...");

        if (userRepository.count() > 0) {
            System.out.println("Data already exists, skipping data loading");
            return;
        }

        try {
            // All roles are created up front — they're structural (referenced by
            // role-assignment, route guards, etc.) even though only two are used
            // by a seeded account here.
            Role superAdminRole = createRole("ROLE_SUPER_ADMIN", "Super administrator with full system access");
            Role adminRole = createRole("ROLE_ADMIN", "Administrator with system management capabilities");
            Role counselorRole = createRole("ROLE_COUNSELOR", "Licensed counselor providing counseling services");
            Role studentRole = createRole("ROLE_STUDENT", "Student seeking counseling services");
            Role clientRole = createRole("ROLE_CLIENT", "General client role for counseling services");
            Role securityRole = createRole("ROLE_SECURITY", "University security staff reviewing sensitive-case alerts");

            roleRepository.saveAll(Set.of(superAdminRole, adminRole, counselorRole, studentRole, clientRole, securityRole));

            User superAdmin = createUser("superadmin@unza.zm", "superadmin@unza.zm", "Admin@123", "System", "Administrator",
                "+260971234567", User.Gender.MALE, LocalDateTime.of(1980, 1, 1, 0, 0), "IT", 5,
                Set.of(superAdminRole));

            User securityStaff = createUser("security@unza.zm", "security@unza.zm", "Security@123", "UNZA", "Security Officer",
                "+260976789012", User.Gender.MALE, LocalDateTime.of(1985, 6, 1, 0, 0), "Campus Security", 0,
                Set.of(securityRole));
            securityStaff.setHasSignedConsent(true);

            userRepository.saveAll(Set.of(superAdmin, securityStaff));

            // Kept: the client consent flow (ConsentGuard) requires an active
            // consent form to exist — this is system configuration, not test data.
            createDefaultConsentForm();

            System.out.println("Data loading completed successfully!");
            System.out.println("Created " + userRepository.count() + " users, " + roleRepository.count() + " roles");

        } catch (Exception e) {
            System.err.println("Error during data loading: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Role createRole(String name, String description) {
        Role role = new Role();
        role.setName(Role.ERole.valueOf(name));
        role.setDescription(description);
        return role;
    }

    private User createUser(String username, String email, String rawPassword, String firstName, String lastName,
                          String phoneNumber, User.Gender gender, LocalDateTime dateOfBirth,
                          String department, int yearOfStudy, Set<Role> roles) {

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        user.setGender(gender);
        user.setDateOfBirth(dateOfBirth);
        user.setDepartment(department);
        user.setYearOfStudy(yearOfStudy);
        user.setActive(true);
        user.setEmailVerified(true);
        user.setLastLogin(LocalDateTime.now().minusDays(1));
        user.setRoles(roles);
        user.setAuthenticationSource(AuthenticationSource.INTERNAL);
        user.setHasSignedConsent(false);
        return user;
    }

    private void createDefaultConsentForm() {
        ConsentForm consentForm = new ConsentForm();
        consentForm.setTitle("Counseling Services Consent Form");
        consentForm.setContent("""
            <h2>UNZA Counseling Services - Consent Form</h2>

            <h3>1. Introduction</h3>
            <p>Welcome to the University of Zambia (UNZA) Counseling Services. This consent form outlines the terms and conditions for receiving counseling services.</p>

            <h3>2. Confidentiality</h3>
            <p>All information shared during counseling sessions is strictly confidential. Exceptions include situations where there is risk of harm to yourself or others, or as required by law.</p>

            <h3>3. Services Provided</h3>
            <p>We offer individual counseling, group therapy, crisis intervention, and academic support for students experiencing personal, emotional, or psychological challenges.</p>

            <h3>4. Your Rights</h3>
            <p>You have the right to:
            <ul>
                <li>Be treated with respect and dignity</li>
                <li>Receive services in a safe and comfortable environment</li>
                <li>Request to stop counseling at any time</li>
                <li>Access your counseling records as permitted by law</li>
            </ul>
            </p>

            <h3>5. Responsibilities</h3>
            <p>As a client, you agree to:
            <ul>
                <li>Attend scheduled appointments on time</li>
                <li>Communicate openly with your counselor</li>
                <li>Notify us of any concerns or changes in your situation</li>
            </ul>
            </p>

            <h3>6. Agreement</h3>
            <p>By signing this form, you acknowledge that you have read, understood, and agree to the terms outlined above.</p>
            """);
        consentForm.setVersion("1.0");
        consentForm.setActive(true);
        consentForm.setEffectiveDate(LocalDateTime.now());
        consentFormRepository.save(consentForm);
        System.out.println("Default consent form created successfully");
    }
}
