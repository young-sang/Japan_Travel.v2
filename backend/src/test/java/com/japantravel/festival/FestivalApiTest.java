package com.japantravel.festival;

import com.fasterxml.jackson.databind.JsonNode;
import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FestivalApiTest extends IntegrationTest {

    private Map<String, Object> festival(long id) {
        return jdbc.queryForMap("""
                SELECT f.month, p.name AS prefecture FROM festivals f JOIN prefectures p ON p.id = f.prefecture_id
                WHERE f.id = ?""", id);
    }

    @Test
    void 목록은_월_오름차순이다() throws Exception {
        JsonNode data = body(mockMvc.perform(get("/api/festivals")).andExpect(status().isOk())).get("data");

        assertThat(data.size()).isPositive();
        for (int i = 1; i < data.size(); i++) {
            assertThat(data.get(i).get("month").asInt()).isGreaterThanOrEqualTo(data.get(i - 1).get("month").asInt());
        }
    }

    @Test
    void 월로_거른다() throws Exception {
        int month = ((Number) festival(festivalId()).get("month")).intValue();

        JsonNode data = body(mockMvc.perform(get("/api/festivals").param("month", String.valueOf(month)))
                .andExpect(status().isOk())).get("data");

        assertThat(data.size()).isPositive();
        data.forEach(f -> assertThat(f.get("month").asInt()).isEqualTo(month));
    }

    @Test
    void 현과_월로_함께_거른다() throws Exception {
        Map<String, Object> seed = festival(festivalId());
        int month = ((Number) seed.get("month")).intValue();
        String prefecture = (String) seed.get("prefecture");

        JsonNode data = body(mockMvc.perform(get("/api/festivals")
                        .param("prefecture", prefecture).param("month", String.valueOf(month)))
                .andExpect(status().isOk())).get("data");

        assertThat(data.size()).isPositive();
        data.forEach(f -> {
            assertThat(f.get("month").asInt()).isEqualTo(month);
            assertThat(f.get("prefecture").asText()).isEqualTo(prefecture);
        });
    }

    @Test
    void 월이_범위_밖이면_400_숫자가_아니면_400() throws Exception {
        expectError(mockMvc.perform(get("/api/festivals").param("month", "13")), 400, "INVALID_MONTH");
        expectError(mockMvc.perform(get("/api/festivals").param("month", "0")), 400, "INVALID_MONTH");
        expectError(mockMvc.perform(get("/api/festivals").param("month", "abc")), 400, "TYPE_MISMATCH");
    }

    @Test
    void 없는_현이면_404() throws Exception {
        expectError(mockMvc.perform(get("/api/festivals").param("prefecture", "없는현")),
                404, "PREFECTURE_NOT_FOUND");
    }

    @Test
    void 상세를_보고_없는_id_면_404() throws Exception {
        long id = festivalId();

        mockMvc.perform(get("/api/festivals/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id));
        expectError(mockMvc.perform(get("/api/festivals/{id}", MISSING_ID)), 404, "FESTIVAL_NOT_FOUND");
    }

    @Test
    void 리뷰_집계가_상세에_실린다() throws Exception {
        long id = festivalId();
        String token = signup("tanaka");
        mockMvc.perform(auth(withJson(post("/api/festivals/{id}/reviews", id), Map.of("rating", 3)), token))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/festivals/{id}", id))
                .andExpect(jsonPath("$.data.avgRating").value(3.0))
                .andExpect(jsonPath("$.data.reviewCount").value(1));
    }
}
