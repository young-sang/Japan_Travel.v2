package com.japantravel.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.japantravel.collector.PrefectureCatalog;
import com.japantravel.collector.WikipediaCollector;
import com.japantravel.collector.WikipediaCollector.TypeAndPrefecture;
import com.japantravel.dto.Dtos.BulkRun;
import com.japantravel.dto.Dtos.BulkRunDetail;
import com.japantravel.dto.Dtos.BulkRunSummary;
import com.japantravel.dto.Dtos.CollectorRunDetail;
import com.japantravel.dto.Dtos.CollectorRunRequest;
import com.japantravel.dto.Dtos.CollectorRunStatus;
import com.japantravel.dto.Dtos.Destination;
import com.japantravel.dto.Dtos.FailureEntry;
import com.japantravel.dto.Dtos.Festival;
import com.japantravel.dto.Dtos.User;
import com.japantravel.dto.Dtos.Post;
import com.japantravel.dto.Dtos.PostPage;
import com.japantravel.repository.*;
import com.japantravel.security.CurrentUser;
import com.japantravel.service.AuditService;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final WikipediaCollector collector;
    private final CollectorRunRepository runs;
    private final BulkRunRepository bulkRuns;
    private final DestinationRepository destRepo;
    private final FestivalRepository festRepo;
    private final CourseRepository courseRepo;
    private final UserDataRepository userData;
    private final UserRepository userRepo;
    private final PostRepository postRepo;
    private final AuditLogRepository auditRepo;
    private final AuditService auditService;
    private final CurrentUser currentUser;
    private final CacheManager cacheManager;
    private final ObjectMapper mapper;

    public AdminController(WikipediaCollector collector, CollectorRunRepository runs,
                           BulkRunRepository bulkRuns,
                           DestinationRepository d, FestivalRepository f, CourseRepository c,
                           UserDataRepository ud, UserRepository ur,
                           AuditLogRepository al, AuditService as,
                           CurrentUser cu, CacheManager cm, ObjectMapper mapper,
                           PostRepository pr) {
        this.collector = collector; this.runs = runs; this.bulkRuns = bulkRuns;
        this.destRepo = d; this.festRepo = f; this.courseRepo = c; this.userData = ud;
        this.userRepo = ur; this.auditRepo = al; this.auditService = as;
        this.currentUser = cu; this.cacheManager = cm; this.mapper = mapper;
        this.postRepo = pr;
    }

    // ── posts (admin) ─────────────────────────────────────────────────────
    @GetMapping("/posts")
    public PostPage adminListPosts(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        List<Post> items = postRepo.listPaged(page * size, size);
        return new PostPage(items, postRepo.count(), page, size);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> adminDeletePost(@PathVariable long id) {
        if (!postRepo.deleteByAdmin(id)) return ResponseEntity.notFound().build();
        auditService.log(currentUser, "POST_DELETE", "post", id, null);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> adminDeleteComment(@PathVariable long id) {
        if (!postRepo.deleteCommentByAdmin(id)) return ResponseEntity.notFound().build();
        auditService.log(currentUser, "POST_COMMENT_DELETE", "post_comment", id, null);
        return ResponseEntity.noContent().build();
    }

    // ── stats ─────────────────────────────────────────────────────────────
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of(
                "destinations", destRepo.count(),
                "festivals", festRepo.count(),
                "courses", courseRepo.count(),
                "favorites", userData.countAllFavorites(),
                "reviews", userData.allReviews().size(),
                "prefecturesCovered", destRepo.distinctPrefectures().size()
        );
    }

    // ── collection matrix ─────────────────────────────────────────────────
    @GetMapping("/collection-matrix")
    public Map<String, Object> collectionMatrix() {
        Map<String, Integer> dest = destRepo.countsByPrefecture();
        Map<String, Integer> fest = festRepo.countsByPrefecture();
        Map<String, String> destAt = destRepo.maxRefreshedByPrefecture();
        Map<String, String> festAt = festRepo.maxRefreshedByPrefecture();
        Map<String, Map<String, Object>> rows = new LinkedHashMap<>();
        for (String p : PrefectureCatalog.ALL) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("destination", dest.getOrDefault(p, 0));
            row.put("festival", fest.getOrDefault(p, 0));
            row.put("destinationAt", destAt.get(p));
            row.put("festivalAt", festAt.get(p));
            rows.put(p, row);
        }
        return Map.of("prefectures", PrefectureCatalog.ALL, "rows", rows);
    }

    // ── collector ─────────────────────────────────────────────────────────
    @PostMapping("/collector/run")
    public ResponseEntity<Map<String, Object>> run(@RequestBody CollectorRunRequest req) {
        if (req.type() == null || req.prefecture() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "type, prefecture required"));
        }
        var existingBulk = bulkRuns.findRunning();
        if (existingBulk.isPresent() && (existingBulk.get().totalTasks() != null && existingBulk.get().totalTasks() > 1)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "code", "BULK_ALREADY_RUNNING",
                    "bulkRunId", existingBulk.get().id(),
                    "message", "대규모 수집이 진행 중입니다. 끝난 뒤 다시 시도하세요."));
        }
        long bulkRunId = collector.runBulkAsyncPairs(
                List.of(new TypeAndPrefecture(req.type(), req.prefecture())));
        Long runId = runs.findByBulkRunId(bulkRunId).stream()
                .findFirst().map(CollectorRunStatus::id).orElse(null);
        auditService.log(currentUser, "COLLECTOR_RUN", "collector", null,
                "type=" + req.type() + ",pref=" + req.prefecture());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("runId", runId);
        body.put("bulkRunId", bulkRunId);
        body.put("message", "수집 시작");
        return ResponseEntity.accepted().body(body);
    }

    @PostMapping("/collector/bulk")
    public ResponseEntity<Map<String, Object>> runBulk() {
        var existing = bulkRuns.findRunning();
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "code", "BULK_ALREADY_RUNNING",
                    "bulkRunId", existing.get().id()
            ));
        }
        int total = PrefectureCatalog.ALL.size() * PrefectureCatalog.TYPES.size();
        long bulkRunId = collector.runBulkAsync(PrefectureCatalog.ALL, PrefectureCatalog.TYPES);
        auditService.log(currentUser, "COLLECTOR_BULK", "bulk_run", bulkRunId, "queued=" + total);
        return ResponseEntity.accepted().body(Map.of("bulkRunId", bulkRunId, "queued", total));
    }

    @GetMapping("/collector/runs")
    public List<CollectorRunStatus> recent(@RequestParam(defaultValue = "20") int limit) {
        return runs.recent(limit);
    }

    @GetMapping("/collector/runs/{id}")
    public ResponseEntity<CollectorRunStatus> one(@PathVariable long id) {
        return runs.find(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // ── bulk runs ─────────────────────────────────────────────────────────
    @GetMapping("/collector/bulk-runs")
    public List<BulkRunSummary> bulkRunsList(@RequestParam(defaultValue = "20") int limit) {
        if (limit <= 0) limit = 20;
        if (limit > 100) limit = 100;
        return bulkRuns.recent(limit).stream().map(AdminController::toSummary).toList();
    }

    @GetMapping("/collector/bulk-runs/{id}")
    public ResponseEntity<BulkRunDetail> bulkRunDetail(@PathVariable long id) {
        var found = bulkRuns.findById(id);
        if (found.isEmpty()) return ResponseEntity.notFound().build();
        BulkRunSummary summary = toSummary(found.get());
        List<CollectorRunDetail> details = new ArrayList<>();
        try {
            List<CollectorRunStatus> children = runs.findByBulkRunId(id);
            for (CollectorRunStatus r : children) {
                details.add(toDetail(r));
            }
        } catch (Exception e) {
            // children 조회 실패 시(예: 스키마 드리프트) summary만으로 부분 응답
        }
        return ResponseEntity.ok(new BulkRunDetail(summary, details));
    }

    @PostMapping("/collector/runs/{id}/retry")
    public ResponseEntity<Map<String, Object>> retryRun(@PathVariable long id) {
        var origin = runs.find(id).orElse(null);
        if (origin == null) return ResponseEntity.notFound().build();
        if (!isFinished(origin.status())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", "RUN_NOT_FINISHED"));
        }
        Long parent = origin.bulkRunId();
        long newRunId = runs.start(origin.type(), origin.prefecture(), "wikipedia-retry", parent);
        collector.runAsync(newRunId, origin.type(), origin.prefecture());
        auditService.log(currentUser, "COLLECTOR_RUN_RETRY", "collector_run", newRunId,
                "origin=" + id + ",type=" + origin.type() + ",pref=" + origin.prefecture());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("runId", newRunId);
        body.put("parentBulkRunId", parent);
        return ResponseEntity.accepted().body(body);
    }

    @PostMapping("/collector/bulk-runs/{id}/retry-failed")
    public ResponseEntity<Map<String, Object>> retryFailed(@PathVariable long id) {
        var origin = bulkRuns.findById(id).orElse(null);
        if (origin == null) return ResponseEntity.notFound().build();
        if ("running".equals(origin.status())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "code", "BULK_ALREADY_RUNNING",
                    "bulkRunId", origin.id()
            ));
        }
        List<CollectorRunStatus> children = runs.findByBulkRunId(id);
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<TypeAndPrefecture> targets = new ArrayList<>();
        for (CollectorRunStatus c : children) {
            String s = c.status();
            if ("failed".equals(s) || "partial".equals(s) || "aborted".equals(s) || "empty".equals(s)) {
                String key = c.type() + "|" + c.prefecture();
                if (seen.add(key)) {
                    targets.add(new TypeAndPrefecture(c.type(), c.prefecture()));
                }
            }
        }
        if (targets.isEmpty()) {
            return ResponseEntity.ok(Map.of("retried", 0, "message", "no failed children"));
        }
        var running = bulkRuns.findRunning();
        if (running.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "code", "BULK_ALREADY_RUNNING",
                    "bulkRunId", running.get().id()
            ));
        }
        long newBulkRunId = collector.runBulkAsyncPairs(targets);
        auditService.log(currentUser, "COLLECTOR_BULK_RETRY", "bulk_run", newBulkRunId,
                "origin=" + id + ",queued=" + targets.size());
        return ResponseEntity.accepted().body(Map.of(
                "bulkRunId", newBulkRunId,
                "queued", targets.size()
        ));
    }

    // ── helpers ───────────────────────────────────────────────────────────
    private static boolean isFinished(String status) {
        return "success".equals(status) || "partial".equals(status) || "failed".equals(status)
                || "aborted".equals(status) || "empty".equals(status);
    }

    private static BulkRunSummary toSummary(BulkRun b) {
        return new BulkRunSummary(
                b.id() == null ? 0L : b.id(),
                b.startedAt(), b.finishedAt(), b.status(),
                b.totalTasks() == null ? 0 : b.totalTasks(),
                b.tasksSuccess() == null ? 0 : b.tasksSuccess(),
                b.tasksPartial() == null ? 0 : b.tasksPartial(),
                b.tasksFailed() == null ? 0 : b.tasksFailed(),
                b.tasksAborted() == null ? 0 : b.tasksAborted()
        );
    }

    private CollectorRunDetail toDetail(CollectorRunStatus r) {
        String errorsJson = r.id() == null ? null : runs.findErrorsJson(r.id()).orElse(null);
        return new CollectorRunDetail(
                r.id() == null ? 0L : r.id(),
                r.type(), r.prefecture(), r.status(),
                r.itemsAdded() == null ? 0 : r.itemsAdded(),
                r.itemsUpdated() == null ? 0 : r.itemsUpdated(),
                r.itemsFailed() == null ? 0 : r.itemsFailed(),
                r.stage(), r.lastHeartbeatAt(),
                parseFailures(errorsJson),
                parseAbortReason(errorsJson)
        );
    }

    private List<FailureEntry> parseFailures(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode arr = root.get("failures");
            if (arr == null || !arr.isArray()) return List.of();
            return mapper.convertValue(arr, new TypeReference<List<FailureEntry>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private String parseAbortReason(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode n = root.get("abort_reason");
            return n == null || n.isNull() ? null : n.asText();
        } catch (Exception e) {
            return null;
        }
    }

    // ── content CRUD ──────────────────────────────────────────────────────
    @PostMapping("/destinations")
    public ResponseEntity<Map<String, Object>> createDestination(@RequestBody Destination d) {
        long id = destRepo.insert(d);
        auditService.log(currentUser, "CONTENT_CREATE", "destination", id, "name=" + d.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @PutMapping("/destinations/{id}")
    public ResponseEntity<Void> updateDestination(@PathVariable long id, @RequestBody Destination d) {
        if (destRepo.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        destRepo.update(id, d);
        auditService.log(currentUser, "CONTENT_UPDATE", "destination", id, "name=" + d.name());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/destinations/{id}")
    public ResponseEntity<Void> deleteDestination(@PathVariable long id) {
        destRepo.deleteById(id);
        auditService.log(currentUser, "CONTENT_DELETE", "destination", id, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/festivals")
    public ResponseEntity<Map<String, Object>> createFestival(@RequestBody Festival f) {
        long id = festRepo.insert(f);
        auditService.log(currentUser, "CONTENT_CREATE", "festival", id, "name=" + f.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @PutMapping("/festivals/{id}")
    public ResponseEntity<Void> updateFestival(@PathVariable long id, @RequestBody Festival f) {
        if (festRepo.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        festRepo.update(id, f);
        auditService.log(currentUser, "CONTENT_UPDATE", "festival", id, "name=" + f.name());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/festivals/{id}")
    public ResponseEntity<Void> deleteFestival(@PathVariable long id) {
        festRepo.deleteById(id);
        auditService.log(currentUser, "CONTENT_DELETE", "festival", id, null);
        return ResponseEntity.noContent().build();
    }

    // ── user management ───────────────────────────────────────────────────
    @GetMapping("/users")
    public List<Map<String, Object>> listUsers() {
        List<Map<String, Object>> rows = userRepo.listAll();
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (Map<String, Object> r : rows) {
            long id = ((Number) r.get("id")).longValue();
            Map<String, Object> m = new LinkedHashMap<>(r);
            m.put("favCount", userData.countFavoritesByUser(id));
            m.put("reviewCount", userData.countReviewsByUser(id));
            m.put("courseCount", userData.countCoursesByUser(id));
            m.put("historyCount", userData.countHistoryByUser(id));
            out.add(m);
        }
        return out;
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<Void> setUserRole(@PathVariable long id, @RequestBody Map<String, String> body) {
        String newRole = body == null ? null : body.get("role");
        if (!"USER".equals(newRole) && !"ADMIN".equals(newRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role은 USER 또는 ADMIN");
        }
        if (currentUser.userId() == id) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "본인 역할은 변경할 수 없습니다");
        }
        User target = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String oldRole = target.role();
        if (oldRole.equals(newRole)) return ResponseEntity.noContent().build();
        if ("ADMIN".equals(oldRole) && "USER".equals(newRole) && userRepo.countByRole("ADMIN") <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "마지막 ADMIN은 강등할 수 없습니다");
        }
        userRepo.updateRole(id, newRole);
        auditService.log(currentUser, "USER_ROLE_CHANGE", "user", id,
                "from=" + oldRole + ",to=" + newRole + ",username=" + target.username());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        if (currentUser.userId() == id) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "본인 계정은 삭제할 수 없습니다");
        }
        User target = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if ("ADMIN".equals(target.role()) && userRepo.countByRole("ADMIN") <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "마지막 ADMIN은 삭제할 수 없습니다");
        }
        userRepo.deleteByIdCascade(id);
        auditService.log(currentUser, "USER_DELETE", "user", id, "username=" + target.username());
        return ResponseEntity.noContent().build();
    }

    // ── audit log ─────────────────────────────────────────────────────────
    @GetMapping("/audit")
    public Map<String, Object> listAudit(@RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) String action,
                                         @RequestParam(required = false) String from,
                                         @RequestParam(required = false) String to,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        if (size <= 0 || size > 500) size = 50;
        if (page < 0) page = 0;
        int offset = page * size;
        List<Map<String, Object>> items = auditRepo.listFiltered(userId, action, from, to, size, offset);
        int total = auditRepo.count(userId, action, from, to);
        return Map.of("items", items, "total", total);
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
