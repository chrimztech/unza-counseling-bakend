package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Message;
import zm.unza.counseling.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRecipientOrderBySentAtDesc(User recipient);
    List<Message> findBySenderOrderBySentAtDesc(User sender);
    
    List<Message> findByConversationIdOrderBySentAtDesc(Long conversationId);
    
    List<Message> findBySenderIdOrRecipientId(Long senderId, Long recipientId);
    
    List<Message> findByContentContaining(String content);
    
    // Conversation queries
    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId OR m.recipient.id = :userId " +
           "ORDER BY m.sentAt DESC")
    List<Message> findAllMessagesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :userId THEN m.recipient.id ELSE m.sender.id END " +
           "FROM Message m WHERE m.sender.id = :userId OR m.recipient.id = :userId")
    List<Long> findDistinctConversationPartners(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId AND m.recipient.id = :partnerId) OR " +
           "(m.sender.id = :partnerId AND m.recipient.id = :userId) " +
           "ORDER BY m.sentAt DESC")
    List<Message> findConversationMessages(@Param("userId") Long userId, @Param("partnerId") Long partnerId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId AND m.recipient.id = :partnerId) OR " +
           "(m.sender.id = :partnerId AND m.recipient.id = :userId) " +
           "ORDER BY m.sentAt DESC LIMIT 1")
    Optional<Message> findLastMessageInConversation(@Param("userId") Long userId, @Param("partnerId") Long partnerId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient.id = :userId AND m.sender.id = :partnerId AND m.isRead = false")
    long countUnreadMessagesFromPartner(@Param("userId") Long userId, @Param("partnerId") Long partnerId);
    
    // Unread count
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient.id = :userId AND m.isRead = false")
    long countUnreadMessages(@Param("userId") Long userId);
    
    // Statistics
    @Query("SELECT COUNT(m) FROM Message m WHERE m.sender.id = :userId OR m.recipient.id = :userId")
    long countTotalMessages(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.sender.id = :userId")
    long countSentMessages(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient.id = :userId")
    long countReceivedMessages(@Param("userId") Long userId);
    
    // Archived messages
    @Query("SELECT m FROM Message m WHERE m.recipient.id = :userId AND m.isArchived = true ORDER BY m.sentAt DESC")
    List<Message> findArchivedMessages(@Param("userId") Long userId);
    
    // Starred messages
    @Query("SELECT m FROM Message m WHERE m.recipient.id = :userId AND m.isStarred = true ORDER BY m.sentAt DESC")
    List<Message> findStarredMessages(@Param("userId") Long userId);
    
    // Bulk operations
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.recipient.id = :userId AND m.id IN :messageIds")
    int bulkMarkAsRead(@Param("messageIds") List<Long> messageIds, @Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Message m SET m.isArchived = true WHERE m.recipient.id = :userId AND m.id = :messageId")
    void archiveMessage(@Param("messageId") Long messageId, @Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Message m SET m.isArchived = false WHERE m.recipient.id = :userId AND m.id = :messageId")
    void unarchiveMessage(@Param("messageId") Long messageId, @Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Message m SET m.isStarred = true WHERE m.recipient.id = :userId AND m.id = :messageId")
    void starMessage(@Param("messageId") Long messageId, @Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Message m SET m.isStarred = false WHERE m.recipient.id = :userId AND m.id = :messageId")
    void unstarMessage(@Param("messageId") Long messageId, @Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.recipient.id = :userId AND (m.sender.id = :partnerId OR :partnerId IS NULL)")
    void markAllMessagesAsRead(@Param("userId") Long userId, @Param("partnerId") Long partnerId);
    
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.recipient.id = :userId")
    void markAllMessagesAsReadByUser(@Param("userId") Long userId);
}