package com.japantravel.recommendation;

import com.fasterxml.jackson.databind.JsonNode;
import com.japantravel.recommendation.ai.LlmPicker;
import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//  AI 추천 페이지 (M4-1). LLM 은 부르지 않는다 — LlmPicker 를 바꿔 끼운다
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
//      후보 밖 id(다른 현 여행지)는 버려진다
        given(llmPicker.pick(anyString(), any())).willReturn(List.of(
                new LlmPicker.Pick(dest, "조용한 온천이 있어요"),
                new LlmPicker.Pick(otherPref, "다른 현")));

        JsonNode data = body(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                        Map.of("preference", "조용한 온천", "prefecture", prefecture)))
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
    void 빈_취향_문장은_400() throws Exception {
        expectError(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                Map.of("preference", "  ", "prefecture", prefectureOfDestination(destinationId())))),
                400, "INVALID_AI_REQUEST");
        verify(llmPicker, never()).pick(anyString(), any());
    }

    @Test
    void 없는_현은_404() throws Exception {
        expectError(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                Map.of("preference", "조용한 온천", "prefecture", "없는현"))),
                404, "PREFECTURE_NOT_FOUND");
    }

    @Test
    void LLM이_후보_밖만_고르면_502() throws Exception {
        given(llmPicker.pick(anyString(), any())).willReturn(List.of(new LlmPicker.Pick(MISSING_ID, "없음")));
        expectError(mockMvc.perform(withJson(post("/api/recommendations/ai"),
                Map.of("preference", "조용한 온천", "prefecture", prefectureOfDestination(destinationId())))),
                502, "AI_RECOMMEND_FAILED");
    }
}
