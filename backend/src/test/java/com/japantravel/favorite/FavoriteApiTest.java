package com.japantravel.favorite;

import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FavoriteApiTest extends IntegrationTest {

    private String token;

    @BeforeEach
    void login() throws Exception {
        token = signup("tanaka");
    }

    @Test
    void 여행지와_축제를_찜하면_각자의_배열에_나온다() throws Exception {
        long destinationId = destinationId();
        long festivalId = festivalId();

        mockMvc.perform(auth(post("/api/me/favorites/destinations/{id}", destinationId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
        mockMvc.perform(auth(post("/api/me/favorites/festivals/{id}", festivalId), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/me/favorites"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.destinations.length()").value(1))
                .andExpect(jsonPath("$.data.destinations[0].id").value(destinationId))
                .andExpect(jsonPath("$.data.festivals.length()").value(1))
                .andExpect(jsonPath("$.data.festivals[0].id").value(festivalId));
    }

    @Test
    void 두_번_찜해도_한_건이다() throws Exception {
        long id = destinationId();
        mockMvc.perform(auth(post("/api/me/favorites/destinations/{id}", id), token)).andExpect(status().isOk());
        mockMvc.perform(auth(post("/api/me/favorites/destinations/{id}", id), token)).andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/me/favorites"), token))
                .andExpect(jsonPath("$.data.destinations.length()").value(1));
    }

    @Test
    void 찜을_풀고_없는_찜을_풀어도_200() throws Exception {
        long id = destinationId();
        mockMvc.perform(auth(post("/api/me/favorites/destinations/{id}", id), token)).andExpect(status().isOk());

        mockMvc.perform(auth(delete("/api/me/favorites/destinations/{id}", id), token)).andExpect(status().isOk());
        mockMvc.perform(auth(delete("/api/me/favorites/destinations/{id}", id), token)).andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/me/favorites"), token))
                .andExpect(jsonPath("$.data.destinations.length()").value(0));
    }

    @Test
    void 없는_대상을_찜하면_404() throws Exception {
        expectError(mockMvc.perform(auth(post("/api/me/favorites/destinations/{id}", MISSING_ID), token)),
                404, "DESTINATION_NOT_FOUND");
        expectError(mockMvc.perform(auth(post("/api/me/favorites/festivals/{id}", MISSING_ID), token)),
                404, "FESTIVAL_NOT_FOUND");
    }

    @Test
    void 남의_찜은_보이지_않는다() throws Exception {
        mockMvc.perform(auth(post("/api/me/favorites/destinations/{id}", destinationId()), token))
                .andExpect(status().isOk());
        String other = signup("suzuki");

        mockMvc.perform(auth(get("/api/me/favorites"), other))
                .andExpect(jsonPath("$.data.destinations.length()").value(0));
    }

    @Test
    void 로그인하지_않으면_401() throws Exception {
        expectError(mockMvc.perform(get("/api/me/favorites")), 401, "UNAUTHORIZED");
    }
}
