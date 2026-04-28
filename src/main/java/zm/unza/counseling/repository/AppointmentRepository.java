package zm.unza.counseling.repository;

import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Appointment.AppointmentStatus;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Appointment Repository - Data access for Appointment entity
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Student appointments
    List<Appointment> findByStudent(User student);

    boolean existsByStudent(User student);
    
    Page<Appointment> findByStudent(User student, Pageable pageable);

    Page<Appointment> findByStudentAndStatus(User student, AppointmentStatus status, Pageable pageable);

    Page<Appointment> findByStudentAndAppointmentDateAfter(User student, LocalDateTime start, Pageable pageable);

    Page<Appointment> findByStudentAndAppointmentDateBefore(User student, LocalDateTime end, Pageable pageable);

    Page<Appointment> findByStudentAndAppointmentDateBetween(User student, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    List<Appointment> findByStudentAndStatus(User student, AppointmentStatus status);
    
    // Client appointments (appointments where client is the user receiving counseling)
    boolean existsByClient(Client client);

    Page<Appointment> findByClient(Client client, Pageable pageable);

    Page<Appointment> findByClientAndStatus(Client client, AppointmentStatus status, Pageable pageable);

    Page<Appointment> findByClientAndAppointmentDateAfter(Client client, LocalDateTime start, Pageable pageable);

    Page<Appointment> findByClientAndAppointmentDateBefore(Client client, LocalDateTime end, Pageable pageable);

    Page<Appointment> findByClientAndAppointmentDateBetween(Client client, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("SELECT a FROM Appointment a WHERE a.student = :student " +
           "AND a.appointmentDate >= :start AND a.appointmentDate <= :end " +
           "ORDER BY a.appointmentDate")
    List<Appointment> findByStudentAndDateRange(
        @Param("student") User student,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // Counselor appointments
    List<Appointment> findByCounselor(User counselor);

    boolean existsByCounselor(User counselor);
    
    Page<Appointment> findByCounselor(User counselor, Pageable pageable);

    Page<Appointment> findByCounselorAndStatus(User counselor, AppointmentStatus status, Pageable pageable);

    Page<Appointment> findByCounselorAndAppointmentDateAfter(User counselor, LocalDateTime start, Pageable pageable);

    Page<Appointment> findByCounselorAndAppointmentDateBefore(User counselor, LocalDateTime end, Pageable pageable);

    Page<Appointment> findByCounselorAndAppointmentDateBetween(User counselor, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    List<Appointment> findByCounselorAndStatus(User counselor, AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.counselor = :counselor " +
           "AND a.appointmentDate >= :start AND a.appointmentDate <= :end " +
           "ORDER BY a.appointmentDate")
    List<Appointment> findByCounselorAndDateRange(
        @Param("counselor") User counselor,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // Status-based queries
    List<Appointment> findByStatus(AppointmentStatus status);
    
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
    
    Page<Appointment> findByAppointmentDateAfter(LocalDateTime start, Pageable pageable);
    
    Page<Appointment> findByAppointmentDateBefore(LocalDateTime end, Pageable pageable);
    
    @Query("SELECT a FROM Appointment a WHERE a.status = :status " +
           "AND a.appointmentDate BETWEEN :start AND :end")
    List<Appointment> findByStatusAndDateRange(
        @Param("status") AppointmentStatus status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // Upcoming appointments
    @Query("SELECT a FROM Appointment a WHERE a.student = :student " +
           "AND a.status = 'SCHEDULED' AND a.appointmentDate > :now " +
           "ORDER BY a.appointmentDate")
    List<Appointment> findUpcomingByStudent(@Param("student") User student, @Param("now") LocalDateTime now);
    
    @Query("SELECT a FROM Appointment a WHERE a.counselor = :counselor " +
           "AND a.status = 'SCHEDULED' AND a.appointmentDate > :now " +
           "ORDER BY a.appointmentDate")
    List<Appointment> findUpcomingByCounselor(@Param("counselor") User counselor, @Param("now") LocalDateTime now);

    // Today's appointments
    @Query("SELECT a FROM Appointment a WHERE a.counselor = :counselor " +
           "AND DATE(a.appointmentDate) = CURRENT_DATE " +
           "ORDER BY a.appointmentDate")
    List<Appointment> findTodayAppointmentsByCounselor(@Param("counselor") User counselor);
    
    @Query("SELECT a FROM Appointment a WHERE a.student = :student " +
           "AND DATE(a.appointmentDate) = CURRENT_DATE " +
           "ORDER BY a.appointmentDate")
    List<Appointment> findTodayAppointmentsByStudent(@Param("student") User student);

    // Next appointment
    Optional<Appointment> findFirstByStudentAndStatusAndAppointmentDateAfterOrderByAppointmentDateAsc(
            User student,
            AppointmentStatus status,
            LocalDateTime now
    );

    Optional<Appointment> findFirstByCounselorAndStatusAndAppointmentDateAfterOrderByAppointmentDateAsc(
            User counselor,
            AppointmentStatus status,
            LocalDateTime now
    );

    // Reminders
    @Query("SELECT a FROM Appointment a WHERE a.status = 'SCHEDULED' " +
           "AND a.reminderSent = false " +
           "AND a.appointmentDate BETWEEN :start AND :end")
    List<Appointment> findAppointmentsNeedingReminder(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // Statistics
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.counselor = :counselor AND a.status = 'COMPLETED'")
    Long countCompletedAppointmentsByCounselor(@Param("counselor") User counselor);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.student = :student AND a.status = 'COMPLETED'")
    Long countCompletedAppointmentsByStudent(@Param("student") User student);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status " +
           "AND a.appointmentDate BETWEEN :start AND :end")
    Long countByStatusAndDateRange(
        @Param("status") AppointmentStatus status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // Search
    @Query("SELECT a FROM Appointment a WHERE " +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Appointment> searchAppointments(@Param("keyword") String keyword, Pageable pageable);

    Page<Appointment> findByAppointmentDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Unassigned appointments (appointments without a counselor)
    @Query("SELECT a FROM Appointment a WHERE a.counselor IS NULL " +
           "AND a.status = 'UNASSIGNED' " +
           "AND a.appointmentDate > :now " +
           "ORDER BY a.appointmentDate")
    Page<Appointment> findUnassignedAppointments(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.counselor IS NULL")
    Long countUnassignedAppointments();

    // Dashboard query with eager fetching to avoid lazy-loading issues
    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN FETCH a.student " +
           "LEFT JOIN FETCH a.counselor " +
           "WHERE a.status = :status " +
           "AND a.appointmentDate BETWEEN :start AND :end " +
           "ORDER BY a.appointmentDate")
    List<Appointment> findByStatusAndDateRangeWithFetch(
        @Param("status") AppointmentStatus status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // Case-based queries
    List<Appointment> findByCaseEntity(Case caseEntity);

    Page<Appointment> findByCaseEntity(Case caseEntity, Pageable pageable);

    long countByCaseEntity(Case caseEntity);

    @Query(value = "SELECT COUNT(*) FROM appointments a WHERE a.student_id = :userId", nativeQuery = true)
    long countAllByStudentId(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) FROM appointments a WHERE a.client_id = :clientId", nativeQuery = true)
    long countAllByClientId(@Param("clientId") Long clientId);

    @Query(value = "SELECT COUNT(*) FROM appointments a WHERE a.counselor_id = :counselorId", nativeQuery = true)
    long countAllByCounselorId(@Param("counselorId") Long counselorId);

    @Query(value = "SELECT COUNT(*) FROM appointments a WHERE a.case_id = :caseId", nativeQuery = true)
    long countAllByCaseId(@Param("caseId") Long caseId);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "UPDATE appointments SET case_id = NULL WHERE case_id = :caseId", nativeQuery = true)
    int unlinkAllByCaseId(@Param("caseId") Long caseId);
     
    List<Appointment> findByCaseEntityAndStatus(Case caseEntity, AppointmentStatus status);

    // Batch deletes for user permanent deletion
    @Modifying
    @Query(value = "DELETE FROM appointments WHERE client_id = :clientId", nativeQuery = true)
    void deleteAllByClientId(@Param("clientId") Long clientId);

    @Modifying
    @Query(value = "DELETE FROM appointments WHERE counselor_id = :counselorId", nativeQuery = true)
    void deleteAllByCounselorId(@Param("counselorId") Long counselorId);
}
