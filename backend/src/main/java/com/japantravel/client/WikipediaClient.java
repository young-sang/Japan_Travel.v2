package com.japantravel.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class WikipediaClient {
    private static final Logger log = LoggerFactory.getLogger(WikipediaClient.class);

    private final WebClient web;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;
    private final String userAgent;

    /** REST summary 호출 throttle. 200ms 간격으로 직렬화. 429 회피용. */
    private final ReentrantLock summaryLock = new ReentrantLock();
    private long lastSummaryAt = 0;
    private static final long SUMMARY_INTERVAL_MS = 250;

    public WikipediaClient(WebClient.Builder builder,
                           @Value("${collector.wikipedia.base-url}") String baseUrl,
                           @Value("${collector.user-agent}") String userAgent) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
        this.web = builder.baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    /** 한국어 위키 카테고리(예: "도쿄도의 관광지")의 페이지 제목 목록. */
    @Cacheable(cacheNames = "wikiCategoryMembers", key = "#categoryName")
    public List<String> categoryMembers(String categoryName) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/w/api.php")
                .queryParam("action", "query")
                .queryParam("list", "categorymembers")
                .queryParam("cmtitle", "분류:" + categoryName)
                .queryParam("cmlimit", 50)
                .queryParam("cmtype", "page")
                .queryParam("format", "json")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        try {
            log.debug("Wikipedia categoryMembers URI: {}", uri);
            String body = web.get().uri(uri).retrieve().bodyToMono(String.class).block();
            JsonNode root = mapper.readTree(body);
            JsonNode members = root.path("query").path("categorymembers");
            List<String> titles = new ArrayList<>();
            for (JsonNode m : members) {
                String title = m.path("title").asText(null);
                if (title != null && !title.startsWith("분류:")) {
                    titles.add(title);
                }
            }
            return titles;
        } catch (Exception e) {
            log.warn("Wikipedia categoryMembers 실패: {} → {}", categoryName, e.getMessage());
            return List.of();
        }
    }

    /** 페이지 요약: extract, thumbnail, coordinates. 429 대비 throttle + 1회 재시도. */
    @Cacheable(cacheNames = "wikiSummary", key = "#title", unless = "#result == null")
    public Optional<Summary> summary(String title) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/rest_v1/page/summary/" + title.replace(' ', '_'))
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        for (int attempt = 0; attempt < 2; attempt++) {
            throttleSummary();
            try {
                String body = web.get().uri(uri).retrieve().bodyToMono(String.class).block();
                return parseSummary(title, body);
            } catch (org.springframework.web.reactive.function.client.WebClientResponseException.TooManyRequests e) {
                log.debug("Wikipedia 429 — 1.5s 후 재시도: {}", title);
                try { Thread.sleep(1500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            } catch (Exception e) {
                log.debug("Wikipedia summary 실패: {} → {}", title, e.getMessage());
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private void throttleSummary() {
        summaryLock.lock();
        try {
            long wait = (lastSummaryAt + SUMMARY_INTERVAL_MS) - System.currentTimeMillis();
            if (wait > 0) {
                try { Thread.sleep(wait); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
            lastSummaryAt = System.currentTimeMillis();
        } finally {
            summaryLock.unlock();
        }
    }

    private Optional<Summary> parseSummary(String title, String body) {
        try {
            JsonNode root = mapper.readTree(body);
            if (root.has("type") && "disambiguation".equals(root.path("type").asText())) {
                return Optional.empty();
            }
            String extract = root.path("extract").asText(null);
            String thumb = root.path("thumbnail").path("source").asText(null);
            Double lat = root.path("coordinates").path("lat").isMissingNode() ? null
                    : root.path("coordinates").path("lat").asDouble();
            Double lng = root.path("coordinates").path("lon").isMissingNode() ? null
                    : root.path("coordinates").path("lon").asDouble();
            String pageUrl = root.path("content_urls").path("desktop").path("page").asText(null);
            return Optional.of(new Summary(title, extract, thumb, lat, lng, pageUrl));
        } catch (Exception e) {
            log.debug("Wikipedia summary 실패: {} → {}", title, e.getMessage());
            return Optional.empty();
        }
    }

    public record Summary(String title, String extract, String thumbnailUrl,
                          Double lat, Double lng, String pageUrl) {}
}
