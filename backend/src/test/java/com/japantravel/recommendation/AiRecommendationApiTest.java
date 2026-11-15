package com.japantravel.recommendation;

import com.fasterxml.jackson.databind.JsonNode;
import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.recommendation.ai.LlmPicker;
import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//  AI 추천 페이지 (M4-1 · M4-2). LLM 은 부르지 않는다 — LlmPicker 를 바꿔 끼운다
class AiRecommendationApiTest extends IntegrationTest {

    @MockBean
    private LlmPicker llmPicker;

    @Test
    void 비로그인도_고른_현의_여행지_카드와_이유를_받는다() throws Exception {
        long dest = destinationId();
        String prefecture = prefectureOfDestination(dest);
        long otherPref = jdbc.queryForObject("""
                SELECT MIN(d.id) FROM destinations d JOIN prefectures p ON p.id = d.prefecture_id
                WHERE p.name <> ?""", Long.class, prefecture);
//      후보 밖 키(다른 현 여행지)는 버려진다
        given(llmPicker.pick(anyString(), any(), anyList())).willReturn(List.of(
                new LlmPicker.Pick("D" + dest, "조용한 온천이 있어요"),
                new LlmPicker.Pick("D" + otherPref, "다른 현")));

        JsonNode data = body(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                        Map.of("preference", "조용한 온천", "prefecture", prefecture, "kind", "DESTINATION")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))).get("data");

        JsonNode card = data.get(0);
        assertThat(card.get("type").asText()).isEqualTo("DESTINATION");
        assertThat(card.get("targetId").asLong()).isEqualTo(dest);
        assertThat(card.get("prefecture").asText()).isEqualTo(prefecture);
        assertThat(card.get("name").asText()).isNotBlank();
        assertThat(card.get("reason").asText()).isEqualTo("조용한 온천이 있어요");
        assertThat(card.has("avgRating")).isTrue();
        assertThat(card.get("reviewCount").asLong()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 축제만_가을이면_가을_축제만_후보이고_조건_밖_키는_버린다() throws Exception {
//      가을 축제와 봄 축제가 모두 있는 현
        Map<String, Object> row = jdbc.queryForMap("""
                SELECT p.name AS prefecture,
                       MIN(CASE WHEN f.month IN (9,10,11) THEN f.id END) AS autumn,
                       MIN(CASE WHEN f.month IN (3,4,5) THEN f.id END) AS spring
                FROM festivals f JOIN prefectures p ON p.id = f.prefecture_id
                GROUP BY p.name
                HAVING autumn IS NOT NULL AND spring IS NOT NULL
                ORDER BY p.name LIMIT 1""");
        String prefecture = (String) row.get("prefecture");
        long autumn = ((Number) row.get("autumn")).longValue();
        long spring = ((Number) row.get("spring")).longValue();
        long dest = jdbc.queryForObject("""
                SELECT MIN(d.id) FROM destinations d JOIN prefectures p ON p.id = d.prefecture_id
                WHERE p.name = ?""", Long.class, prefecture);

        given(llmPicker.pick(anyString(), any(), anyList())).willReturn(List.of(
                new LlmPicker.Pick("D" + dest, "여행지"),
                new LlmPicker.Pick("F" + spring, "봄 축제"),
                new LlmPicker.Pick("F" + autumn, "가을 축제")));

        JsonNode data = body(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                        Map.of("preference", "단풍", "prefecture", prefecture, "kind", "FESTIVAL", "season", "AUTUMN")))
                .andExpect(status().isOk())).get("data");
        assertThat(data).hasSize(1);
        assertThat(data.get(0).get("type").asText()).isEqualTo("FESTIVAL");
        assertThat(data.get(0).get("targetId").asLong()).isEqualTo(autumn);

