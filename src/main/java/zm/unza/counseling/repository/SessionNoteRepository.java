package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.SessionNote;
import java.util.List;

@Repository
public interface SessionNoteRepository extends JpaRepository<SessionNote, Long> {
    List<SessionNote> findBySessionId(Long sessionId);
}
