package zm.unza.counseling.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.RegisterRequest;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.ChatMessageRepository;
import zm.unza.counseling.repository.KeyboardShortcutRepository;
import zm.unza.counseling.repository.MessageRepository;
import zm.unza.counseling.repository.NotificationRepository;
import zm.unza.counseling.repository.RoleRepository;
import zm.unza.counseling.security.AuthenticationSource;
import zm.unza.counseling.repository.UserBookmarkRepository;
import zm.unza.counseling.repository.UserConsentRepository;
import zm.unza.counseling.repository.UserDashboardConfigRepository;
import zm.unza.counseling.repository.UserFeedbackRepository;
import zm.unza.counseling.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationRepository notificationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserDashboardConfigRepository userDashboardConfigRepository;
    private final KeyboardShortcutRepository keyboardShortcutRepository;
    private final UserBookmarkRepository userBookmarkRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final UserConsentRepository userConsentRepository;
    private final MessageRepository messageRepository;

    @PersistenceContext
    private EntityManager em;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User getUserByUsernameOrEmail(String identifier) {
        // Try to find by email first, then by username
        return userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + identifier)));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User createUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("A user with this email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setStudentId(request.getStudentId());
        user.setActive(true);
        user.setEmailVerified(true);
        user.setAuthenticationSource(AuthenticationSource.INTERNAL);

        java.util.Set<Role> roles = new java.util.HashSet<>();
        Role.ERole roleEnum;
        try {
            roleEnum = request.getRole() != null
                    ? Role.ERole.valueOf(request.getRole().startsWith("ROLE_") ? request.getRole() : "ROLE_" + request.getRole())
                    : Role.ERole.ROLE_STUDENT;
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid role specified: " + request.getRole());
        }
        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new ValidationException("Role not found: " + roleEnum));
        roles.add(role);

        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Transactional
    public User assignRole(Long id, String roleName) {
        User user = getUserById(id);

        Role.ERole roleEnum;
        try {
            roleEnum = Role.ERole.valueOf(roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid role specified: " + roleName);
        }
        Role newRole = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new ValidationException("Role not found: " + roleEnum));

        // Changing away from ADMIN/SUPER_ADMIN must not leave the system with no
        // active administrator, same guard used for deactivation/deletion.
        if (!roleEnum.name().equals("ROLE_ADMIN") && !roleEnum.name().equals("ROLE_SUPER_ADMIN")) {
            guardAgainstRemovingLastAdministrator(user);
        }

        user.setRoles(new java.util.HashSet<>(java.util.Set.of(newRole)));
        return userRepository.save(user);
    }

    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        if (user.getActive() != null && !user.getActive()) {
            guardAgainstSelfRemoval(existingUser);
            guardAgainstRemovingLastAdministrator(existingUser);
        }
        if (user.getActive() != null) {
            existingUser.setActive(user.getActive());
        }
        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        deleteUser(id, true);
    }

    @Transactional
    public void deleteUser(Long id, boolean permanent) {
        User user = getUserById(id);
        if (permanent) {
            permanentlyDeleteUser(user);
            return;
        }
        deactivateUserInternal(user);
    }

    @Transactional
    public void permanentlyDeleteUser(Long id) {
        permanentlyDeleteUser(getUserById(id));
    }

    public List<User> getUsersByRole(String role) {
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }
        return userRepository.findByRolesName(Role.ERole.valueOf(role));
    }

    public Long getUserCount() {
        return userRepository.count();
    }

    public Page<User> searchUsers(String query, Pageable pageable) {
        return userRepository.findByEmailContainingOrFirstNameContainingOrLastNameContaining(
                query, query, query, pageable);
    }

    public Page<User> getActiveUsers(Pageable pageable) {
        return userRepository.findByActiveTrue(pageable);
    }

    public Page<User> getInactiveUsers(Pageable pageable) {
        return userRepository.findByActiveFalse(pageable);
    }

    public User activateUser(Long id) {
        User user = getUserById(id);
        user.setActive(true);
        return userRepository.save(user);
    }

    public User deactivateUser(Long id) {
        User user = getUserById(id);
        return deactivateUserInternal(user);
    }

    public List<String> getAllRoles() {
        return userRepository.findAllRoles();
    }

    public Map<String, Long> getUserCountByRole() {
        return userRepository.countUsersByRole().stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    public User getCurrentUserProfile() {
        User currentUser = resolveCurrentUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("Authenticated user profile could not be resolved");
        }
        return currentUser;
    }

    @Transactional
    public void changeUserPassword(Long id, String newPassword) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Self-service password change for the currently authenticated user — distinct from
    // changeUserPassword() above, which is admin-only and doesn't verify the caller knows
    // the existing password. This one does, since anyone can call it on their own account.
    @Transactional
    public void changeOwnPassword(String currentPassword, String newPassword) {
        User currentUser = getCurrentUserProfile();
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);
    }

    public byte[] exportUsers(String format) {
        // TODO: Implement export logic for CSV/Excel
        return new byte[0];
    }

    private User deactivateUserInternal(User user) {
        guardAgainstSelfRemoval(user);
        guardAgainstRemovingLastAdministrator(user);
        user.setActive(false);
        return userRepository.save(user);
    }

    private void permanentlyDeleteUser(User user) {
        guardAgainstSelfRemoval(user);
        guardAgainstRemovingLastAdministrator(user);
        cascadeDeleteClinicalRecords(user.getId());
        deleteUserOwnedArtifacts(user);
        userRepository.delete(user);
    }

    // Permanently and irreversibly removes every clinical/audit record this user is
    // linked to (as student, client, or counselor) before the user row itself is
    // deleted, in FK-dependency order (children before parents) so no constraint is
    // violated. There is no soft option here — deactivateUserInternal() is the
    // reversible alternative for callers who want to preserve this history.
    private void cascadeDeleteClinicalRecords(Long userId) {
        // mental_health_academic_analysis references academic_performance, self_assessments
        // and risk_assessments, so it must go first.
        em.createNativeQuery(
                "DELETE FROM mental_health_academic_analysis WHERE client_id = :id OR analyzed_by = :id")
                .setParameter("id", userId).executeUpdate();

        // Session element-collection child tables (Hibernate-managed, no DB-level cascade).
        em.createNativeQuery(
                "DELETE FROM session_tags WHERE session_id IN " +
                        "(SELECT id FROM sessions WHERE student_id = :id OR client_id = :id OR counselor_id = :id)")
                .setParameter("id", userId).executeUpdate();
        em.createNativeQuery(
                "DELETE FROM session_attachments WHERE session_id IN " +
                        "(SELECT id FROM sessions WHERE student_id = :id OR client_id = :id OR counselor_id = :id)")
                .setParameter("id", userId).executeUpdate();

        // Sessions reference appointments, so they must be removed before appointments.
        em.createNativeQuery(
                "DELETE FROM sessions WHERE student_id = :id OR client_id = :id OR counselor_id = :id")
                .setParameter("id", userId).executeUpdate();

        // Case assignments reference cases, so clear them (both by participant and by
        // owning case) before the cases themselves are deleted further down.
        em.createNativeQuery(
                "DELETE FROM case_assignments WHERE assigned_by = :id OR assigned_to = :id " +
                        "OR case_id IN (SELECT id FROM cases WHERE client_id = :id OR counselor_id = :id)")
                .setParameter("id", userId).executeUpdate();

        // Intake/personal-data forms reference cases too; their own element-collection
        // child tables (form_previous_counselling, reasons_*, etc.) cascade at the DB level.
        em.createNativeQuery(
                "DELETE FROM client_intake_forms WHERE client_id = :id OR counselor_id = :id " +
                        "OR case_id IN (SELECT id FROM cases WHERE client_id = :id OR counselor_id = :id)")
                .setParameter("id", userId).executeUpdate();
        em.createNativeQuery(
                "DELETE FROM personal_data_forms WHERE client_id = :id " +
                        "OR case_id IN (SELECT id FROM cases WHERE client_id = :id OR counselor_id = :id)")
                .setParameter("id", userId).executeUpdate();

        // Appointments reference cases, so they must go before cases too.
        em.createNativeQuery(
                "DELETE FROM appointments WHERE student_id = :id OR client_id = :id OR counselor_id = :id " +
                        "OR case_id IN (SELECT id FROM cases WHERE client_id = :id OR counselor_id = :id)")
                .setParameter("id", userId).executeUpdate();

        em.createNativeQuery("DELETE FROM risk_assessments WHERE client_id = :id OR assessor_id = :id")
                .setParameter("id", userId).executeUpdate();
        em.createNativeQuery("DELETE FROM reports WHERE client_id = :id OR counselor_id = :id")
                .setParameter("id", userId).executeUpdate();
        em.createNativeQuery("DELETE FROM goals WHERE client_id = :id")
                .setParameter("id", userId).executeUpdate();
        em.createNativeQuery("DELETE FROM academic_performance WHERE client_id = :id")
                .setParameter("id", userId).executeUpdate();
        em.createNativeQuery("DELETE FROM academic_qualifications WHERE client_id = :id")
                .setParameter("id", userId).executeUpdate();
        em.createNativeQuery("DELETE FROM self_assessments WHERE submitted_by_user_id = :id OR client_id = :id")
                .setParameter("id", userId).executeUpdate();

        // Cases are now safe to remove — every table that referenced them has been cleared.
        em.createNativeQuery("DELETE FROM cases WHERE client_id = :id OR counselor_id = :id")
                .setParameter("id", userId).executeUpdate();
    }

    private void deleteUserOwnedArtifacts(User user) {
        Long userId = user.getId();

        // cascadeDeleteClinicalRecords has already removed every clinical/audit record
        // this user was linked to. Only direct user-owned artifacts and legacy profile
        // rows remain to clean up here.
        deleteByUserIdIfTableExists("notifications", "recipient_id", userId);
        deleteByUserIdIfTableExists("chat_messages", "user_id", userId);
        deleteByUserIdIfTableExists("user_dashboard_config", "user_id", userId);
        deleteByUserIdIfTableExists("keyboard_shortcuts", "user_id", userId);
        deleteByUserIdIfTableExists("user_bookmarks", "user_id", userId);
        deleteByUserIdIfTableExists("user_feedback", "user_id", userId);
        deleteByUserIdIfTableExists("user_consent", "user_id", userId);
        deleteByUserIdIfTableExists("resources", "uploaded_by", userId);
        deleteByUserIdIfTableExists("user_roles", "user_id", userId);
        messageRepository.deleteAllByUserId(userId);

        // Delete legacy profile rows, if present.
        deleteByUserIdIfTableExists("clients", "user_id", userId);
        deleteByUserIdIfTableExists("counselors", "user_id", userId);
        deleteByUserIdIfTableExists("admins", "user_id", userId);

        // Keep the entity graph in sync before user deletion.
        user.getRoles().clear();
    }

    private void deleteByUserIdIfTableExists(String tableName, String userIdColumn, Long userId) {
        if (!tableExists(tableName)) {
            return;
        }

        em.createNativeQuery("DELETE FROM " + tableName + " WHERE " + userIdColumn + " = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    private boolean tableExists(String tableName) {
        Number tableCount = (Number) em.createNativeQuery(
                        "SELECT COUNT(1) " +
                                "FROM information_schema.tables " +
                                "WHERE table_schema = 'public' AND table_name = :tableName"
                )
                .setParameter("tableName", tableName)
                .getSingleResult();

        return tableCount.intValue() > 0;
    }

    private void guardAgainstSelfRemoval(User user) {
        User currentUser = resolveCurrentUser();
        if (currentUser != null && currentUser.getId().equals(user.getId())) {
            throw new ValidationException("Administrators cannot delete or deactivate their own account");
        }
    }

    private void guardAgainstRemovingLastAdministrator(User user) {
        if (!user.hasRole("ROLE_ADMIN") && !user.hasRole("ROLE_SUPER_ADMIN")) {
            return;
        }

        long activeAdministratorCount = userRepository.findActiveByRoleName(Role.ERole.ROLE_ADMIN).size()
                + userRepository.findActiveByRoleName(Role.ERole.ROLE_SUPER_ADMIN).size();
        if (Boolean.TRUE.equals(user.getActive()) && activeAdministratorCount <= 1) {
            throw new ValidationException("Cannot remove the last active administrator");
        }
    }

    private User resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        String identifier;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            identifier = userDetails.getUsername();
        } else {
            identifier = authentication.getName();
        }

        if (identifier == null || identifier.isBlank() || "anonymousUser".equals(identifier)) {
            return null;
        }

        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElse(null);
    }
}
