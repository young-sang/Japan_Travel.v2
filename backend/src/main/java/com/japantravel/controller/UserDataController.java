package com.japantravel.controller;

import com.japantravel.dto.Dtos.*;
import com.japantravel.repository.UserDataRepository;
import com.japantravel.security.CurrentUser;
import com.japantravel.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** favorites · reviews · history 통합 컨트롤러 (세션 사용자 기반). */
@RestController
@RequestMapping("/api")
public class UserDataController {

    private final UserDataRepository repo;
    private final CurrentUser currentUser;
    private final AuditService auditService;
    public UserDataController(UserDataRepository repo, CurrentUser currentUser, AuditService auditService) {
        this.repo = repo;
        this.currentUser = currentUser;
        this.auditService = auditService;
    }

    // ── favorites ──
    @GetMapping("/favorites")
    public List<Favorite> favorites() { return repo.listFavorites(currentUser.userId()); }

    @PostMapping("/favorites")
    public ResponseEntity<Void> addFav(@RequestBody FavoriteCreateRequest req) {
        repo.addFavorite(currentUser.userId(), req.targetType(), req.targetId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/favorites/{type}/{id}")
    public ResponseEntity<Void> removeFav(@PathVariable String type, @PathVariable long id) {
        repo.removeFavorite(currentUser.userId(), type, id);
        return ResponseEntity.noContent().build();
    }

    // ── reviews ──
    // 대상 리뷰 조회는 익명도 가능. mine=true 또는 작성/수정/삭제는 로그인 필요.
    @GetMapping("/reviews")
    public Object reviews(@RequestParam(required = false) String targetType,
                          @RequestParam(required = false) Long targetId,
                          @RequestParam(required = false) Boolean mine) {
        if (Boolean.TRUE.equals(mine)) return repo.reviewsByUser(currentUser.userId());
        if (targetType != null && targetId != null) {
            List<Review> list = repo.reviewsForTarget(targetType, targetId);
            return Map.of("items", list, "average", repo.averageRating(targetType, targetId), "count", list.size());
        }
        return repo.allReviews();
    }

    @PostMapping("/reviews")
    public Review addReview(@RequestBody ReviewCreateRequest req) {
        long uid = currentUser.userId();
        long id = repo.addReview(uid, req.targetType(), req.targetId(),
                req.rating() == null ? 5 : req.rating(), req.comment());
        auditService.log(currentUser, "REVIEW_CREATE", "review", id,
                "target=" + req.targetType() + "/" + req.targetId());
        return repo.reviewsByUser(uid).stream().filter(r -> r.id() == id).findFirst().orElse(null);
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<Void> updateReview(@PathVariable long id, @RequestBody ReviewCreateRequest req) {
        long uid = currentUser.userId();
        if (!repo.reviewExistsForUser(id, uid) && !currentUser.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        repo.updateReview(id, req.rating() == null ? 5 : req.rating(), req.comment());
        auditService.log(currentUser, "REVIEW_UPDATE", "review", id, null);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable long id) {
        long uid = currentUser.userId();
        if (!repo.reviewExistsForUser(id, uid) && !currentUser.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        repo.deleteReview(id);
        auditService.log(currentUser, "REVIEW_DELETE", "review", id, null);
        return ResponseEntity.noContent().build();
    }

    // ── history ──
    @GetMapping("/history")
    public List<HistoryItem> history() { return repo.historyOf(currentUser.userId()); }

    @PostMapping("/history")
    public ResponseEntity<Void> touch(@RequestBody FavoriteCreateRequest req) {
        repo.touchHistory(currentUser.userId(), req.targetType(), req.targetId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory() {
        repo.clearHistory(currentUser.userId());
        return ResponseEntity.noContent().build();
    }
}
