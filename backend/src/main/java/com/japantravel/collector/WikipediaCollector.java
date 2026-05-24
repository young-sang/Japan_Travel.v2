package com.japantravel.collector;

import com.japantravel.client.NominatimClient;
import com.japantravel.client.WikipediaClient;
import com.japantravel.repository.CollectorRunRepository;
import com.japantravel.repository.DestinationRepository;
import com.japantravel.repository.FestivalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Wikipedia 카테고리 → 페이지 summary → Nominatim 좌표 보강 → UPSERT 파이프라인.
 * type ∈ {"destination", "festival"}, prefecture는 한국어("도쿄도", "교토부" 등).
 */
@Service
public class WikipediaCollector {
    private static final Logger log = LoggerFactory.getLogger(WikipediaCollector.class);

    private final WikipediaClient wiki;
    private final NominatimClient nominatim;
    private final TagInferrer inferrer;
    private final DestinationRepository destRepo;
    private final FestivalRepository festRepo;
    private final CollectorRunRepository runRepo;

    public WikipediaCollector(WikipediaClient wiki, NominatimClient nominatim, TagInferrer inferrer,
                              DestinationRepository destRepo, FestivalRepository festRepo,
                              CollectorRunRepository runRepo) {
        this.wiki = wiki; this.nominatim = nominatim; this.inferrer = inferrer;
        this.destRepo = destRepo; this.festRepo = festRepo; this.runRepo = runRepo;
    }

    /** 동기 호출자가 먼저 run row를 생성하고 id를 받은 뒤 호출. 비동기 처리. */
    @Async
    public void runAsync(long runId, String type, String prefecture) {
        process(runId, type, prefecture);
    }

    /** 부팅 시 동기 수집 — 새 run 생성 후 즉시 처리. */
    public long runBlocking(String type, String prefecture) {
        long runId = runRepo.start(type, prefecture, "wikipedia");
        process(runId, type, prefecture);
        return runId;
    }

    private void process(long runId, String type, String prefecture) {
        String category = prefecture + ("destination".equals(type) ? "의 관광지" : "의 축제");
        log.info("Collector start: run_id={} type={} prefecture={} category={}", runId, type, prefecture, category);

        List<String> titles = wiki.categoryMembers(category);
        log.info("  카테고리 페이지 {}개 발견", titles.size());

        int added = 0, updated = 0, failed = 0;
        for (String title : titles) {
            try {
                Optional<WikipediaClient.Summary> sumOpt = wiki.summary(title);
                if (sumOpt.isEmpty()) { failed++; runRepo.increment(runId, 0, 0, 1); continue; }
                WikipediaClient.Summary s = sumOpt.get();

                Double lat = s.lat(), lng = s.lng();
                if (lat == null || lng == null) {
                    var fallback = nominatim.search(title + " " + prefecture);
                    if (fallback.isPresent()) { lat = fallback.get().lat(); lng = fallback.get().lng(); }
                }

                boolean isNew;
                if ("destination".equals(type)) {
                    var tags = inferrer.inferDestinationTags(title, s.extract());
                    isNew = destRepo.upsertByWiki(
                            title.replace('_', ' '), prefecture, tags, lat, lng,
                            s.thumbnailUrl(), s.extract(), title);
                } else {
                    Integer month = inferrer.inferMonth(s.extract());
                    isNew = festRepo.upsertByWiki(
                            title.replace('_', ' '), prefecture, month, null,
                            lat, lng, s.thumbnailUrl(), s.extract(), title);
                }
                if (isNew) added++; else updated++;
                runRepo.increment(runId, isNew ? 1 : 0, isNew ? 0 : 1, 0);
            } catch (Exception e) {
                failed++;
                runRepo.increment(runId, 0, 0, 1);
                log.warn("  항목 처리 실패: {} → {}", title, e.getMessage());
            }
        }
        String status = titles.isEmpty() ? "failed"
                : failed == titles.size() ? "failed"
                : failed > 0 ? "partial" : "success";
        runRepo.finish(runId, status, null);
        log.info("Collector done: run_id={} added={} updated={} failed={}", runId, added, updated, failed);
    }
}
