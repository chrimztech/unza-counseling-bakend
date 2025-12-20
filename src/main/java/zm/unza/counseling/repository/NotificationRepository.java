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

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(String recipientId, Boolean isRead);

    long countByRecipientIdAndIsRead(String recipientId, Boolean isRead);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = ?1 ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdOrderByCreatedAtDescNative(String recipientId);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = ?1 AND n.isRead = ?2 ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDescNative(String recipientId, Boolean isRead);
}
