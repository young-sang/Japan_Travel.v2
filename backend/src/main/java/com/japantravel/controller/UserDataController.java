package com.japantravel.controller;

import com.japantravel.dto.Dtos.*;
import com.japantravel.repository.UserDataRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** favorites · reviews · history 통합 컨트롤러 (단일 사용자 user_id=1). */
@RestController
@RequestMapping("/api")
public class UserDataController {

    private static final long USER_ID = 1L;
    private final UserDataRepository repo;
    public UserDataController(UserDataRepository repo) { this.repo = repo; }

    // ── favorites ──
    @GetMapping("/favorites")
    public List<Favorite> favorites() { return repo.listFavorites(USER_ID); }

    @PostMapping("/favorites")
    public ResponseEntity<Void> addFav(@RequestBody FavoriteCreateRequest req) {
        repo.addFavorite(USER_ID, req.targetType(), req.targetId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/favorites/{type}/{id}")
    public ResponseEntity<Void> removeFav(@PathVariable String type, @PathVariable long id) {
        repo.removeFavorite(USER_ID, type, id);
        return ResponseEntity.noContent().build();
    }

    // ── reviews ──
    @GetMapping("/reviews")
    public Object reviews(@RequestParam(required = false) String targetType,
                          @RequestParam(required = false) Long targetId,
                          @RequestParam(required = false) Boolean mine) {
        if (Boolean.TRUE.equals(mine)) return repo.reviewsByUser(USER_ID);
        if (targetType != null && targetId != null) {
            List<Review> list = repo.reviewsForTarget(targetType, targetId);
            return Map.of("items", list, "average", repo.averageRating(targetType, targetId), "count", list.size());
        }
        return repo.allReviews();
    }

    @PostMapping("/reviews")
    public Review addReview(@RequestBody ReviewCreateRequest req) {
        long id = repo.addReview(USER_ID, req.targetType(), req.targetId(),
                req.rating() == null ? 5 : req.rating(), req.comment());
        return repo.reviewsByUser(USER_ID).stream().filter(r -> r.id() == id).findFirst().orElse(null);
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<Void> updateReview(@PathVariable long id, @RequestBody ReviewCreateRequest req) {
        repo.updateReview(id, req.rating() == null ? 5 : req.rating(), req.comment());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable long id) {
        repo.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    // ── history ──
    @GetMapping("/history")
    public List<HistoryItem> history() { return repo.historyOf(USER_ID); }

    @PostMapping("/history")
    public ResponseEntity<Void> touch(@RequestBody FavoriteCreateRequest req) {
        repo.touchHistory(USER_ID, req.targetType(), req.targetId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory() {
        repo.clearHistory(USER_ID);
        return ResponseEntity.noContent().build();
    }
}