//      LLM 에 넘어간 후보는 모두 축제(F)이고 개최 월이 9~11월, 방문 시기도 넘어간다
        ArgumentCaptor<List<LlmPicker.Candidate>> captor = ArgumentCaptor.forClass(List.class);
        verify(llmPicker).pick(eq("단풍"), eq("가을(9~11월)"), captor.capture());
        assertThat(captor.getValue()).isNotEmpty().allSatisfy(c -> {
            assertThat(c.key()).startsWith("F");
            int month = jdbc.queryForObject("SELECT month FROM festivals WHERE id = ?", Integer.class,
                    Long.parseLong(c.key().substring(1)));
            assertThat(month).isBetween(9, 11);
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void 종류_계절을_생략하면_여행지와_축제를_함께_넘긴다() throws Exception {
        String prefecture = jdbc.queryForObject("""
                SELECT p.name FROM festivals f JOIN prefectures p ON p.id = f.prefecture_id
                ORDER BY f.id LIMIT 1""", String.class);
        long fest = jdbc.queryForObject("""
                SELECT MIN(f.id) FROM festivals f JOIN prefectures p ON p.id = f.prefecture_id
                WHERE p.name = ?""", Long.class, prefecture);
        given(llmPicker.pick(anyString(), any(), anyList())).willReturn(List.of(new LlmPicker.Pick("F" + fest, "축제")));

        JsonNode data = body(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                        Map.of("preference", "축제 구경", "prefecture", prefecture)))
                .andExpect(status().isOk())).get("data");
        assertThat(data.get(0).get("type").asText()).isEqualTo("FESTIVAL");

        ArgumentCaptor<List<LlmPicker.Candidate>> captor = ArgumentCaptor.forClass(List.class);
        verify(llmPicker).pick(anyString(), eq(null), captor.capture());
        assertThat(captor.getValue()).extracting(c -> c.key().substring(0, 1)).contains("D", "F");
    }

    @Test
    void 잘못된_계절_값은_400() throws Exception {
        expectError(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                Map.of("preference", "조용한 온천", "prefecture", prefectureOfDestination(destinationId()),
                        "season", "RAINY"))),
                400, "MALFORMED_REQUEST");
    }

    @Test
    void 빈_취향_문장은_400() throws Exception {
        expectError(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                Map.of("preference", "  ", "prefecture", prefectureOfDestination(destinationId())))),
                400, "INVALID_AI_REQUEST");
        verify(llmPicker, never()).pick(anyString(), any(), anyList());
    }

    @Test
    void 없는_현은_404() throws Exception {
        expectError(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                Map.of("preference", "조용한 온천", "prefecture", "없는현"))),
                404, "PREFECTURE_NOT_FOUND");
    }

    @Test
    void 조건에_맞는_후보가_없으면_404이고_LLM을_부르지_않는다() throws Exception {
//      겨울(12 · 1 · 2월) 축제가 하나도 없는 현
        String prefecture = jdbc.queryForObject("""
                SELECT p.name FROM prefectures p
                WHERE NOT EXISTS (SELECT 1 FROM festivals f WHERE f.prefecture_id = p.id AND f.month IN (12, 1, 2))
                ORDER BY p.id LIMIT 1""", String.class);
        expectError(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                Map.of("preference", "눈 축제", "prefecture", prefecture, "kind", "FESTIVAL", "season", "WINTER"))),
                404, "NO_AI_CANDIDATES");
        verify(llmPicker, never()).pick(anyString(), any(), anyList());
    }

    @Test
    void LLM_호출이_실패하면_502() throws Exception {
        given(llmPicker.pick(anyString(), any(), anyList()))
                .willThrow(new ApiException(ErrorCode.AI_RECOMMEND_FAILED));
        expectError(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                Map.of("preference", "조용한 온천", "prefecture", prefectureOfDestination(destinationId())))),
                502, "AI_RECOMMEND_FAILED");
    }

    @Test
    void LLM이_후보_밖만_고르면_502() throws Exception {
        given(llmPicker.pick(anyString(), any(), anyList()))
                .willReturn(List.of(new LlmPicker.Pick("D" + MISSING_ID, "없음")));
        expectError(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                Map.of("preference", "조용한 온천", "prefecture", prefectureOfDestination(destinationId())))),
                502, "AI_RECOMMEND_FAILED");
    }
}
