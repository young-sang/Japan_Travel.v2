package com.japantravel.controller;

import com.japantravel.dto.Dtos.Course;
import com.japantravel.repository.CourseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private static final long USER_ID = 1L;
    private final CourseRepository repo;
    public CourseController(CourseRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Course> list(@RequestParam(required = false) String prefecture,
                             @RequestParam(required = false) String tag,
                             @RequestParam(required = false) Long ownerUserId,
                             @RequestParam(required = false) String status) {
        return repo.findAll(prefecture, tag, ownerUserId, status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> one(@PathVariable long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Course> create(@RequestBody Course req) {
        long id = repo.create(req.title(), req.prefecture(),
                req.tags() == null ? List.of() : req.tags(),
                req.duration(), req.imagePath(), req.centerLat(), req.centerLng(),
                req.timeline() == null ? List.of() : req.timeline(),
                true, USER_ID,
                req.status() == null ? "published" : req.status());
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.internalServerError().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable long id, @RequestBody Course req) {
        if (repo.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        repo.update(id, req.title(), req.prefecture(),
                req.tags() == null ? List.of() : req.tags(),
                req.duration(), req.imagePath(), req.centerLat(), req.centerLng(),
                req.timeline() == null ? List.of() : req.timeline(),
                req.status() == null ? "published" : req.status());
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        repo.delete(id);
        return ResponseEntity.noContent().build();
    }
}
