package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.CreateAdminRequest;
import zm.unza.counseling.entity.Admin;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.AdminRepository;
import zm.unza.counseling.repository.RoleRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.security.AuthenticationSource;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AdminIdentityService adminIdentityService;

    public List<Admin> getAllAdmins() {
        return adminIdentityService.getAllAdmins();
    }

    @Transactional
    public Admin createAdmin(CreateAdminRequest request) {
        String email = request.getEmail().trim();
        validateNewAdmin(email);
        String adminLevel = normalizeAdminLevel(request.getAdminLevel());

        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setUsername(email);
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setFirstName(request.getFirstName().trim());
        admin.setLastName(request.getLastName().trim());
        admin.setPhoneNumber(trimToNull(request.getPhoneNumber()));
        admin.setAdminLevel(adminLevel);
        admin.setDepartmentManaged(trimToNull(request.getDepartmentManaged()));
        admin.setGender(User.Gender.OTHER); // Required field
        admin.setActive(true);
        admin.setEmailVerified(true);
        admin.setAuthenticationSource(AuthenticationSource.INTERNAL);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        // Set admin role
        Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(Role.ERole.ROLE_ADMIN);
                    newRole.setDescription("Administrator with system management capabilities");
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        if ("SUPER_ADMIN".equals(adminLevel)) {
            roles.add(getOrCreateRole(
                    Role.ERole.ROLE_SUPER_ADMIN,
                    "Super administrator with complete system control"
            ));
        }
        admin.setRoles(roles);

        return adminRepository.save(admin);
    }

    private void validateNewAdmin(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("A user with this email already exists");
        }
        if (userRepository.existsByUsername(email)) {
            throw new ValidationException("A user with this username already exists");
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

    private String normalizeAdminLevel(String adminLevel) {
        if (adminLevel == null || adminLevel.isBlank()) {
            return "STANDARD_ADMIN";
        }

        return switch (adminLevel.trim().toUpperCase()) {
            case "STANDARD", "STANDARD_ADMIN", "ADMIN", "DEPARTMENT_ADMIN" -> "STANDARD_ADMIN";
            case "SENIOR", "SENIOR_ADMIN" -> "SENIOR_ADMIN";
            case "SUPER", "SUPER_ADMIN", "SYSTEM_ADMIN" -> "SUPER_ADMIN";
            default -> throw new ValidationException("Unsupported admin level: " + adminLevel);
        };
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Transactional
    public void deleteAdmin(Long id) {
        Admin admin = adminIdentityService.getOrCreateAdmin(id);
        adminRepository.delete(admin);
    }
}
