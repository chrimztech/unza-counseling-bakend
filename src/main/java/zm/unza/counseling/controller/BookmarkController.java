package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.entity.UserBookmark;
import zm.unza.counseling.repository.UserBookmarkRepository;
import zm.unza.counseling.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/bookmarks", "/v1/bookmarks", "/bookmarks"})
@RequiredArgsConstructor
public class BookmarkController {

    private final UserBookmarkRepository bookmarkRepository;
    private final UserService userService;

    /**
     * Resolve the authenticated caller's user id. The user id is always derived from the
     * SecurityContext/Principal rather than trusted from request input, to prevent a caller
     * from acting on another user's bookmarks (IDOR).
     */
    private Long currentUserId(Authentication authentication) {
        return userService.getUserByEmail(authentication.getName()).getId();
    }

    private void assertOwner(UserBookmark bookmark, Long userId) {
        if (bookmark.getUserId() == null || !bookmark.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to access this bookmark");
        }
    }

    /**
     * Add a bookmark
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserBookmark>> addBookmark(
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {

        UserBookmark bookmark = new UserBookmark();
        bookmark.setUserId(currentUserId(authentication));
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
    public ResponseEntity<ApiResponse<List<UserBookmark>>> getBookmarks(Authentication authentication) {

        List<UserBookmark> bookmarks = bookmarkRepository.findByUserIdOrderByLastUsedAtDesc(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success(bookmarks));
    }

    /**
     * Get frequently used bookmarks
     */
    @GetMapping("/frequent")
    public ResponseEntity<ApiResponse<List<UserBookmark>>> getFrequentBookmarks(Authentication authentication) {

        List<UserBookmark> bookmarks = bookmarkRepository.findByUserIdOrderByUsageCountDesc(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.success(bookmarks));
    }

    /**
     * Get bookmarks by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<UserBookmark>>> getBookmarksByCategory(
            @PathVariable String category,
            Authentication authentication) {

        List<UserBookmark> bookmarks = bookmarkRepository.findByUserIdAndCategoryOrderByUsageCountDesc(currentUserId(authentication), category);
        return ResponseEntity.ok(ApiResponse.success(bookmarks));
    }

    /**
     * Use a bookmark (increment usage count)
     */
    @PostMapping("/{id}/use")
    public ResponseEntity<ApiResponse<UserBookmark>> useBookmark(@PathVariable Long id, Authentication authentication) {

        UserBookmark bookmark = bookmarkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));
        assertOwner(bookmark, currentUserId(authentication));

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
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {

        UserBookmark bookmark = bookmarkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));
        assertOwner(bookmark, currentUserId(authentication));

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
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(@PathVariable Long id, Authentication authentication) {

        UserBookmark bookmark = bookmarkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));
        assertOwner(bookmark, currentUserId(authentication));

        bookmarkRepository.deleteById(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Bookmark deleted successfully"));
    }
}
