package com.japantravel.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.timeout.ReadTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
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
    private static final long SUMMARY_INTERVAL_MS = 600;

    /** w/api.php (categoryMembers) throttle. */
    private final ReentrantLock apiLock = new ReentrantLock();
    private long lastApiAt = 0;
    private static final long API_INTERVAL_MS = 1100;

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
    @Cacheable(cacheNames = "wikiCategoryMembers", key = "#categoryName", unless = "!#result.isSuccess()")
    public FetchResult<List<String>> categoryMembers(String categoryName) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/w/api.php")
                .queryParam("action", "query")
                .queryParam("list", "categorymembers")
                .queryParam("cmtitle", "분류:" + categoryName)
                .queryParam("cmlimit", 500)
                .queryParam("cmtype", "page")
                .queryParam("format", "json")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        Exception lastError = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            throttleApi();
            try {
                log.debug("Wikipedia categoryMembers URI: {}", uri);
                String body = web.get().uri(uri).retrieve().bodyToMono(String.class).block(Duration.ofSeconds(20));
                JsonNode root = mapper.readTree(body);
                JsonNode members = root.path("query").path("categorymembers");
                List<String> titles = new ArrayList<>();
                for (JsonNode m : members) {
                    String title = m.path("title").asText(null);
                    if (title != null && !title.startsWith("분류:")) {
                        titles.add(title);
                    }
                }
                return FetchResult.success(titles);
            } catch (WebClientResponseException.TooManyRequests e) {
                lastError = e;
                log.debug("Wikipedia categoryMembers 429 — 1.5s 후 재시도: {}", categoryName);
                try { Thread.sleep(1500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            } catch (Exception e) {
                log.warn("Wikipedia categoryMembers 실패: {} → {}", categoryName, e.getMessage());
                return FetchResult.error(classify(e), e);
            }
        }
        return FetchResult.error(FetchResult.FailureKind.RATE_LIMITED,
                lastError != null ? lastError : new RuntimeException("rate limited"));
    }

    private void throttleApi() {
        apiLock.lock();
        try {
            long wait = (lastApiAt + API_INTERVAL_MS) - System.currentTimeMillis();
            if (wait > 0) {
                try { Thread.sleep(wait); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
            lastApiAt = System.currentTimeMillis();
        } finally {
            apiLock.unlock();
        }
    }

    /** 페이지 요약: extract, thumbnail, coordinates. 429 대비 throttle + 1회 재시도. */
    @Cacheable(cacheNames = "wikiSummary", key = "#title", unless = "!#result.isSuccess()")
    public FetchResult<Summary> summary(String title) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/rest_v1/page/summary/" + title.replace(' ', '_'))
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        Exception lastError = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            throttleSummary();
            try {
                String body = web.get().uri(uri).retrieve().bodyToMono(String.class).block(Duration.ofSeconds(20));
                return parseSummary(title, body);
            } catch (WebClientResponseException.TooManyRequests e) {
                lastError = e;
                log.debug("Wikipedia 429 — 1.5s 후 재시도: {}", title);
                try { Thread.sleep(1500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            } catch (WebClientResponseException.NotFound e) {
                log.debug("Wikipedia summary 404: {}", title);
                return FetchResult.notFound();
            } catch (Exception e) {
                log.debug("Wikipedia summary 실패: {} → {}", title, e.getMessage());
                return FetchResult.error(classify(e), e);
            }
        }
        // 429 재시도 후에도 실패
        return FetchResult.error(FetchResult.FailureKind.RATE_LIMITED,
                lastError != null ? lastError : new RuntimeException("rate limited"));
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

    private FetchResult<Summary> parseSummary(String title, String body) {
        try {
            JsonNode root = mapper.readTree(body);
            if (root.has("type") && "disambiguation".equals(root.path("type").asText())) {
                return FetchResult.notFound();
            }
            String extract = root.path("extract").asText(null);
            String thumb = root.path("thumbnail").path("source").asText(null);
            Double lat = root.path("coordinates").path("lat").isMissingNode() ? null
                    : root.path("coordinates").path("lat").asDouble();
            Double lng = root.path("coordinates").path("lon").isMissingNode() ? null
                    : root.path("coordinates").path("lon").asDouble();
            String pageUrl = root.path("content_urls").path("desktop").path("page").asText(null);
            return FetchResult.success(new Summary(title, extract, thumb, lat, lng, pageUrl));
        } catch (Exception e) {
            log.debug("Wikipedia summary 실패: {} → {}", title, e.getMessage());
            return FetchResult.error(FetchResult.FailureKind.PARSE, e);
        }
    }

    /** Throwable → FailureKind 매핑. */
    static FetchResult.FailureKind classify(Throwable e) {
        // Unwrap reactor-wrapped exceptions
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof WebClientResponseException.TooManyRequests) return FetchResult.FailureKind.RATE_LIMITED;
            if (cause instanceof WebClientResponseException w) {
                int sc = w.getStatusCode().value();
                if (sc == 404) return FetchResult.FailureKind.NOT_FOUND;
                return FetchResult.FailureKind.HTTP_ERROR;
            }
            if (cause instanceof ReadTimeoutException) return FetchResult.FailureKind.TIMEOUT;
            if (cause instanceof TimeoutException) return FetchResult.FailureKind.TIMEOUT;
            if (cause instanceof ConnectException) return FetchResult.FailureKind.NETWORK;
            if (cause instanceof WebClientRequestException) return FetchResult.FailureKind.NETWORK;
            if (cause instanceof IOException) return FetchResult.FailureKind.NETWORK;
            if (cause instanceof JsonProcessingException) return FetchResult.FailureKind.PARSE;
            Throwable next = cause.getCause();
            if (next == cause) break;
            cause = next;
        }
        // Block timeout from Mono#block(Duration) — IllegalStateException with "Timeout on blocking read"
        String msg = e.getMessage();
        if (msg != null && msg.toLowerCase().contains("timeout")) return FetchResult.FailureKind.TIMEOUT;
        return FetchResult.FailureKind.HTTP_ERROR;
    }

    public record Summary(String title, String extract, String thumbnailUrl,
                          Double lat, Double lng, String pageUrl) {}
}
