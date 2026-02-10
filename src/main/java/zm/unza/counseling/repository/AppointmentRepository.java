package zm.unza.counseling.repository;

import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Appointment.AppointmentStatus;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
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
    
    Page<Appointment> findByStudent(User student, Pageable pageable);
    
    List<Appointment> findByStudentAndStatus(User student, AppointmentStatus status);
    
    // Client appointments (appointments where client is the user receiving counseling)
    Page<Appointment> findByClient(Client client, Pageable pageable);
    
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
    
    Page<Appointment> findByCounselor(User counselor, Pageable pageable);
    
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
    @Query("SELECT a FROM Appointment a WHERE a.student = :student " +
           "AND a.status = 'SCHEDULED' AND a.appointmentDate > :now " +
           "ORDER BY a.appointmentDate LIMIT 1")
    Optional<Appointment> findNextAppointmentByStudent(@Param("student") User student, @Param("now") LocalDateTime now);
    
    @Query("SELECT a FROM Appointment a WHERE a.counselor = :counselor " +
           "AND a.status = 'SCHEDULED' AND a.appointmentDate > :now " +
           "ORDER BY a.appointmentDate LIMIT 1")
    Optional<Appointment> findNextAppointmentByCounselor(@Param("counselor") User counselor, @Param("now") LocalDateTime now);

    // Conflict detection
    @Query("SELECT a FROM Appointment a WHERE a.counselor = :counselor " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND ((a.appointmentDate <= :start AND :start < FUNCTION('ADDTIME', a.appointmentDate, CONCAT(a.duration, ':00'))) " +
           "OR (a.appointmentDate < :end AND :end <= FUNCTION('ADDTIME', a.appointmentDate, CONCAT(a.duration, ':00'))) " +
           "OR (:start <= a.appointmentDate AND FUNCTION('ADDTIME', a.appointmentDate, CONCAT(a.duration, ':00')) <= :end))")
    List<Appointment> findConflictingAppointments(
        @Param("counselor") User counselor,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
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
}