package com.japantravel.controller;

import com.japantravel.collector.WikipediaCollector;
import com.japantravel.dto.Dtos.CollectorRunRequest;
import com.japantravel.dto.Dtos.CollectorRunStatus;
import com.japantravel.repository.*;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final WikipediaCollector collector;
    private final CollectorRunRepository runs;
    private final DestinationRepository destRepo;
    private final FestivalRepository festRepo;
    private final CourseRepository courseRepo;
    private final UserDataRepository userData;
    private final CacheManager cacheManager;

    public AdminController(WikipediaCollector collector, CollectorRunRepository runs,
                           DestinationRepository d, FestivalRepository f, CourseRepository c,
                           UserDataRepository ud, CacheManager cm) {
        this.collector = collector; this.runs = runs;
        this.destRepo = d; this.festRepo = f; this.courseRepo = c; this.userData = ud;
        this.cacheManager = cm;
    }

    // ── stats ─────────────────────────────────────────────────────────────
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of(
                "destinations", destRepo.count(),
                "festivals", festRepo.count(),
                "courses", courseRepo.count(),
                "favorites", userData.countFavorites(1L),
                "reviews", userData.allReviews().size(),
                "prefecturesCovered", destRepo.distinctPrefectures().size()
        );
    }

    // ── collector ─────────────────────────────────────────────────────────
    @PostMapping("/collector/run")
    public ResponseEntity<Map<String, Object>> run(@RequestBody CollectorRunRequest req) {
        if (req.type() == null || req.prefecture() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "type, prefecture required"));
        }
        long runId = runs.start(req.type(), req.prefecture(), "wikipedia");
        collector.runAsync(runId, req.type(), req.prefecture());
        return ResponseEntity.accepted().body(Map.of("runId", runId, "message", "수집 시작"));
    }

    @GetMapping("/collector/runs")
    public List<CollectorRunStatus> recent(@RequestParam(defaultValue = "20") int limit) {
        return runs.recent(limit);
    }

    @GetMapping("/collector/runs/{id}")
    public ResponseEntity<CollectorRunStatus> one(@PathVariable long id) {
        return runs.find(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // ── cache ─────────────────────────────────────────────────────────────
    @GetMapping("/cache/stats")
    public Map<String, Object> cacheStats() {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String name : cacheManager.getCacheNames()) {
            var c = cacheManager.getCache(name);
            if (c instanceof CaffeineCache cc) {
                CacheStats s = cc.getNativeCache().stats();
                out.put(name, Map.of(
                        "hitCount", s.hitCount(),
                        "missCount", s.missCount(),
                        "hitRate", s.hitRate(),
                        "evictionCount", s.evictionCount(),
                        "size", cc.getNativeCache().estimatedSize()
                ));
            }
        }
        return out;
    }

    @PostMapping("/cache/invalidate")
    public ResponseEntity<Map<String, Object>> invalidate(@RequestParam(required = false) String name) {
        if (name == null || name.isBlank()) {
            cacheManager.getCacheNames().forEach(n -> cacheManager.getCache(n).clear());
            return ResponseEntity.ok(Map.of("cleared", cacheManager.getCacheNames()));
        }
        var c = cacheManager.getCache(name);
        if (c == null) return ResponseEntity.notFound().build();
        c.clear();
        return ResponseEntity.ok(Map.of("cleared", List.of(name)));
    }
}
