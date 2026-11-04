package com.japantravel.news;

import com.fasterxml.jackson.databind.JsonNode;
import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//  뉴스 (D-075 · D-079). 스펙: docs/superpowers/specs/2026-11-04-news-api-design.md
class NewsApiTest extends IntegrationTest {

//  작성자가 지워져도 뉴스는 남으므로(ON DELETE SET NULL) 사용자 정리와 따로 비운다
    @BeforeEach
    void cleanNews() {
        jdbc.update("DELETE FROM news");
    }

    // ---- 조회 ----

    @Test
    void 목록은_로그인_없이_최신순이고_본문은_앞_100자_미리보기() throws Exception {
        long older = insertNews("첫 소식", "짧은 본문", "2026-11-01 10:00:00");
        long newer = insertNews("둘째 소식", "줄\n바꿈" + "가".repeat(120), "2026-11-02 10:00:00");

        JsonNode data = body(mockMvc.perform(get("/api/news")).andExpect(status().isOk())).get("data");

        assertThat(data.size()).isEqualTo(2);
        assertThat(data.get(0).get("id").asLong()).isEqualTo(newer);
        assertThat(data.get(1).get("id").asLong()).isEqualTo(older);
        String excerpt = data.get(0).get("excerpt").asText();
        assertThat(excerpt).startsWith("줄 바꿈").endsWith("…").hasSize(101);
        assertThat(data.get(1).get("excerpt").asText()).isEqualTo("짧은 본문");
        assertThat(data.get(0).has("body")).isFalse();
    }

    @Test
    void 상세는_로그인_없이_본문_전체와_사진_출처() throws Exception {
        long id = insertNews("소식", "본문\n둘째 줄", "2026-11-01 10:00:00");
        jdbc.update("UPDATE news SET image_path = 'https://x/a.jpg', image_credit = '작가 / CC BY 4.0' WHERE id = ?", id);

        mockMvc.perform(get("/api/news/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("소식"))
                .andExpect(jsonPath("$.data.body").value("본문\n둘째 줄"))
                .andExpect(jsonPath("$.data.imagePath").value("https://x/a.jpg"))
                .andExpect(jsonPath("$.data.imageCredit").value("작가 / CC BY 4.0"))
                .andExpect(jsonPath("$.data.authorNickname").doesNotExist());
    }

    @Test
    void 없는_뉴스는_404() throws Exception {
        expectError(mockMvc.perform(get("/api/news/{id}", MISSING_ID)), 404, "NEWS_NOT_FOUND");
    }

    // ---- 도우미 ----

    private long insertNews(String title, String body, String createdAt) {
        jdbc.update("INSERT INTO news (title, body, created_at) VALUES (?, ?, ?)", title, body, createdAt);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }
}
