package com.japantravel.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.japantravel.client.FetchResult;
import com.japantravel.client.NominatimClient;
import com.japantravel.client.WikipediaClient;
import com.japantravel.dto.Dtos.FailureEntry;
import com.japantravel.repository.BulkRunRepository;
import com.japantravel.repository.CollectorRunRepository;
import com.japantravel.repository.DestinationRepository;
import com.japantravel.repository.FestivalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wikipedia 카테고리 → 페이지 summary → Nominatim 좌표 보강 → UPSERT 파이프라인.
 * type ∈ {"destination", "festival"}, prefecture는 한국어("도쿄도", "교토부" 등).
 *
 * Bulk 실행은 bulk_runs 부모와 연동되며, child run마다 heartbeat/stage/errors_json 기록.
 * per-task timeout 5분, ZombieWatcher가 heartbeat 끊김 감지.
 */
@Service
public class WikipediaCollector {
    private static final Logger log = LoggerFactory.getLogger(WikipediaCollector.class);

    /** Bulk per-task timeout. */
    private static final long TASK_TIMEOUT_MIN = 5;
    /** Bulk 동시 처리 풀 크기. 1로 시작(외부 API rate limit 보호). */
    private static final int BULK_POOL_SIZE = 1;
    /** heartbeat 갱신 주기(페이지 처리 단위). */
    private static final int HEARTBEAT_EVERY = 5;

    private final WikipediaClient wiki;
    private final NominatimClient nominatim;
    private final TagInferrer inferrer;
    private final DestinationRepository destRepo;
    private final FestivalRepository festRepo;
    private final CollectorRunRepository runRepo;
    private final BulkRunRepository bulkRuns;
    private final ObjectMapper mapper;

    private final ExecutorService bulkExecutor;
    private final ScheduledExecutorService timeoutScheduler;

