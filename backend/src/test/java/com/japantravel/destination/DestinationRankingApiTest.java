package com.japantravel.destination;

import com.fasterxml.jackson.databind.JsonNode;
import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//  Top25 순위 (D-074 · D-076). 점수 = 본 사람 수 + 즐겨찾기 수 × 2 + 리뷰 수 × 3.
//  매 테스트 전에 사용자가 지워지므로(IntegrationTest) 점수는 이 테스트가 만든 것뿐이다.
class DestinationRankingApiTest extends IntegrationTest {

    @Test
    void 리뷰_찜_본_순으로_무겁고_0점은_빠진다() throws Exception {
        List<Long> ids = destinationIds(3);
        String token = signup("tanaka");
        review(token, ids.get(0), 3);      // 3점
        favorite(token, ids.get(1));       // 2점
        view(token, ids.get(2));           // 1점

        JsonNode data = ranking();

        assertThat(data.size()).isEqualTo(3);
        assertThat(idsOf(data)).containsExactly(ids.get(0), ids.get(1), ids.get(2));
        assertThat(data.get(0).get("rank").asInt()).isEqualTo(1);
        assertThat(data.get(0).get("score").asLong()).isEqualTo(3);
        assertThat(data.get(1).get("favoriteCount").asLong()).isEqualTo(1);
        assertThat(data.get(2).get("viewerCount").asLong()).isEqualTo(1);
        assertThat(data.get(0).at("/destination/reviewCount").asLong()).isEqualTo(1);
        assertThat(data.get(0).at("/destination/prefecture").asText()).isNotBlank();
    }

    @Test
    void 점수가_같으면_평점_높은_곳_그다음_리뷰_없는_곳은_id_순() throws Exception {
        List<Long> ids = destinationIds(4);
        String a = signup("tanaka");
        String b = signup("suzuki");
//      ids[0] · ids[1]: 리뷰 1건 = 3점. 평점은 ids[1] 이 높다
        review(a, ids.get(0), 2);
        review(a, ids.get(1), 5);
//      ids[2] · ids[3]: 찜 1 + 본 사람 1 = 3점. 리뷰가 없어 평점은 null — 평점 있는 곳 뒤, id 오름차순
        favorite(a, ids.get(3));
        view(b, ids.get(3));
        favorite(a, ids.get(2));
        view(b, ids.get(2));

        assertThat(idsOf(ranking())).containsExactly(ids.get(1), ids.get(0), ids.get(2), ids.get(3));
    }

    @Test
    void 최대_25개() throws Exception {
        String token = signup("tanaka");
        for (long id : destinationIds(26)) {
            view(token, id);
        }

        JsonNode data = ranking();

        assertThat(data.size()).isEqualTo(25);
        assertThat(data.get(24).get("rank").asInt()).isEqualTo(25);
    }

    @Test
    void 점수가_하나도_없으면_빈_목록() throws Exception {
        assertThat(ranking().size()).isZero();
    }

    // ---- 도우미 ----

    private JsonNode ranking() throws Exception {
        return body(mockMvc.perform(get("/api/destinations/ranking")).andExpect(status().isOk())).get("data");
    }

    private List<Long> destinationIds(int n) {
        return jdbc.queryForList("SELECT id FROM destinations ORDER BY id LIMIT ?", Long.class, n);
    }

    private List<Long> idsOf(JsonNode data) {
        return java.util.stream.StreamSupport.stream(data.spliterator(), false)
                .map(r -> r.at("/destination/id").asLong()).toList();
    }

    private void review(String token, long id, int rating) throws Exception {
        mockMvc.perform(auth(withJson(post("/api/destinations/{id}/reviews", id), Map.of("rating", rating)), token))
                .andExpect(status().isCreated());
    }

    private void favorite(String token, long id) throws Exception {
        mockMvc.perform(auth(post("/api/me/favorites/destinations/{id}", id), token))
                .andExpect(status().is2xxSuccessful());
    }

    private void view(String token, long id) throws Exception {
        mockMvc.perform(auth(post("/api/me/history/destinations/{id}", id), token))
                .andExpect(status().is2xxSuccessful());
    }
}
