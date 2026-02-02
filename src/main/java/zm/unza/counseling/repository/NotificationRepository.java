package zm.unza.counseling.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(Long recipientId, Boolean isRead);

    long countByRecipientIdAndIsRead(Long recipientId, Boolean isRead);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = ?1 ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdOrderByCreatedAtDescNative(Long recipientId);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = ?1 AND n.isRead = ?2 ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDescNative(Long recipientId, Boolean isRead);
}
