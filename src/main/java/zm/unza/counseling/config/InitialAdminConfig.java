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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration to create an initial admin user for system access
 */
@Component
@RequiredArgsConstructor
public class InitialAdminConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createOrUpdateInitialAdminUser();
    }

    private void createOrUpdateInitialAdminUser() {
        String adminEmail = "admin@unza.zm";
        String adminUsername = "admin@unza.zm"; // Use email as username for consistency
        String adminPassword = "Admin@123";

        Role adminRole = getOrCreateRole(Role.ERole.ROLE_ADMIN, "System Administrator with full access");
        Role superAdminRole = getOrCreateRole(Role.ERole.ROLE_SUPER_ADMIN, "Super Administrator with complete system control");

        Optional<User> existingAdminOpt = userRepository.findByEmail(adminEmail);
        
        // Fallback: try to find by old username "admin" if email lookup failed
        if (existingAdminOpt.isEmpty()) {
            existingAdminOpt = userRepository.findByUsername("admin");
        }

        if (existingAdminOpt.isPresent()) {
            User adminUser = existingAdminOpt.get();
            boolean needsUpdate = false;

            // Ensure username matches email (Critical for login)
            if (!adminUsername.equals(adminUser.getUsername())) {
                adminUser.setUsername(adminUsername);
                needsUpdate = true;
                System.out.println("Correcting admin username to: " + adminUsername);
            }

            // Ensure email matches adminEmail (Critical for lookup)
            if (!adminEmail.equals(adminUser.getEmail())) {
                adminUser.setEmail(adminEmail);
                needsUpdate = true;
                System.out.println("Correcting admin email to: " + adminEmail);
            }

            // Ensure password is correct (Force reset to Admin@123)
            if (!passwordEncoder.matches(adminPassword, adminUser.getPassword())) {
                adminUser.setPassword(passwordEncoder.encode(adminPassword));
                needsUpdate = true;
                System.out.println("Resetting admin password to default.");
            }

            // Ensure admin is active
            if (!Boolean.TRUE.equals(adminUser.getActive())) {
                adminUser.setActive(true);
                needsUpdate = true;
                System.out.println("Activating admin user.");
            }

            Set<Role.ERole> currentRoles = adminUser.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            if (!currentRoles.contains(Role.ERole.ROLE_ADMIN)) {
                adminUser.getRoles().add(adminRole);
                needsUpdate = true;
                System.out.println("Adding ROLE_ADMIN to existing admin user.");
            }
            if (!currentRoles.contains(Role.ERole.ROLE_SUPER_ADMIN)) {
                adminUser.getRoles().add(superAdminRole);
                needsUpdate = true;
                System.out.println("Adding ROLE_SUPER_ADMIN to existing admin user.");
            }

            if (needsUpdate) {
                userRepository.save(adminUser);
                System.out.println("Admin user " + adminEmail + " updated with required roles.");
            } else {
                System.out.println("Admin user " + adminEmail + " already has all required roles.");
            }

        } else {
            // Create the admin user if they do not exist
            String encodedPassword = passwordEncoder.encode(adminPassword);
            
            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(encodedPassword);

            adminUser.setFirstName("System");
            adminUser.setLastName("Administrator");
            adminUser.setActive(true);
            adminUser.setEmailVerified(true);
            adminUser.setAuthenticationSource(AuthenticationSource.INTERNAL);
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setUpdatedAt(LocalDateTime.now());
            adminUser.setLastLogin(LocalDateTime.now());

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(superAdminRole);
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            
            System.out.println("==========================================");
            System.out.println("Initial Admin User Created Successfully!");
            System.out.println("Email: " + adminEmail);
            System.out.println("Username: " + adminUsername);
            System.out.println("Roles: ROLE_ADMIN, ROLE_SUPER_ADMIN");
            System.out.println("==========================================");
        }
    }

    private Role getOrCreateRole(Role.ERole roleName, String description) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    newRole.setDescription(description);
                    return roleRepository.save(newRole);
                });
    }
}