package zm.unza.counseling.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.RoleRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.security.AuthenticationSource;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration to fix password encoding for existing admin user
 */
@Component
@RequiredArgsConstructor
public class PasswordFixConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        fixAdminUserPassword();
    }

    private void fixAdminUserPassword() {
        String adminEmail = "admin@unza.zm";
        String adminUsername = "admin";
        String adminPassword = "Admin@123";
        
        // Find the existing admin user
        User adminUser = userRepository.findByEmail(adminEmail).orElse(null);
        
        if (adminUser != null) {
            // Check if password needs to be fixed (not properly encoded)
            String currentPassword = adminUser.getPassword();
            boolean needsFix = !currentPassword.startsWith("$2a$") || currentPassword.length() != 60;
            
            if (needsFix) {
                // Test password encoding first
                String encodedPassword = passwordEncoder.encode(adminPassword);
                System.out.println("==========================================");
                System.out.println("Password Fix - Admin User:");
                System.out.println("Plain Password: " + adminPassword);
                System.out.println("Encoded Password: " + encodedPassword);
                System.out.println("Password starts with $2a$: " + encodedPassword.startsWith("$2a$"));
                System.out.println("Password length: " + encodedPassword.length());
                System.out.println("==========================================");

                // Update the password and ensure username is set
                adminUser.setPassword(encodedPassword);
                if (adminUser.getUsername() == null || adminUser.getUsername().trim().isEmpty()) {
                    adminUser.setUsername(adminUsername);
                }
                adminUser.setUpdatedAt(LocalDateTime.now());
                
                userRepository.save(adminUser);
                
                System.out.println("==========================================");
                System.out.println("Admin User Password Fixed Successfully!");
                System.out.println("Email: " + adminEmail);
                System.out.println("Username: " + adminUser.getUsername());
                System.out.println("Password: " + adminPassword);
                System.out.println("==========================================");
            } else {
                System.out.println("Admin user password is already properly encoded: " + adminEmail);
            }
        } else {
            // Create admin user if it doesn't exist
            createAdminUser();
        }
    }

    private void createAdminUser() {
        String adminEmail = "admin@unza.zm";
        String adminUsername = "admin";
        String adminPassword = "Admin@123";
        
        // Create admin role if it doesn't exist
        Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(Role.ERole.ROLE_ADMIN);
                    newRole.setDescription("System Administrator with full access");
                    return roleRepository.save(newRole);
                });

        // Create super admin role if it doesn't exist
        Role superAdminRole = roleRepository.findByName(Role.ERole.ROLE_SUPER_ADMIN)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(Role.ERole.ROLE_SUPER_ADMIN);
                    newRole.setDescription("Super Administrator with complete system control");
                    return roleRepository.save(newRole);
                });

        // Test password encoding first
        String encodedPassword = passwordEncoder.encode(adminPassword);
        System.out.println("==========================================");
        System.out.println("Password Encoding Test:");
        System.out.println("Plain Password: " + adminPassword);
        System.out.println("Encoded Password: " + encodedPassword);
        System.out.println("Password starts with $2a$: " + encodedPassword.startsWith("$2a$"));
        System.out.println("Password length: " + encodedPassword.length());
        System.out.println("==========================================");

        // Create the admin user
        User adminUser = new User();
        adminUser.setEmail(adminEmail);
        adminUser.setUsername(adminUsername);
        adminUser.setPassword(encodedPassword); // Use the encoded password
        adminUser.setFirstName("System");
        adminUser.setLastName("Administrator");
        adminUser.setActive(true);
        adminUser.setEmailVerified(true);
        adminUser.setAuthenticationSource(AuthenticationSource.INTERNAL);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        adminUser.setLastLogin(LocalDateTime.now());

        // Assign both admin and super admin roles
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(superAdminRole);
        adminUser.setRoles(roles);

        userRepository.save(adminUser);
        
        System.out.println("==========================================");
        System.out.println("Admin User Created Successfully!");
        System.out.println("Email: " + adminEmail);
        System.out.println("Username: " + adminUsername);
        System.out.println("Password: " + adminPassword);
        System.out.println("Roles: ROLE_ADMIN, ROLE_SUPER_ADMIN");
        System.out.println("==========================================");
    }
}