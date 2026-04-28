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
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.Counselor;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.AcademicPerformanceRepository;
import zm.unza.counseling.repository.AcademicQualificationRepository;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.CaseAssignmentRepository;
import zm.unza.counseling.repository.CaseRepository;
import zm.unza.counseling.repository.ChatMessageRepository;
import zm.unza.counseling.repository.ClientIntakeFormRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.CounselorRepository;
import zm.unza.counseling.repository.GoalRepository;
import zm.unza.counseling.repository.KeyboardShortcutRepository;
import zm.unza.counseling.repository.MentalHealthAcademicAnalysisRepository;
import zm.unza.counseling.repository.MessageRepository;
import zm.unza.counseling.repository.NotificationRepository;
import zm.unza.counseling.repository.PersonalDataFormRepository;
import zm.unza.counseling.repository.ReportRepository;
import zm.unza.counseling.repository.RiskAssessmentRepository;
import zm.unza.counseling.repository.SelfAssessmentRepository;
import zm.unza.counseling.repository.SessionRepository;
import zm.unza.counseling.repository.UserBookmarkRepository;
import zm.unza.counseling.repository.UserConsentRepository;
import zm.unza.counseling.repository.UserDashboardConfigRepository;
import zm.unza.counseling.repository.UserFeedbackRepository;
import zm.unza.counseling.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationRepository notificationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserDashboardConfigRepository userDashboardConfigRepository;
    private final KeyboardShortcutRepository keyboardShortcutRepository;
    private final UserBookmarkRepository userBookmarkRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final UserConsentRepository userConsentRepository;
    private final MessageRepository messageRepository;
    private final ClientRepository clientRepository;
    private final CounselorRepository counselorRepository;
    private final CaseRepository caseRepository;
    private final AppointmentRepository appointmentRepository;
    private final SessionRepository sessionRepository;
    private final CaseAssignmentRepository caseAssignmentRepository;
    private final ClientIntakeFormRepository clientIntakeFormRepository;
    private final PersonalDataFormRepository personalDataFormRepository;
    private final ReportRepository reportRepository;
    private final SelfAssessmentRepository selfAssessmentRepository;
    private final GoalRepository goalRepository;
    private final AcademicPerformanceRepository academicPerformanceRepository;
    private final AcademicQualificationRepository academicQualificationRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final MentalHealthAcademicAnalysisRepository mentalHealthAcademicAnalysisRepository;

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
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
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
        ensureUserCanBePermanentlyDeleted(user);
        deleteUserOwnedArtifacts(user);
        userRepository.delete(user);
    }

    private void ensureUserCanBePermanentlyDeleted(User user) {
        List<String> blockers = new ArrayList<>();
        Long userId = user.getId();

        if (appointmentRepository.countAllByStudentId(userId) > 0) {
            blockers.add("student appointment history");
        }
        if (sessionRepository.countByStudentId(userId) > 0) {
            blockers.add("student session records");
        }
        if (selfAssessmentRepository.countBySubmittedByUserId(userId) > 0) {
            blockers.add("self-assessment history");
        }
        if (caseAssignmentRepository.existsByAssignedBy(user)) {
            blockers.add("case assignment audit history");
        }

        Optional<Client> clientRecord = clientRepository.findById(userId);
        if (clientRecord.isPresent()) {
            Client client = clientRecord.get();
            if (caseRepository.countByClient(client) > 0) {
                blockers.add("case files");
            }
            if (appointmentRepository.countAllByClientId(userId) > 0) {
                blockers.add("client-linked appointments");
            }
            if (sessionRepository.countByClientId(userId) > 0) {
                blockers.add("client counseling sessions");
            }
            if (clientIntakeFormRepository.existsByClientId(userId)) {
                blockers.add("client intake forms");
            }
            if (personalDataFormRepository.existsByClientId(userId)) {
                blockers.add("personal data forms");
            }
            if (goalRepository.countByClientId(userId) > 0) {
                blockers.add("goal records");
            }
            if (academicPerformanceRepository.countByClientId(userId) > 0) {
                blockers.add("academic performance records");
            }
            if (academicQualificationRepository.countByClientId(userId) > 0) {
                blockers.add("academic qualification records");
            }
            if (riskAssessmentRepository.countByClientId(userId) > 0) {
                blockers.add("risk assessments");
            }
            if (mentalHealthAcademicAnalysisRepository.countByClientId(userId) > 0) {
                blockers.add("mental health analyses");
            }
            if (reportRepository.existsByClientId(userId)) {
                blockers.add("generated reports");
            }
        }

        Optional<Counselor> counselorRecord = counselorRepository.findById(userId);
        if (counselorRecord.isPresent()) {
            Counselor counselor = counselorRecord.get();
            if (caseRepository.existsByCounselor(counselor)) {
                blockers.add("assigned case files");
            }
            if (appointmentRepository.countAllByCounselorId(userId) > 0) {
                blockers.add("counselor appointment history");
            }
            if (sessionRepository.countByCounselorId(userId) > 0) {
                blockers.add("counselor session records");
            }
            if (caseAssignmentRepository.existsByAssignedTo(counselor)) {
                blockers.add("case assignment history");
            }
            if (clientIntakeFormRepository.existsByCounselor(counselor)) {
                blockers.add("counselor-authored intake forms");
            }
            if (reportRepository.existsByCounselorId(userId)) {
                blockers.add("generated reports");
            }
        }

        if (!blockers.isEmpty()) {
            throw new ValidationException(
                    "Permanent delete is blocked because this user still has protected records: "
                            + String.join(", ", blockers)
                            + ". Deactivate the user instead to preserve clinical and audit history."
            );
        }
    }

    private void deleteUserOwnedArtifacts(User user) {
        Long userId = user.getId();

        // At this point ensureUserCanBePermanentlyDeleted has already verified that
        // the user has no protected clinical/audit records. Only delete direct
        // user-owned artifacts and legacy profile rows here.
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
