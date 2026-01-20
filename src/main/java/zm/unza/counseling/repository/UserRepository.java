package zm.unza.counseling.repository;

import zm.unza.counseling.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Repository - Data access for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic queries
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);
    
    Optional<User> findByStudentId(String studentId);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    Boolean existsByStudentId(String studentId);

    // Role-based queries
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.active = true")
    List<User> findActiveByRoleName(@Param("roleName") String roleName);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.availableForAppointments = true")
    List<User> findAvailableCounselors(@Param("roleName") String roleName);

    // Search queries
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.studentId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Search users by email, first name, or last name with pagination
     * @param email the email to search for
     * @param firstName the first name to search for
     * @param lastName the last name to search for
     * @param pageable pagination information
     * @return paginated list of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    Page<User> findByEmailContainingOrFirstNameContainingOrLastNameContaining(
            @Param("email") String email,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            Pageable pageable);

    // Active/Inactive users
    List<User> findByActive(Boolean active);
    
    Page<User> findByActive(Boolean active, Pageable pageable);

    // Department queries
    List<User> findByDepartment(String department);
    
    @Query("SELECT DISTINCT u.department FROM User u WHERE u.department IS NOT NULL ORDER BY u.department")
    List<String> findAllDepartments();

    // Statistics queries
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = 'ROLE_STUDENT'")
    Long countStudents();
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = 'ROLE_COUNSELOR'")
    Long countCounselors();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    Long countActiveUsers();

    // Recent users
    List<User> findTop10ByOrderByCreatedAtDesc();
    
    List<User> findByCreatedAtAfter(LocalDateTime date);

    // Password reset
    Optional<User> findByResetPasswordToken(String token);
    
    @Query("SELECT u FROM User u WHERE u.resetPasswordToken = :token AND u.resetPasswordExpiry > :now")
    Optional<User> findByValidResetToken(@Param("token") String token, @Param("now") LocalDateTime now);

    // Counselor-specific queries
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_COUNSELOR' " +
           "AND u.specialization = :specialization AND u.availableForAppointments = true")
    List<User> findAvailableCounselorsBySpecialization(@Param("specialization") String specialization);

    // Additional methods for enhanced user management
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRolesName(@Param("roleName") String roleName);
    
    @Query("SELECT u FROM User u WHERE u.active = true")
    Page<User> findByActiveTrue(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.active = false")
    Page<User> findByActiveFalse(Pageable pageable);
    
    @Query("SELECT DISTINCT r.name FROM User u JOIN u.roles r")
    List<String> findAllRoles();
    
    @Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r GROUP BY r.name")
    List<Object[]> countUsersByRole();
}