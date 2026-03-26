package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.KnowledgeBaseArticle;
import zm.unza.counseling.repository.KnowledgeBaseArticleRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/knowledge-base", "/v1/knowledge-base", "/knowledge-base"})
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseArticleRepository articleRepository;

    /**
     * Get all published articles (public)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<KnowledgeBaseArticle>>> getPublishedArticles() {
        
        List<KnowledgeBaseArticle> articles = articleRepository.findByPublishedTrueOrderByViewCountDesc();
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    /**
     * Get article by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KnowledgeBaseArticle>> getArticle(@PathVariable Long id) {
        
        KnowledgeBaseArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        // Increment view count
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);
        
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    /**
     * Search articles
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KnowledgeBaseArticle>>> searchArticles(
            @RequestParam String q) {
        
        List<KnowledgeBaseArticle> articles = articleRepository.findByTitleContainingIgnoreCase(q);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    /**
     * Get articles by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<KnowledgeBaseArticle>>> getArticlesByCategory(
            @PathVariable String category) {
        
        List<KnowledgeBaseArticle> articles = articleRepository.findByCategoryOrderByViewCountDesc(category);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    /**
     * Create article (admin/staff only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<KnowledgeBaseArticle>> createArticle(
            @RequestBody Map<String, Object> payload) {
        
        KnowledgeBaseArticle article = new KnowledgeBaseArticle();
        article.setTitle(payload.get("title").toString());
        article.setContent(payload.getOrDefault("content", "").toString());
        article.setCategory(payload.getOrDefault("category", "general").toString());
        article.setTags(payload.getOrDefault("tags", "").toString());
        article.setAuthor(payload.getOrDefault("author", "System").toString());
        article.setPublished(false);
        
        article = articleRepository.save(article);
        
        return ResponseEntity.ok(ApiResponse.success(article, "Article created successfully"));
    }

    /**
     * Update article (admin/staff only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<ApiResponse<KnowledgeBaseArticle>> updateArticle(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        
        KnowledgeBaseArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        if (payload.containsKey("title")) article.setTitle(payload.get("title").toString());
        if (payload.containsKey("content")) article.setContent(payload.get("content").toString());
        if (payload.containsKey("category")) article.setCategory(payload.get("category").toString());
        if (payload.containsKey("tags")) article.setTags(payload.get("tags").toString());
        if (payload.containsKey("published")) article.setPublished(Boolean.valueOf(payload.get("published").toString()));
        
        article = articleRepository.save(article);
        
        return ResponseEntity.ok(ApiResponse.success(article, "Article updated successfully"));
    }

    /**
     * Delete article (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        
        articleRepository.deleteById(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Article deleted successfully"));
    }
}
