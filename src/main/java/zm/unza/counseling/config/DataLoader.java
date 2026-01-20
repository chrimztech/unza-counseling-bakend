package zm.unza.counseling.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.*;
import zm.unza.counseling.repository.*;
import zm.unza.counseling.security.AuthenticationSource;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataLoader {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;

    public DataLoader(UserRepository userRepository, RoleRepository roleRepository, 
                     PasswordEncoder passwordEncoder, AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.appointmentRepository = appointmentRepository;
    }

    @PostConstruct
    @Transactional
    public void loadData() {
        System.out.println("Starting data loading process...");
        
        // Check if data already exists
        if (userRepository.count() > 0) {
            System.out.println("Data already exists, skipping data loading");
            return;
        }

        try {
            // Create roles first
            Role superAdminRole = createRole("ROLE_SUPER_ADMIN", "Super administrator with full system access");
            Role adminRole = createRole("ROLE_ADMIN", "Administrator with system management capabilities");
            Role counselorRole = createRole("ROLE_COUNSELOR", "Licensed counselor providing counseling services");
            Role studentRole = createRole("ROLE_STUDENT", "Student seeking counseling services");
            Role clientRole = createRole("ROLE_CLIENT", "General client role for counseling services");

            // Save roles first
            roleRepository.saveAll(Set.of(superAdminRole, adminRole, counselorRole, studentRole, clientRole));

            // Create users
            User superAdmin = createUser("superadmin", "superadmin@unza.zm", "System", "Administrator", "+260971234567", 
                User.Gender.MALE, LocalDateTime.of(1980, 1, 1, 0, 0), "IT", "Computer Science", 5, 
                true, true, Set.of(superAdminRole));
                
            User admin = createUser("admin", "admin@unza.zm", "John", "Mwanza", "+260972345678", 
                User.Gender.MALE, LocalDateTime.of(1985, 3, 15, 0, 0), "Psychology", "Counseling Psychology", 3, 
                true, true, Set.of(adminRole));

            User counselor1 = createUser("counselor1", "grace.chiluba@unza.zm", "Grace", "Chiluba", "+260973456789", 
                User.Gender.FEMALE, LocalDateTime.of(1982, 7, 22, 0, 0), "Psychology", "Clinical Psychology", 5, 
                true, true, Set.of(counselorRole));
            counselor1.setLicenseNumber("LIC2023002");
            counselor1.setSpecialization("Relationship Counseling, Family Therapy");
            counselor1.setQualifications("M.A. Clinical Psychology, University of Lusaka");
            counselor1.setYearsOfExperience(5);
            counselor1.setAvailableForAppointments(true);
            counselor1.setHasSignedConsent(true);

            User counselor2 = createUser("counselor2", "michael.simukoko@unza.zm", "Michael", "Simukoko", "+260974567890", 
                User.Gender.MALE, LocalDateTime.of(1978, 11, 5, 0, 0), "Psychology", "Counseling Psychology", 12, 
                true, true, Set.of(counselorRole));
            counselor2.setLicenseNumber("LIC2023003");
            counselor2.setSpecialization("Addiction Counseling, Trauma Therapy");
            counselor2.setQualifications("Ph.D. Counseling Psychology, University of Cape Town");
            counselor2.setYearsOfExperience(12);
            counselor2.setAvailableForAppointments(true);
            counselor2.setHasSignedConsent(true);

            User counselor3 = createUser("counselor3", "sarah.banda@unza.zm", "Sarah", "Banda", "+260975678901", 
                User.Gender.FEMALE, LocalDateTime.of(1988, 4, 18, 0, 0), "Psychology", "Educational Psychology", 3, 
                true, true, Set.of(counselorRole));
            counselor3.setLicenseNumber("LIC2023004");
            counselor3.setSpecialization("Academic Stress, Anxiety Disorders");
            counselor3.setQualifications("M.Sc. Psychology, University of Zambia");
            counselor3.setYearsOfExperience(3);
            counselor3.setAvailableForAppointments(true);
            counselor3.setHasSignedConsent(true);

            User student1 = createUser("student1", "student1@unza.zm", "David", "Phiri", "+260976789012", 
                User.Gender.MALE, LocalDateTime.of(2000, 9, 12, 0, 0), "Computer Science", "Bachelor of Science", 2, 
                true, true, Set.of(studentRole));

            User student2 = createUser("student2", "student2@unza.zm", "Emily", "Kabwe", "+260977890123", 
                User.Gender.FEMALE, LocalDateTime.of(2001, 2, 25, 0, 0), "Psychology", "Bachelor of Arts", 3, 
                true, true, Set.of(studentRole));

            User student3 = createUser("student3", "student3@unza.zm", "James", "Mwamba", "+260978901234", 
                User.Gender.MALE, LocalDateTime.of(1999, 11, 30, 0, 0), "Engineering", "Bachelor of Engineering", 4, 
                true, true, Set.of(studentRole));

            User student4 = createUser("student4", "student4@unza.zm", "Sophia", "Lungu", "+260979012345", 
                User.Gender.FEMALE, LocalDateTime.of(2002, 5, 14, 0, 0), "Medicine", "Bachelor of Medicine", 1, 
                true, true, Set.of(studentRole));

            User student5 = createUser("student5", "student5@unza.zm", "Peter", "Chanda", "+260970123456", 
                User.Gender.MALE, LocalDateTime.of(2000, 8, 19, 0, 0), "Business", "Bachelor of Business Administration", 3, 
                true, true, Set.of(studentRole));

            // Save all users
            userRepository.saveAll(Set.of(superAdmin, admin, counselor1, counselor2, counselor3, 
                student1, student2, student3, student4, student5));

            // Create appointments
            Appointment appointment1 = new Appointment();
            appointment1.setTitle("Initial Consultation - Academic Stress");
            appointment1.setStudent(student1);
            appointment1.setCounselor(counselor1);
            appointment1.setAppointmentDate(LocalDateTime.now().minusDays(5).withHour(10).withMinute(0));
            appointment1.setDuration(60);
            appointment1.setType(Appointment.AppointmentType.INITIAL_CONSULTATION);
            appointment1.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointment1.setDescription("Student experiencing academic stress and anxiety");
            appointment1.setLocation("Counseling Office 1");
            appointment1.setReminderSent(false);

            Appointment appointment2 = new Appointment();
            appointment2.setTitle("Follow-up Session - Anxiety Management");
            appointment2.setStudent(student1);
            appointment2.setCounselor(counselor1);
            appointment2.setAppointmentDate(LocalDateTime.now().minusDays(3).withHour(14).withMinute(0));
            appointment2.setDuration(45);
            appointment2.setType(Appointment.AppointmentType.FOLLOW_UP);
            appointment2.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointment2.setDescription("Follow-up on anxiety management techniques");
            appointment2.setLocation("Counseling Office 1");
            appointment2.setReminderSent(false);

            Appointment appointment3 = new Appointment();
            appointment3.setTitle("Initial Consultation - Relationship Issues");
            appointment3.setStudent(student2);
            appointment3.setCounselor(counselor2);
            appointment3.setAppointmentDate(LocalDateTime.now().minusDays(4).withHour(9).withMinute(0));
            appointment3.setDuration(60);
            appointment3.setType(Appointment.AppointmentType.INITIAL_CONSULTATION);
            appointment3.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointment3.setDescription("Student dealing with relationship challenges");
            appointment3.setLocation("Counseling Office 2");
            appointment3.setReminderSent(false);

            Appointment appointment4 = new Appointment();
            appointment4.setTitle("Crisis Intervention - Exam Stress");
            appointment4.setStudent(student3);
            appointment4.setCounselor(counselor3);
            appointment4.setAppointmentDate(LocalDateTime.now().minusDays(2).withHour(11).withMinute(0));
            appointment4.setDuration(90);
            appointment4.setType(Appointment.AppointmentType.CRISIS_INTERVENTION);
            appointment4.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointment4.setDescription("Urgent session for exam-related stress and panic attacks");
            appointment4.setLocation("Counseling Office 3");
            appointment4.setReminderSent(false);

            Appointment appointment5 = new Appointment();
            appointment5.setTitle("Follow-up - Relationship Counseling");
            appointment5.setStudent(student2);
            appointment5.setCounselor(counselor2);
            appointment5.setAppointmentDate(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0));
            appointment5.setDuration(45);
            appointment5.setType(Appointment.AppointmentType.FOLLOW_UP);
            appointment5.setStatus(Appointment.AppointmentStatus.SCHEDULED);
            appointment5.setDescription("Follow-up session on relationship progress");
            appointment5.setLocation("Counseling Office 2");
            appointment5.setReminderSent(false);

            appointmentRepository.saveAll(Set.of(appointment1, appointment2, appointment3, appointment4, appointment5));

            System.out.println("Data loading completed successfully!");
            System.out.println("Created " + userRepository.count() + " users, " + roleRepository.count() + " roles, " + appointmentRepository.count() + " appointments");

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

    private User createUser(String username, String email, String firstName, String lastName, 
                          String phoneNumber, User.Gender gender, LocalDateTime dateOfBirth, 
                          String department, String program, int yearOfStudy, 
                          boolean active, boolean emailVerified, Set<Role> roles) {
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        user.setGender(gender);
        user.setDateOfBirth(dateOfBirth);
        user.setDepartment(department);
        user.setProgram(program);
        user.setYearOfStudy(yearOfStudy);
        user.setActive(active);
        user.setEmailVerified(emailVerified);
        user.setLastLogin(LocalDateTime.now().minusDays(1));
        user.setRoles(roles);
        user.setAuthenticationSource(AuthenticationSource.INTERNAL);
        user.setHasSignedConsent(false);
        return user;
    }
}