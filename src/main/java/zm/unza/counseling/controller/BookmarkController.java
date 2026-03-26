package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.UserBookmark;
import zm.unza.counseling.repository.UserBookmarkRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/bookmarks", "/v1/bookmarks", "/bookmarks"})
@RequiredArgsConstructor
public class BookmarkController {

    private final UserBookmarkRepository bookmarkRepository;

    /**
     * Add a bookmark
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserBookmark>> addBookmark(
            @RequestBody Map<String, Object> payload) {
        
        UserBookmark bookmark = new UserBookmark();
        bookmark.setUserId(Long.valueOf(payload.get("userId").toString()));
        bookmark.setTitle(payload.get("title").toString());
        bookmark.setDescription(payload.getOrDefault("description", "").toString());
        bookmark.setUrl(payload.get("url").toString());
        bookmark.setIcon(payload.getOrDefault("icon", "").toString());
        bookmark.setCategory(payload.getOrDefault("category", "general").toString());
        
        bookmark = bookmarkRepository.save(bookmark);
        
        return ResponseEntity.ok(ApiResponse.success(bookmark, "Bookmark added successfully"));
    }

    /**
     * Get user's bookmarks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserBookmark>>> getBookmarks(
            @RequestParam Long userId) {
        
        List<UserBookmark> bookmarks = bookmarkRepository.findByUserIdOrderByLastUsedAtDesc(userId);
        return ResponseEntity.ok(ApiResponse.success(bookmarks));
    }

    /**
     * Get frequently used bookmarks
     */
    @GetMapping("/frequent")
    public ResponseEntity<ApiResponse<List<UserBookmark>>> getFrequentBookmarks(
            @RequestParam Long userId) {
        
        List<UserBookmark> bookmarks = bookmarkRepository.findByUserIdOrderByUsageCountDesc(userId);
        return ResponseEntity.ok(ApiResponse.success(bookmarks));
    }

    /**
     * Get bookmarks by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<UserBookmark>>> getBookmarksByCategory(
            @RequestParam Long userId,
            @PathVariable String category) {
        
        List<UserBookmark> bookmarks = bookmarkRepository.findByUserIdAndCategoryOrderByUsageCountDesc(userId, category);
        return ResponseEntity.ok(ApiResponse.success(bookmarks));
    }

    /**
     * Use a bookmark (increment usage count)
     */
    @PostMapping("/{id}/use")
    public ResponseEntity<ApiResponse<UserBookmark>> useBookmark(@PathVariable Long id) {
        
        UserBookmark bookmark = bookmarkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));
        
        bookmark.setUsageCount(bookmark.getUsageCount() + 1);
        bookmark.setLastUsedAt(LocalDateTime.now());
        
        bookmark = bookmarkRepository.save(bookmark);
        
        return ResponseEntity.ok(ApiResponse.success(bookmark));
    }

    /**
     * Update a bookmark
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserBookmark>> updateBookmark(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        
        UserBookmark bookmark = bookmarkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));
        
        if (payload.containsKey("title")) bookmark.setTitle(payload.get("title").toString());
        if (payload.containsKey("description")) bookmark.setDescription(payload.get("description").toString());
        if (payload.containsKey("url")) bookmark.setUrl(payload.get("url").toString());
        if (payload.containsKey("icon")) bookmark.setIcon(payload.get("icon").toString());
        if (payload.containsKey("category")) bookmark.setCategory(payload.get("category").toString());
        
        bookmark = bookmarkRepository.save(bookmark);
        
        return ResponseEntity.ok(ApiResponse.success(bookmark, "Bookmark updated successfully"));
    }

    /**
     * Delete a bookmark
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(@PathVariable Long id) {
        
        bookmarkRepository.deleteById(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Bookmark deleted successfully"));
    }
}
