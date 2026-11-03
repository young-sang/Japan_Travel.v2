package com.japantravel.review;

import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewApiTest extends IntegrationTest {

    private String token;
    private long destinationId;

    @BeforeEach
    void setUp() throws Exception {
        token = signup("tanaka");
        destinationId = destinationId();
    }

    private long write(long targetId, int rating) throws Exception {
        return body(mockMvc.perform(auth(withJson(post("/api/destinations/{id}/reviews", targetId),
                        Map.of("rating", rating, "comment", "좋다")), token))
                .andExpect(status().isCreated())).at("/data/id").asLong();
    }

    @Test
    void 리뷰를_쓰면_201_과_작성자를_돌려준다() throws Exception {
        mockMvc.perform(auth(withJson(post("/api/destinations/{id}/reviews", destinationId),
                        Map.of("rating", 4, "comment", "좋다")), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.rating").value(4))
                .andExpect(jsonPath("$.data.comment").value("좋다"))
                .andExpect(jsonPath("$.data.userId").value(userId("tanaka")))
                .andExpect(jsonPath("$.data.nickname").value("tanaka-닉"))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.updatedAt").doesNotExist());
    }

    @Test
    void 코멘트_없이_별점만_쓸_수_있다() throws Exception {
        mockMvc.perform(auth(withJson(post("/api/destinations/{id}/reviews", destinationId),
                        Map.of("rating", 5)), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.comment").doesNotExist());
    }

    @Test
    void 별점이_1에서_5_밖이거나_없으면_400() throws Exception {
        for (int rating : new int[]{0, 6}) {
            expectError(mockMvc.perform(auth(withJson(post("/api/destinations/{id}/reviews", destinationId),
                    Map.of("rating", rating)), token)), 400, "INVALID_RATING");
        }
        Map<String, Object> noRating = new HashMap<>();
        noRating.put("comment", "별점 없음");
        expectError(mockMvc.perform(auth(withJson(post("/api/destinations/{id}/reviews", destinationId),
                noRating), token)), 400, "INVALID_RATING");
    }

    @Test
    void 목록은_로그인_없이_최신순으로_본다() throws Exception {
        long first = write(destinationId, 3);
        long second = write(destinationId, 5);

        mockMvc.perform(get("/api/destinations/{id}/reviews", destinationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(second))
                .andExpect(jsonPath("$.data[1].id").value(first));
    }

    @Test
    void 없는_대상의_목록이나_작성은_404() throws Exception {
        expectError(mockMvc.perform(get("/api/destinations/{id}/reviews", MISSING_ID)), 404, "DESTINATION_NOT_FOUND");
        expectError(mockMvc.perform(auth(withJson(post("/api/destinations/{id}/reviews", MISSING_ID),
                Map.of("rating", 3)), token)), 404, "DESTINATION_NOT_FOUND");
    }

    @Test
    void 작성자는_수정하고_삭제한다() throws Exception {
        long reviewId = write(destinationId, 3);

        mockMvc.perform(auth(withJson(put("/api/destinations/{id}/reviews/{rid}", destinationId, reviewId),
                        Map.of("rating", 1, "comment", "별로")), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(1))
                .andExpect(jsonPath("$.data.comment").value("별로"))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());

        mockMvc.perform(auth(delete("/api/destinations/{id}/reviews/{rid}", destinationId, reviewId), token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/destinations/{id}/reviews", destinationId))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void 남의_리뷰는_수정_삭제가_403() throws Exception {
        long reviewId = write(destinationId, 3);
        String other = signup("suzuki");

        expectError(mockMvc.perform(auth(withJson(put("/api/destinations/{id}/reviews/{rid}", destinationId, reviewId),
                Map.of("rating", 1)), other)), 403, "FORBIDDEN");
        expectError(mockMvc.perform(auth(delete("/api/destinations/{id}/reviews/{rid}", destinationId, reviewId), other)),
                403, "FORBIDDEN");
    }

    @Test
    void 없는_리뷰나_다른_대상에_딸린_리뷰는_403_이_아니라_404() throws Exception {
        long reviewId = write(destinationId, 3);
        String other = signup("suzuki");

        expectError(mockMvc.perform(auth(delete("/api/destinations/{id}/reviews/{rid}", destinationId, MISSING_ID), other)),
                404, "REVIEW_NOT_FOUND");
//      경로의 여행지와 리뷰의 여행지가 다르면 없는 리뷰다
        expectError(mockMvc.perform(auth(withJson(put("/api/destinations/{id}/reviews/{rid}", otherDestinationId(), reviewId),
                Map.of("rating", 2)), token)), 404, "REVIEW_NOT_FOUND");
    }

    @Test
    void 수정할_때도_별점을_먼저_본다() throws Exception {
        expectError(mockMvc.perform(auth(withJson(put("/api/destinations/{id}/reviews/{rid}", destinationId, MISSING_ID),
                Map.of("rating", 9)), token)), 400, "INVALID_RATING");
    }

    @Test
    void 축제_리뷰도_같은_규칙이다() throws Exception {
        long festivalId = festivalId();
        long reviewId = body(mockMvc.perform(auth(withJson(post("/api/festivals/{id}/reviews", festivalId),
                        Map.of("rating", 2)), token))
                .andExpect(status().isCreated())).at("/data/id").asLong();

        mockMvc.perform(get("/api/festivals/{id}/reviews", festivalId))
                .andExpect(jsonPath("$.data[0].id").value(reviewId));
        mockMvc.perform(auth(withJson(put("/api/festivals/{id}/reviews/{rid}", festivalId, reviewId),
                        Map.of("rating", 5)), token))
                .andExpect(jsonPath("$.data.rating").value(5));
        expectError(mockMvc.perform(auth(delete("/api/festivals/{id}/reviews/{rid}", festivalId, reviewId),
                signup("suzuki"))), 403, "FORBIDDEN");
        expectError(mockMvc.perform(get("/api/festivals/{id}/reviews", MISSING_ID)), 404, "FESTIVAL_NOT_FOUND");
    }

    @Test
    void 내_리뷰는_두_대상을_합쳐_최신순으로_본다() throws Exception {
        write(destinationId, 4);
        long festivalId = festivalId();
        mockMvc.perform(auth(withJson(post("/api/festivals/{id}/reviews", festivalId), Map.of("rating", 2)), token))
                .andExpect(status().isCreated());
        write(destinationId, 1);
//      같은 초 안의 리뷰도 created_at(마이크로초, D-065) 으로 순서가 갈린다
        signup("suzuki");

        mockMvc.perform(auth(get("/api/me/reviews"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].type").value("DESTINATION"))
                .andExpect(jsonPath("$.data[0].rating").value(1))
                .andExpect(jsonPath("$.data[1].type").value("FESTIVAL"))
                .andExpect(jsonPath("$.data[1].targetId").value(festivalId))
                .andExpect(jsonPath("$.data[2].rating").value(4));
    }

    @Test
    void 로그인하지_않으면_쓰기는_401() throws Exception {
        expectError(mockMvc.perform(withJson(post("/api/destinations/{id}/reviews", destinationId),
                Map.of("rating", 3))), 401, "UNAUTHORIZED");
        expectError(mockMvc.perform(get("/api/me/reviews")), 401, "UNAUTHORIZED");
    }
}
