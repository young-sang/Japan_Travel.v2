package com.japantravel.destination;

import com.fasterxml.jackson.databind.JsonNode;
import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DestinationApiTest extends IntegrationTest {

    @Test
    void 목록은_로그인_없이_최신순으로_본다() throws Exception {
        JsonNode data = body(mockMvc.perform(get("/api/destinations")).andExpect(status().isOk())).get("data");

        assertThat(data.size()).isPositive();
        assertThat(data.get(0).get("id").asLong()).isGreaterThan(data.get(data.size() - 1).get("id").asLong());
    }

    @Test
    void 현으로_거르면_그_현의_여행지만_나온다() throws Exception {
        String prefecture = prefectureOfDestination(destinationId());

        JsonNode data = body(mockMvc.perform(get("/api/destinations").param("prefecture", prefecture))
                .andExpect(status().isOk())).get("data");

        assertThat(data.size()).isPositive();
        data.forEach(d -> assertThat(d.get("prefecture").asText()).isEqualTo(prefecture));
    }

    @Test
    void 없는_현이면_404() throws Exception {
        expectError(mockMvc.perform(get("/api/destinations").param("prefecture", "없는현")),
                404, "PREFECTURE_NOT_FOUND");
    }

    @Test
    void 상세를_본다() throws Exception {
        long id = destinationId();

        mockMvc.perform(get("/api/destinations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.prefecture").value(prefectureOfDestination(id)));
    }

    @Test
    void 없는_id_면_404_숫자가_아니면_400() throws Exception {
        expectError(mockMvc.perform(get("/api/destinations/{id}", MISSING_ID)), 404, "DESTINATION_NOT_FOUND");
        expectError(mockMvc.perform(get("/api/destinations/abc")), 400, "TYPE_MISMATCH");
    }

    @Test
    void 리뷰가_없으면_평균은_null_있으면_소수_첫째_자리_평균과_개수() throws Exception {
        long id = destinationId();
        mockMvc.perform(get("/api/destinations/{id}", id))
                .andExpect(jsonPath("$.data.avgRating").doesNotExist())
                .andExpect(jsonPath("$.data.reviewCount").value(0));

        String token = signup("tanaka");
        for (int rating : new int[]{4, 5, 5}) {
            mockMvc.perform(auth(withJson(post("/api/destinations/{id}/reviews", id), Map.of("rating", rating)), token))
                    .andExpect(status().isCreated());
        }

//      (4 + 5 + 5) / 3 = 4.666… → 4.7 (D-057)
        mockMvc.perform(get("/api/destinations/{id}", id))
                .andExpect(jsonPath("$.data.avgRating").value(4.7))
                .andExpect(jsonPath("$.data.reviewCount").value(3));
    }
}
