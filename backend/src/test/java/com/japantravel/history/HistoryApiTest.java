package com.japantravel.history;

import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//  최근 본 장소 (D-052)
class HistoryApiTest extends IntegrationTest {

    private String token;

    @BeforeEach
    void login() throws Exception {
        token = signup("tanaka");
    }

    @Test
    void 같은_대상을_여러_번_봐도_한_건이다() throws Exception {
        long id = destinationId();
        mockMvc.perform(auth(post("/api/me/history/destinations/{id}", id), token)).andExpect(status().isOk());
        mockMvc.perform(auth(post("/api/me/history/destinations/{id}", id), token)).andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/me/history"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].type").value("DESTINATION"))
                .andExpect(jsonPath("$.data[0].targetId").value(id))
                .andExpect(jsonPath("$.data[0].prefecture").value(prefectureOfDestination(id)));
    }

    @Test
    void 두_대상을_섞어_최근에_본_것부터() throws Exception {
        long destinationId = destinationId();
        long festivalId = festivalId();
        mockMvc.perform(auth(post("/api/me/history/destinations/{id}", destinationId), token));
        mockMvc.perform(auth(post("/api/me/history/festivals/{id}", festivalId), token));
//      visited_at 은 초 단위 DATETIME 이라 같은 초 안의 두 기록은 순서가 정해지지 않는다. 시각을 벌려 둔다
        jdbc.update("UPDATE history_destinations SET visited_at = NOW() - INTERVAL 2 HOUR");
        jdbc.update("UPDATE history_festivals SET visited_at = NOW() - INTERVAL 1 HOUR");

        mockMvc.perform(auth(get("/api/me/history"), token))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].type").value("FESTIVAL"))
                .andExpect(jsonPath("$.data[1].type").value("DESTINATION"));

//      다시 보면 visitedAt 이 갱신돼 맨 앞으로 온다
        mockMvc.perform(auth(post("/api/me/history/destinations/{id}", destinationId), token));
        mockMvc.perform(auth(get("/api/me/history"), token))
                .andExpect(jsonPath("$.data[0].type").value("DESTINATION"));
    }

    @Test
    void 전체_삭제한다() throws Exception {
        mockMvc.perform(auth(post("/api/me/history/destinations/{id}", destinationId()), token));
        mockMvc.perform(auth(post("/api/me/history/festivals/{id}", festivalId()), token));

        mockMvc.perform(auth(delete("/api/me/history"), token)).andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/me/history"), token))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void 없는_대상이면_404() throws Exception {
        expectError(mockMvc.perform(auth(post("/api/me/history/destinations/{id}", MISSING_ID), token)),
                404, "DESTINATION_NOT_FOUND");
        expectError(mockMvc.perform(auth(post("/api/me/history/festivals/{id}", MISSING_ID), token)),
                404, "FESTIVAL_NOT_FOUND");
    }

    @Test
    void 로그인하지_않으면_401() throws Exception {
        expectError(mockMvc.perform(post("/api/me/history/destinations/{id}", destinationId())),
                401, "UNAUTHORIZED");
    }
}
