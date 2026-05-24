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
import java.time.Duration;
import java.util.concurrent.locks.ReentrantLock;

/** OpenStreetMap Nominatim. Usage Policy: 1 req/sec, User-Agent 필수. */
@Component
public class NominatimClient {
    private static final Logger log = LoggerFactory.getLogger(NominatimClient.class);

    private final WebClient web;
    private final ObjectMapper mapper = new ObjectMapper();
    private final long rateLimitMs;
    private final String baseUrl;
    private final ReentrantLock lock = new ReentrantLock();
    private long lastRequestAt = 0;

    public NominatimClient(WebClient.Builder builder,
                           @Value("${collector.nominatim.base-url}") String baseUrl,
                           @Value("${collector.nominatim.rate-limit-ms}") long rateLimitMs,
                           @Value("${collector.user-agent}") String userAgent) {
        this.rateLimitMs = rateLimitMs;
        this.baseUrl = baseUrl;
        this.web = builder.baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent)
                .defaultHeader("Accept-Language", "ko")
                .build();
    }

    /** 자유 텍스트 검색 → 첫 좌표 반환. */
    @Cacheable(cacheNames = "nominatimSearch", key = "#query", unless = "!#result.isSuccess()")
    public FetchResult<LatLng> search(String query) {
        throttle();
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                .queryParam("q", query)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .queryParam("countrycodes", "jp")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        try {
            String body = web.get().uri(uri).retrieve().bodyToMono(String.class).block(Duration.ofSeconds(20));
            JsonNode arr = mapper.readTree(body);
            if (arr.isArray() && arr.size() > 0) {
                JsonNode first = arr.get(0);
                double lat = first.path("lat").asDouble();
                double lng = first.path("lon").asDouble();
                return FetchResult.success(new LatLng(lat, lng));
            }
            return FetchResult.notFound();
        } catch (Exception e) {
            log.debug("Nominatim search 실패: {} → {}", query, e.getMessage());
            return FetchResult.error(WikipediaClient.classify(e), e);
        }
    }

    private void throttle() {
        lock.lock();
        try {
            long now = System.currentTimeMillis();
            long wait = (lastRequestAt + rateLimitMs) - now;
            if (wait > 0) {
                try { Thread.sleep(wait); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
            lastRequestAt = System.currentTimeMillis();
        } finally {
            lock.unlock();
        }
    }

    public record LatLng(double lat, double lng) {}
}
