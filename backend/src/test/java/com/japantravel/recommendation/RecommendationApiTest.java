package com.japantravel.recommendation;

import com.fasterxml.jackson.databind.JsonNode;
import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//  추천 — 활동 기록으로 비슷한 장소 5곳 (D-098). 시드 임베딩(embeddings.sql)을 그대로 쓴다
class RecommendationApiTest extends IntegrationTest {

    private String token;

    @BeforeEach
    void login() throws Exception {
        token = signup("tanaka");
    }

    @Test
    void 활동한_장소와_비슷한_5곳을_유사도_순으로_준다() throws Exception {
        long favorite = destinationId();
        mockMvc.perform(auth(post("/api/me/favorites/destinations/{id}", favorite), token)).andExpect(status().isOk());

        JsonNode data = body(mockMvc.perform(auth(get("/api/me/recommendations"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5))).get("data");

        double previous = Double.MAX_VALUE;
        Map<String, Integer> perType = new HashMap<>();
        Map<String, Integer> perPrefecture = new HashMap<>();
        for (JsonNode item : data) {
            perType.merge(item.get("type").asText(), 1, Integer::sum);
            perPrefecture.merge(item.get("prefecture").asText(), 1, Integer::sum);
//          활동한 장소는 다시 추천하지 않는다
            assertThat(item.get("type").asText() + item.get("targetId").asLong()).isNotEqualTo("DESTINATION" + favorite);
            assertThat(item.get("name").asText()).isNotBlank();
            assertThat(item.get("prefecture").asText()).isNotBlank();
//          활동이 즐겨찾기 하나뿐이라 모든 이유의 근거는 그 장소다 (D-100)
            assertThat(item.get("reason").asText()).startsWith("즐겨찾기한 '");
            assertThat(item.at("/basis/type").asText()).isEqualTo("DESTINATION");
            assertThat(item.at("/basis/targetId").asLong()).isEqualTo(favorite);
            assertThat(item.at("/basis/activity").asText()).isEqualTo("FAVORITE");
            double similarity = item.get("similarity").asDouble();
            assertThat(similarity).isLessThanOrEqualTo(previous);
            previous = similarity;
        }
//      여행지 4 + 축제 1, 같은 현은 최대 2곳 (D-099)
        assertThat(perType).isEqualTo(Map.of("DESTINATION", 4, "FESTIVAL", 1));
        assertThat(perPrefecture.values()).allMatch(n -> n <= 2);
    }

    @Test
    void 활동이_없으면_빈_배열() throws Exception {
        mockMvc.perform(auth(get("/api/me/recommendations"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void 로그인하지_않으면_401() throws Exception {
        expectError(mockMvc.perform(get("/api/me/recommendations")), 401, "UNAUTHORIZED");
    }
}
