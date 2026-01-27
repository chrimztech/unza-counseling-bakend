package zm.unza.counseling.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Session;
import zm.unza.counseling.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByStudent(User student);

    Page<Session> findByStudent(User student, Pageable pageable);

    List<Session> findByCounselor(User counselor);

    Page<Session> findByCounselor(User counselor, Pageable pageable);

    List<Session> findBySessionDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Session s WHERE s.counselor = :counselor AND s.sessionDate >= :start AND s.sessionDate <= :end")
    List<Session> findByCounselorAndDateRange(@Param("counselor") User counselor, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM Session s WHERE s.student = :student AND s.sessionDate >= :start AND s.sessionDate <= :end")
    List<Session> findByStudentAndDateRange(@Param("student") User student, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.counselor = :counselor")
    Long countByCounselor(@Param("counselor") User counselor);

    @Query("SELECT s FROM Session s WHERE " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.presentingIssue) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Session> searchSessions(@Param("keyword") String keyword, Pageable pageable);

    Page<Session> findByStudentId(Long studentId, Pageable pageable);
}