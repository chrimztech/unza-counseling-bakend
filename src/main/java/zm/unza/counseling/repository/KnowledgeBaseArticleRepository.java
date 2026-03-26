package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.KnowledgeBaseArticle;

import java.util.List;

@Repository
public interface KnowledgeBaseArticleRepository extends JpaRepository<KnowledgeBaseArticle, Long> {
    
    List<KnowledgeBaseArticle> findByPublishedTrueOrderByViewCountDesc();
    
    List<KnowledgeBaseArticle> findByCategoryOrderByViewCountDesc(String category);
    
    List<KnowledgeBaseArticle> findByTitleContainingIgnoreCase(String searchTerm);
}
