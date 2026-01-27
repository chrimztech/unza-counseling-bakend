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
 * Fix configuration to create an admin user with proper password encoding
 */
//@Component
@RequiredArgsConstructor
public class FixPasswordConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createFixedAdminUser();
    }

    private void createFixedAdminUser() {
        String adminEmail = "fixed@unza.zm";
        String adminUsername = "fixed";
        String adminPassword = "Fixed@123";
        
        if (!userRepository.existsByEmail(adminEmail)) {
            // Test password encoding first
            String encodedPassword = passwordEncoder.encode(adminPassword);
            System.out.println("==========================================");
            System.out.println("Password Encoding Test:");
            System.out.println("Plain Password: " + adminPassword);
            System.out.println("Encoded Password: " + encodedPassword);
            System.out.println("Password starts with $2a$: " + encodedPassword.startsWith("$2a$"));
            System.out.println("Password length: " + encodedPassword.length());
            System.out.println("==========================================");

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

            // Create the admin user
            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(encodedPassword); // Use the encoded password
            adminUser.setFirstName("Fixed");
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
            System.out.println("Fixed Admin User Created Successfully!");
            System.out.println("Email: " + adminEmail);
            System.out.println("Username: " + adminUsername);
            System.out.println("Password: " + adminPassword);
            System.out.println("Roles: ROLE_ADMIN, ROLE_SUPER_ADMIN");
            System.out.println("==========================================");
        } else {
            System.out.println("Fixed admin user already exists: " + adminEmail);
        }
    }
}