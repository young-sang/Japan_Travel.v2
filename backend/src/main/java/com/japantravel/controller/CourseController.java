package com.japantravel.controller;

import com.japantravel.dto.Dtos.Course;
import com.japantravel.repository.CourseRepository;
import com.japantravel.security.AppUserPrincipal;
import com.japantravel.security.CurrentUser;
import com.japantravel.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository repo;
    private final CurrentUser currentUser;
    private final AuditService auditService;
    public CourseController(CourseRepository repo, CurrentUser currentUser, AuditService auditService) {
        this.repo = repo;
        this.currentUser = currentUser;
        this.auditService = auditService;
    }

    // 공개 코스 목록(owner_user_id IS NULL 또는 published) — 익명 가능.
    // mine=true 면 현재 사용자의 코스만.
    @GetMapping
    public List<Course> list(@RequestParam(required = false) String prefecture,
                             @RequestParam(required = false) String tag,
                             @RequestParam(required = false) Boolean mine,
                             @RequestParam(required = false) String status) {
        Long ownerId = null;
        if (Boolean.TRUE.equals(mine)) {
            AppUserPrincipal p = currentUser.principalOrNull();
            if (p == null) return List.of();
            ownerId = p.getId();
        }
        return repo.findAll(prefecture, tag, ownerId, status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> one(@PathVariable long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Course> create(@RequestBody Course req) {
        long uid = currentUser.userId();
        long id = repo.create(req.title(), req.prefecture(),
                req.tags() == null ? List.of() : req.tags(),
                req.duration(), req.imagePath(), req.centerLat(), req.centerLng(),
                req.timeline() == null ? List.of() : req.timeline(),
                true, uid,
                req.status() == null ? "published" : req.status());
        auditService.log(currentUser, "COURSE_CREATE", "course", id, "title=" + req.title());
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.internalServerError().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable long id, @RequestBody Course req) {
        var existing = repo.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        if (!isOwnerOrAdmin(existing.get())) return ResponseEntity.status(403).build();
        repo.update(id, req.title(), req.prefecture(),
                req.tags() == null ? List.of() : req.tags(),
                req.duration(), req.imagePath(), req.centerLat(), req.centerLng(),
                req.timeline() == null ? List.of() : req.timeline(),
                req.status() == null ? "published" : req.status());
        auditService.log(currentUser, "COURSE_UPDATE", "course", id, null);
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        var existing = repo.findById(id);
        if (existing.isEmpty()) return ResponseEntity.noContent().build();
        if (!isOwnerOrAdmin(existing.get())) return ResponseEntity.status(403).build();
        repo.delete(id);
        auditService.log(currentUser, "COURSE_DELETE", "course", id, null);
        return ResponseEntity.noContent().build();
    }

    private boolean isOwnerOrAdmin(Course c) {
        AppUserPrincipal p = currentUser.principalOrNull();
        if (p == null) return false;
        if ("ADMIN".equals(p.getRole())) return true;
        return c.ownerUserId() != null && c.ownerUserId() == p.getId();
    }
}
