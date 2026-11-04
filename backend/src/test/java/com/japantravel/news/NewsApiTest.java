package com.japantravel.news;

import com.fasterxml.jackson.databind.JsonNode;
import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    // ---- 쓰기 · 권한 ----

    @Test
    void 토큰이_없으면_401_일반_사용자면_403() throws Exception {
        Map<String, Object> req = Map.of("title", "제목", "body", "본문");
        long id = insertNews("소식", "본문", "2026-11-01 10:00:00");
        String user = signup("tanaka");

        expectError(mockMvc.perform(withJson(post("/api/news"), req)), 401, "UNAUTHORIZED");
        expectError(mockMvc.perform(auth(withJson(post("/api/news"), req), user)), 403, "FORBIDDEN");
        expectError(mockMvc.perform(auth(withJson(put("/api/news/{id}", id), req), user)), 403, "FORBIDDEN");
        expectError(mockMvc.perform(auth(delete("/api/news/{id}", id), user)), 403, "FORBIDDEN");
    }

    @Test
    void 관리자가_쓰면_201_이고_목록_맨_위에_작성자와_함께_나온다() throws Exception {
        insertNews("예전 소식", "본문", "2026-11-01 10:00:00");
        String admin = admin("boss");

        JsonNode created = body(mockMvc.perform(auth(withJson(post("/api/news"),
                        Map.of("title", "새 소식", "body", "본문", "imagePath", "https://x/a.jpg", "imageCredit", "")), admin))
                .andExpect(status().isCreated())).get("data");

        assertThat(created.get("authorNickname").asText()).isEqualTo("boss-닉");
        assertThat(created.get("imagePath").asText()).isEqualTo("https://x/a.jpg");
//      빈 문자열은 null 로 저장한다
        assertThat(created.get("imageCredit").isNull()).isTrue();
        JsonNode list = body(mockMvc.perform(get("/api/news"))).get("data");
        assertThat(list.get(0).get("id").asLong()).isEqualTo(created.get("id").asLong());
    }

    @Test
    void 제목이나_본문이_비었거나_제목이_200자를_넘으면_400() throws Exception {
        String admin = admin("boss");
        for (Map<String, Object> req : List.of(
                Map.<String, Object>of("title", " ", "body", "본문"),
                Map.<String, Object>of("title", "제목", "body", "\n"),
                Map.<String, Object>of("body", "본문"),
                Map.<String, Object>of("title", "가".repeat(201), "body", "본문"))) {
            expectError(mockMvc.perform(auth(withJson(post("/api/news"), req), admin)), 400, "INVALID_NEWS");
        }
    }

    @Test
    void 관리자는_수정하고_삭제한다_없는_뉴스면_404() throws Exception {
        long id = insertNews("소식", "본문", "2026-11-01 10:00:00");
        String admin = admin("boss");

        mockMvc.perform(auth(withJson(put("/api/news/{id}", id), Map.of("title", "고친 소식", "body", "고친 본문")), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("고친 소식"))
                .andExpect(jsonPath("$.data.updatedAt").exists());

        mockMvc.perform(auth(delete("/api/news/{id}", id), admin)).andExpect(status().isOk());
        expectError(mockMvc.perform(get("/api/news/{id}", id)), 404, "NEWS_NOT_FOUND");

        expectError(mockMvc.perform(auth(withJson(put("/api/news/{id}", MISSING_ID),
                Map.of("title", "제목", "body", "본문")), admin)), 404, "NEWS_NOT_FOUND");
        expectError(mockMvc.perform(auth(delete("/api/news/{id}", MISSING_ID), admin)), 404, "NEWS_NOT_FOUND");
    }

    @Test
    void 작성자_계정을_지워도_뉴스는_남는다() throws Exception {
        String admin = admin("boss");
        long id = body(mockMvc.perform(auth(withJson(post("/api/news"), Map.of("title", "소식", "body", "본문")), admin))
                .andExpect(status().isCreated())).at("/data/id").asLong();

        jdbc.update("DELETE FROM users WHERE username = 'boss'");

        mockMvc.perform(get("/api/news/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authorNickname").doesNotExist());
    }

    // ---- 도우미 ----

//  가입 → role 을 ADMIN 으로 → 다시 로그인. 토큰의 role 은 발급할 때 정해진다
    private String admin(String username) throws Exception {
        signup(username);
        jdbc.update("UPDATE users SET role = 'ADMIN' WHERE username = ?", username);
        return body(mockMvc.perform(withJson(post("/api/auth/login"), Map.of("username", username, "password", "pw1234")))
                .andExpect(status().isOk())).at("/data/token").asText();
    }

    private long insertNews(String title, String body, String createdAt) {
        jdbc.update("INSERT INTO news (title, body, created_at) VALUES (?, ?, ?)", title, body, createdAt);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }
}
