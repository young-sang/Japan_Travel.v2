package com.japantravel.collector;

import com.japantravel.repository.BulkRunRepository;
import com.japantravel.repository.CollectorRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * heartbeat가 끊긴 collector_runs를 'aborted'로 정리하고
 * 연관된 bulk_runs 부모 카운터를 증분/finalize.
 */
@Component
public class ZombieWatcher {
    private static final Logger log = LoggerFactory.getLogger(ZombieWatcher.class);
    /** 마지막 heartbeat가 이 초 이상 오래된 running run을 좀비로 간주. */
    private static final int STALE_THRESHOLD_SEC = 180;
    /** 좀비 finalize에 박는 errors_json. */
    private static final String HEARTBEAT_TIMEOUT_JSON =
            "{\"failures\":[],\"abort_reason\":\"heartbeat_timeout\"}";

    private final CollectorRunRepository runs;
    private final BulkRunRepository bulkRuns;

    public ZombieWatcher(CollectorRunRepository runs, BulkRunRepository bulkRuns) {
        this.runs = runs;
        this.bulkRuns = bulkRuns;
    }

    @Scheduled(fixedDelay = 30_000L)
    public void sweep() {
        var stale = runs.findStaleRunning(STALE_THRESHOLD_SEC);
        if (stale.isEmpty()) return;

        Set<Long> touchedBulks = new HashSet<>();
        for (var s : stale) {
            // 멱등: 이미 다른 경로(processGuarded finally / bulk timeout)가 finish했다면 0행.
            int rows = runs.finishIfNotDone(s.id(), "aborted", HEARTBEAT_TIMEOUT_JSON);
            if (rows == 0) continue;
            log.warn("ZombieWatcher: run_id={} type={} pref={} stage={} → aborted(heartbeat_timeout)",
                    s.id(), s.type(), s.prefecture(), s.stage());
            // bulk child의 부모 카운터는 CompletableFuture.whenComplete가 책임진다(중복 카운트 방지).
            // ZombieWatcher는 비-bulk(수동 단일 실행) 좀비만 정리하므로 부모 finalize는 불필요.
            if (s.bulkRunId() == null) {
                // 단일 실행: 부모 없음. 별도 카운터 처리 없음.
            } else {
                touchedBulks.add(s.bulkRunId());
            }
        }
        for (Long bid : touchedBulks) {
            try { bulkRuns.finalizeIfDone(bid); } catch (Throwable t) {
                log.warn("ZombieWatcher: finalize 실패 bulk_run_id={} → {}", bid, t.toString());
            }
        }
    }
}
