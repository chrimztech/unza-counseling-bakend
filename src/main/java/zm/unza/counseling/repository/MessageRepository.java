package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Message;
import zm.unza.counseling.entity.User;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRecipientOrderBySentAtDesc(User recipient);
    List<Message> findBySenderOrderBySentAtDesc(User sender);
}