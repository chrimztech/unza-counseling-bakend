package zm.unza.counseling.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.Admin;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.AdminRepository;
import zm.unza.counseling.repository.UserRepository;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Bridges admin-role user records into the Admin subtype used by management screens.
 */
@Service
public class AdminIdentityService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AdminIdentityService(AdminRepository adminRepository, UserRepository userRepository) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<Admin> getAllAdmins() {
        syncRoleBackedAdmins();
        return adminRepository.findAll();
    }

    @Transactional
    public Admin getOrCreateAdmin(Long userId) {
        return adminRepository.findById(userId)
                .orElseGet(() -> promoteUserToAdmin(userId));
    }

    @Transactional
    public void syncRoleBackedAdmins() {
        LinkedHashMap<Long, User> distinctUsers = new LinkedHashMap<>();
        userRepository.findByRolesName(Role.ERole.ROLE_ADMIN)
                .forEach(user -> distinctUsers.put(user.getId(), user));
        userRepository.findByRolesName(Role.ERole.ROLE_SUPER_ADMIN)
                .forEach(user -> distinctUsers.put(user.getId(), user));

        distinctUsers.keySet().forEach(this::getOrCreateAdmin);
    }

    private Admin promoteUserToAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + userId));

        if (!isAdminUser(user)) {
            throw new ValidationException("Selected user is not configured as an administrator");
        }

        String adminLevel = user.hasRole(Role.ERole.ROLE_SUPER_ADMIN.name())
                ? "SUPER_ADMIN"
                : "STANDARD_ADMIN";

        entityManager.createNativeQuery("""
                UPDATE users
                SET user_type = 'ADMIN',
                    admin_level = COALESCE(admin_level, :adminLevel),
                    department_managed = COALESCE(department_managed, department)
                WHERE id = :userId
                """)
                .setParameter("adminLevel", adminLevel)
                .setParameter("userId", userId)
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        return adminRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + userId));
    }

    private boolean isAdminUser(User user) {
        return user.hasRole(Role.ERole.ROLE_ADMIN.name())
                || user.hasRole(Role.ERole.ROLE_SUPER_ADMIN.name());
    }
}
