package com.japantravel.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.japantravel.client.FetchResult;
import com.japantravel.client.NominatimClient;
import com.japantravel.client.NominatimClient.LatLng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 일회성 좌표 백필. 부팅 인자 --app.task=backfill-coords 가 있을 때만 동작.
 *
 * 전략:
 *  1) wiki_title (ko) → ko.wikipedia langlinks(ja) → 일본어 제목으로 Nominatim 검색.
 *  2) 실패 시 한국어 name + prefecture 로 Nominatim 재시도.
 *  3) 실패 시 Kakao 로컬 키워드 검색 (KAKAO_REST_KEY 환경변수가 있을 때만, 일본 bbox 필터링).
 */
@Component
public class BackfillCoordsRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(BackfillCoordsRunner.class);
    private static final String TRIGGER = "--app.task=backfill-coords";

    // 일본 bbox (대략): lat 24~46, lng 122~146
    private static final double JP_LAT_MIN = 24.0, JP_LAT_MAX = 46.0;
    private static final double JP_LNG_MIN = 122.0, JP_LNG_MAX = 146.0;

    private final JdbcTemplate jdbc;
    private final NominatimClient nominatim;
    private final ConfigurableApplicationContext ctx;
    private final WebClient ko;
    private final WebClient kakao;
    private final String kakaoKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private long lastWikiCallAt = 0;
    private static final long WIKI_INTERVAL_MS = 1100;

    public BackfillCoordsRunner(JdbcTemplate jdbc,
                                NominatimClient nominatim,
                                ConfigurableApplicationContext ctx,
                                WebClient.Builder builder,
                                @Value("${collector.wikipedia.base-url}") String koBase,
                                @Value("${collector.user-agent}") String userAgent,
                                @Value("${collector.kakao.rest-key:}") String kakaoKey) {
        this.jdbc = jdbc;
        this.nominatim = nominatim;
        this.ctx = ctx;
        this.kakaoKey = kakaoKey;
        this.ko = builder.baseUrl(koBase)
                .defaultHeader("User-Agent", userAgent)
                .build();
        this.kakao = builder.baseUrl("https://dapi.kakao.com")
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    @Override
    public void run(String... args) {
        boolean triggered = false;
        for (String a : args) if (TRIGGER.equals(a)) { triggered = true; break; }
        if (!triggered) return;

        log.info("=== backfill-coords 시작 (kakao={}) ===", kakaoKey == null || kakaoKey.isBlank() ? "off" : "on");
        Summary dest = process("destinations");
        Summary fest = process("festivals");

        log.info("=== backfill-coords 완료 ===");
        log.info("  destinations  대상={}  성공={}  실패={}", dest.total, dest.ok, dest.fail);
        log.info("  festivals     대상={}  성공={}  실패={}", fest.total, fest.ok, fest.fail);

        System.exit(SpringApplication.exit(ctx, () -> 0));
    }

    private Summary process(String table) {
        List<Row> rows = jdbc.query(
                "SELECT id, name, prefecture, wiki_title FROM " + table + " WHERE lat IS NULL OR lng IS NULL",
                (rs, n) -> new Row(rs.getLong("id"), rs.getString("name"),
                        rs.getString("prefecture"), rs.getString("wiki_title")));
        int total = rows.size();
        int ok = 0, fail = 0;
        log.info("[{}] 대상 {}건", table, total);
        if (total == 0) return new Summary(0, 0, 0);

        String sql = "UPDATE " + table + " SET lat=?, lng=? WHERE id=?";
        List<String> failed = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            LatLng ll = lookup(r);
            if (ll != null) {
                jdbc.update(sql, ll.lat(), ll.lng(), r.id);
                ok++;
            } else {
                fail++;
                failed.add(r.id + ":" + r.name);
            }
            if ((i + 1) % 20 == 0) {
                log.info("[{}] 진행 {}/{}  성공={}  실패={}", table, i + 1, total, ok, fail);
            }
        }
        if (!failed.isEmpty()) {
            log.info("[{}] 실패 목록 ({}건): {}", table, failed.size(), failed);
        }
        return new Summary(total, ok, fail);
    }

    private LatLng lookup(Row r) {
        // 1) 일본어 제목 시도
        String jaTitle = (r.wikiTitle != null && !r.wikiTitle.isBlank())
                ? fetchJaTitle(r.wikiTitle) : null;
        if (jaTitle != null) {
            FetchResult<LatLng> res = nominatim.search(jaTitle);
            if (res.isSuccess()) {
                log.debug("hit(ja): id={} '{}' → {}", r.id, jaTitle, res.value());
                return res.value();
            }
        }
        // 2) 한국어 + 현 fallback (Nominatim)
        String q = (r.name + " " + r.prefecture).trim();
        FetchResult<LatLng> res = nominatim.search(q);
        if (res.isSuccess()) {
            log.debug("hit(ko): id={} '{}' → {}", r.id, q, res.value());
            return res.value();
        }
        // 3) Kakao 로컬 검색
        if (kakaoKey != null && !kakaoKey.isBlank()) {
            LatLng kk = searchKakao(r.name + " " + r.prefecture);
            if (kk != null) {
                log.debug("hit(kakao): id={} '{}' → {}", r.id, r.name, kk);
                return kk;
            }
            // 한 번 더: 이름만으로 (현 prefix 빼고)
            kk = searchKakao(r.name);
            if (kk != null) {
                log.debug("hit(kakao-name): id={} '{}' → {}", r.id, r.name, kk);
                return kk;
            }
        }
        return null;
    }

    private LatLng searchKakao(String query) {
        URI uri = UriComponentsBuilder.fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", query)
                .queryParam("size", 15)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        try {
            String body = kakao.get().uri(uri)
                    .header("Authorization", "KakaoAK " + kakaoKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(15));
            JsonNode docs = mapper.readTree(body).path("documents");
            if (!docs.isArray()) return null;
            for (JsonNode d : docs) {
                double lng = d.path("x").asDouble(0);  // x=경도, y=위도
                double lat = d.path("y").asDouble(0);
                if (lat >= JP_LAT_MIN && lat <= JP_LAT_MAX
                        && lng >= JP_LNG_MIN && lng <= JP_LNG_MAX) {
                    return new LatLng(lat, lng);
                }
            }
        } catch (Exception e) {
            log.debug("kakao 실패: {} → {}", query, e.getMessage());
        }
        return null;
    }

    private String fetchJaTitle(String koTitle) {
        URI uri = UriComponentsBuilder.fromHttpUrl("https://ko.wikipedia.org/w/api.php")
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("prop", "langlinks")
                .queryParam("lllang", "ja")
                .queryParam("titles", koTitle)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        for (int attempt = 0; attempt < 3; attempt++) {
            throttleWiki();
            try {
                String body = ko.get().uri(uri).retrieve().bodyToMono(String.class).block(Duration.ofSeconds(20));
                JsonNode pages = mapper.readTree(body).path("query").path("pages");
                if (pages.isObject()) {
                    var it = pages.fields();
                    while (it.hasNext()) {
                        JsonNode page = it.next().getValue();
                        JsonNode lls = page.path("langlinks");
                        if (lls.isArray() && lls.size() > 0) {
                            String ja = lls.get(0).path("*").asText(null);
                            if (ja != null && !ja.isBlank()) return ja;
                        }
                    }
                }
                return null;
            } catch (org.springframework.web.reactive.function.client.WebClientResponseException.TooManyRequests e) {
                log.debug("langlinks 429 — 3s 후 재시도: {}", koTitle);
                try { Thread.sleep(3000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            } catch (Exception e) {
                log.debug("langlinks 실패: {} → {}", koTitle, e.getMessage());
                return null;
            }
        }
        return null;
    }

    private synchronized void throttleWiki() {
        long wait = (lastWikiCallAt + WIKI_INTERVAL_MS) - System.currentTimeMillis();
        if (wait > 0) {
            try { Thread.sleep(wait); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
        lastWikiCallAt = System.currentTimeMillis();
    }

    private record Row(long id, String name, String prefecture, String wikiTitle) {}
    private record Summary(int total, int ok, int fail) {}
}