    public WikipediaCollector(WikipediaClient wiki, NominatimClient nominatim, TagInferrer inferrer,
                              DestinationRepository destRepo, FestivalRepository festRepo,
                              CollectorRunRepository runRepo, BulkRunRepository bulkRuns,
                              ObjectMapper mapper) {
        this.wiki = wiki; this.nominatim = nominatim; this.inferrer = inferrer;
        this.destRepo = destRepo; this.festRepo = festRepo; this.runRepo = runRepo;
        this.bulkRuns = bulkRuns; this.mapper = mapper;

        ThreadFactory tf = new ThreadFactory() {
            private final AtomicInteger seq = new AtomicInteger(1);
            @Override public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "bulk-collector-" + seq.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        };
        this.bulkExecutor = new ThreadPoolExecutor(
                BULK_POOL_SIZE, BULK_POOL_SIZE,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                tf);

        this.timeoutScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "bulk-timeout");
            t.setDaemon(true);
            return t;
        });
    }

    @PreDestroy
    public void shutdown() {
        try { timeoutScheduler.shutdownNow(); } catch (Throwable ignore) {}
        try { bulkExecutor.shutdownNow(); } catch (Throwable ignore) {}
    }

    /** 동기 호출자가 먼저 run row를 생성하고 id를 받은 뒤 호출. 비동기 처리. */
    @Async
    public void runAsync(long runId, String type, String prefecture) {
        processGuarded(runId, type, prefecture, null);
    }

    /** 부팅 시 동기 수집 — 새 run 생성 후 즉시 처리. */
    public long runBlocking(String type, String prefecture) {
        long runId = runRepo.start(type, prefecture, "wikipedia");
        processGuarded(runId, type, prefecture, null);
        return runId;
    }

    /** retry-failed 용: (type, prefecture) 쌍을 명시적으로 전달하는 입력 타입. */
    public record TypeAndPrefecture(String type, String prefecture) {}

    /**
     * prefecture × type 조합을 bulk_runs 부모와 묶어 실행.
     * 각 child는 per-task timeout 5분, 끝나면 부모 카운터 +1 후 finalizeIfDone.
     * @return 생성된 bulk_run_id
     */
    public long runBulkAsync(List<String> prefectures, List<String> types) {
        List<TypeAndPrefecture> pairs = new ArrayList<>(prefectures.size() * types.size());
        for (String pref : prefectures) {
            for (String type : types) {
                pairs.add(new TypeAndPrefecture(type, pref));
            }
        }
        return runBulkAsyncPairs(pairs);
    }

    /**
     * 임의의 (type, prefecture) 쌍 리스트로 bulk 실행. retry-failed 경로에서 사용.
     * @return 생성된 bulk_run_id
     */
    public long runBulkAsyncPairs(List<TypeAndPrefecture> pairs) {
        long bulkRunId = bulkRuns.create(pairs.size());

        for (TypeAndPrefecture p : pairs) {
            final String fType = p.type();
            final String fPref = p.prefecture();
            long runId = runRepo.start(fType, fPref, "wikipedia", bulkRunId);

            // 결과 단일화용 CF (정상 완료/예외/timeout 셋 다 여기로 합류).
            CompletableFuture<Void> cf = new CompletableFuture<>();

            // 실제 실행: bulkExecutor.submit() 으로 Future를 잡아야 cancel(true)이 스레드에 인터럽트를 전달.
            // (CompletableFuture.runAsync는 cancel해도 내부 task를 인터럽트하지 않음 — 원래 버그의 본질.)
            Future<?> taskFuture = bulkExecutor.submit(() -> {
                try {
                    processGuarded(runId, fType, fPref, bulkRunId);
                    cf.complete(null);
                } catch (Throwable t) {
                    cf.completeExceptionally(t);
                }
            });

            // per-task timeout watchdog: 5분 내 미완료면 실제 스레드를 인터럽트.
            ScheduledFuture<?> killer = timeoutScheduler.schedule(() -> {
                if (!taskFuture.isDone()) {
                    log.warn("bulk task timeout: run_id={} type={} prefecture={} → interrupting", runId, fType, fPref);
                    taskFuture.cancel(true); // mayInterruptIfRunning=true → 워커 스레드에 interrupt
                    // 안전망: processGuarded의 finally가 인터럽트 후에도 finishIfNotDone을 호출하지만,
                    // WebClient.block 도중 인터럽트 시 catch(Throwable)로 빠지므로 finally는 결국 도달.
                    // 그래도 인터럽트가 전파 안 되는 극단 경우를 대비해 여기서도 멱등 finish.
                    String errJson = buildErrorsJson(List.of(), "timeout");
                    runRepo.finishIfNotDone(runId, "aborted", errJson);
                    // cf를 강제 완료시켜 후속 카운터/finalize가 즉시 진행되도록.
                    cf.complete(null);
                }
            }, TASK_TIMEOUT_MIN, TimeUnit.MINUTES);

            // 단일 합류점: 정상 종료든 timeout 후 강제 complete든 한 번만 부모 카운터 증분.
            cf.whenComplete((v, err) -> {
                killer.cancel(false); // 정상 완료라면 watchdog 취소
                try {
                    var snap = runRepo.find(runId).orElse(null);
                    String finalStatus = snap != null ? snap.status() : "aborted";
                    bulkRuns.incrementChildResult(bulkRunId, finalStatus);
                    bulkRuns.finalizeIfDone(bulkRunId);
                } catch (Throwable t) {
                    log.warn("bulk parent finalize 실패: bulk_run_id={} → {}", bulkRunId, t.toString());
                }
            });
        }
        return bulkRunId;
    }

    /**
     * 처리 본체. 모든 종료 경로에서 finishIfNotDone 호출(멱등).
     * timeout watcher가 먼저 finish해도 이 finally는 0행 갱신이 되어 무해.
     */
    private void processGuarded(long runId, String type, String prefecture, Long bulkRunId) {
        List<FailureEntry> failures = new ArrayList<>();
        String abortReason = null;
        String finalStatus = "failed";
        int added = 0, updated = 0;
        boolean emptyCategory = false;

        try {
            runRepo.markRunning(runId);
            runRepo.setStage(runId, "category");

            String category = prefecture + ("destination".equals(type) ? "의 관광지" : "의 축제");
            log.info("Collector start: run_id={} type={} prefecture={} category={} bulk={}",
                    runId, type, prefecture, category, bulkRunId);

            FetchResult<List<String>> catRes = wiki.categoryMembers(category);
            if (!catRes.isSuccess()) {
                failures.add(new FailureEntry(category, "category",
                        catRes.exceptionClass(), catRes.message()));
                finalStatus = "failed";
                return;
            }
            List<String> titles = catRes.value();
            log.info("  카테고리 페이지 {}개 발견", titles.size());
            if (titles.isEmpty()) {
                emptyCategory = true;
                finalStatus = "empty";
                return;
            }

            int processedCount = 0;
            for (String title : titles) {
                // summary
                runRepo.setStage(runId, "summary");
                FetchResult<WikipediaClient.Summary> sumRes = wiki.summary(title);
                if (!sumRes.isSuccess()) {
                    if (sumRes.kind() != FetchResult.FailureKind.NOT_FOUND) {
                        failures.add(new FailureEntry(title, "summary",
                                sumRes.exceptionClass(), sumRes.message()));
                        runRepo.increment(runId, 0, 0, 1);
                    }
                    if (++processedCount % HEARTBEAT_EVERY == 0) runRepo.heartbeat(runId);
                    continue;
                }
                WikipediaClient.Summary s = sumRes.value();

                // nominatim fallback
                Double lat = s.lat(), lng = s.lng();
                if (lat == null || lng == null) {
                    runRepo.setStage(runId, "nominatim");
                    FetchResult<NominatimClient.LatLng> nRes = nominatim.search(title + " " + prefecture);
                    if (nRes.isSuccess()) {
                        lat = nRes.value().lat();
                        lng = nRes.value().lng();
                    } else if (nRes.kind() != FetchResult.FailureKind.NOT_FOUND) {
                        failures.add(new FailureEntry(title, "nominatim",
                                nRes.exceptionClass(), nRes.message()));
                        // 좌표 없이도 upsert는 계속 진행
                    }
                }

                // infer + upsert
                try {
                    runRepo.setStage(runId, "infer");
                    boolean isNew;
                    if ("destination".equals(type)) {
                        var tags = inferrer.inferDestinationTags(title, s.extract());
                        runRepo.setStage(runId, "upsert");
                        isNew = destRepo.upsertByWiki(
                                title.replace('_', ' '), prefecture, tags, lat, lng,
                                s.thumbnailUrl(), s.extract(), title);
                    } else {
                        Integer month = inferrer.inferMonth(s.extract());
                        runRepo.setStage(runId, "upsert");
                        isNew = festRepo.upsertByWiki(
                                title.replace('_', ' '), prefecture, month, null,
                                lat, lng, s.thumbnailUrl(), s.extract(), title);
                    }
                    if (isNew) added++; else updated++;
                    runRepo.increment(runId, isNew ? 1 : 0, isNew ? 0 : 1, 0);
                } catch (Exception ex) {
                    failures.add(new FailureEntry(title, "upsert",
                            ex.getClass().getSimpleName(), ex.getMessage()));
                    runRepo.increment(runId, 0, 0, 1);
                    log.warn("  항목 처리 실패: {} → {}", title, ex.getMessage());
                }

                if (++processedCount % HEARTBEAT_EVERY == 0) runRepo.heartbeat(runId);
            }

            int processed = added + updated;
            if (processed == 0) finalStatus = "failed";
            else if (!failures.isEmpty()) finalStatus = "partial";
            else finalStatus = "success";

        } catch (Throwable t) {
            // 인터럽트(타임아웃 watchdog 또는 풀 shutdown)에서 빠져나온 경우: aborted로 마킹.
            if (t instanceof InterruptedException || Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                abortReason = "interrupted";
                finalStatus = "aborted";
                log.warn("Collector item interrupted: run_id={}", runId);
            } else {
                abortReason = t.getClass().getSimpleName() + ":" + safeMsg(t.getMessage());
                finalStatus = "failed";
                log.warn("Collector item aborted: run_id={} → {}", runId, t.toString());
            }
        } finally {
            String errJson = (failures.isEmpty() && abortReason == null && !emptyCategory)
                    ? null
                    : buildErrorsJson(failures, abortReason);
            // 멱등: timeout watcher가 먼저 'aborted'로 finish했다면 0행, 그대로 OK.
            int updatedRows = runRepo.finishIfNotDone(runId, finalStatus, errJson);
            log.info("Collector done: run_id={} status={} added={} updated={} failures={} (finish rows={})",
                    runId, finalStatus, added, updated, failures.size(), updatedRows);
        }
    }

    private String buildErrorsJson(List<FailureEntry> failures, String abortReason) {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("failures", failures);
            root.put("abort_reason", abortReason);
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            return "{\"failures\":[],\"abort_reason\":\"json_serialize_failed\"}";
        }
    }

    private static String safeMsg(String msg) {
        if (msg == null) return "";
        return msg.length() > 200 ? msg.substring(0, 200) : msg;
    }
}
