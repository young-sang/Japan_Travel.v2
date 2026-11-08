package com.japantravel.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//  통합 검색 (D-053)
class SearchApiTest extends IntegrationTest {

    @Test
    void 검색어가_없거나_비면_빈_결과다() throws Exception {
        for (String q : new String[]{null, " "}) {
            var req = get("/api/search");
            if (q != null) req.param("q", q);
            mockMvc.perform(req)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.destinations.length()").value(0))
                    .andExpect(jsonPath("$.data.festivals.length()").value(0))
                    .andExpect(jsonPath("$.data.courses.length()").value(0));
        }
    }

    @Test
    void 현_이름으로_찾으면_그_현의_여행지가_나온다() throws Exception {
        String prefecture = prefectureOfDestination(destinationId());

        JsonNode destinations = body(mockMvc.perform(get("/api/search").param("q", prefecture))
                .andExpect(status().isOk())).at("/data/destinations");

        assertThat(destinations.size()).isBetween(1, 20);
    }

    @Test
    void 공개_코스만_찾는다() throws Exception {
        String token = signup("tanaka");
        String prefecture = prefectureOfDestination(destinationId());
        for (boolean isPublic : new boolean[]{true, false}) {
            mockMvc.perform(auth(withJson(post("/api/courses"), Map.of("title", "검색용코스" + isPublic,
                    "prefecture", prefecture, "isPublic", isPublic, "stops", List.of())), token))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(auth(get("/api/search").param("q", "검색용코스"), token))
                .andExpect(jsonPath("$.data.courses.length()").value(1))
                .andExpect(jsonPath("$.data.courses[0].title").value("검색용코스true"));
    }

//  띄어 쓴 검색어는 낱말로 나눠, 모든 낱말이 (서로 다른 칸에 있어도) 들어간 것만 찾는다 (D-083)
    @Test
    void 띄어_쓴_검색어는_모든_낱말이_들어간_것만_찾는다() throws Exception {
        String token = signup("suzuki");
        String prefecture = prefectureOfDestination(destinationId());
        for (String title : new String[]{"낱말갑 낱말을", "낱말갑"}) {
            mockMvc.perform(auth(withJson(post("/api/courses"), Map.of("title", title,
                    "prefecture", prefecture, "isPublic", true, "stops", List.of())), token))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/search").param("q", "낱말을  낱말갑"))
                .andExpect(jsonPath("$.data.courses.length()").value(1))
                .andExpect(jsonPath("$.data.courses[0].title").value("낱말갑 낱말을"));

        mockMvc.perform(get("/api/search").param("q", "낱말갑 " + prefecture))
                .andExpect(jsonPath("$.data.courses.length()").value(2));
    }
}
