package com.japantravel.course;

import com.fasterxml.jackson.databind.JsonNode;
import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseApiTest extends IntegrationTest {

    private String token;
    private String prefecture;

    @BeforeEach
    void setUp() throws Exception {
        token = signup("tanaka");
        prefecture = prefectureOfDestination(destinationId());
    }

    private Map<String, Object> stop(int dayNo, String type, long targetId) {
        return Map.of("dayNo", dayNo, "type", type, "targetId", targetId);
    }

    private Map<String, Object> course(String title, boolean isPublic, List<Map<String, Object>> stops) {
        Map<String, Object> req = new HashMap<>();
        req.put("title", title);
        req.put("description", "설명");
        req.put("prefecture", prefecture);
        req.put("isPublic", isPublic);
        req.put("stops", stops);
        return req;
    }

    private long create(String owner, String title, boolean isPublic) throws Exception {
        return body(mockMvc.perform(auth(withJson(post("/api/courses"), course(title, isPublic, List.of())), owner))
                .andExpect(status().isCreated())).at("/data/id").asLong();
    }

    // ---- 조회 ----

    @Test
    void 공개_목록에는_기본_제공_코스가_있다() throws Exception {
        JsonNode data = body(mockMvc.perform(get("/api/courses")).andExpect(status().isOk())).get("data");

        assertThat(data.size()).isPositive();
        assertThat(data.findValues("id")).extracting(JsonNode::asLong).contains(builtInCourseId());
    }

    @Test
    void 기본_제공_코스_상세는_주인이_없고_정류장이_순서대로다() throws Exception {
        JsonNode data = body(mockMvc.perform(get("/api/courses/{id}", builtInCourseId()))
                .andExpect(status().isOk())).get("data");

        assertThat(data.get("ownerId").isNull()).isTrue();
        JsonNode stops = data.get("stops");
        for (int i = 1; i < stops.size(); i++) {
            JsonNode prev = stops.get(i - 1), cur = stops.get(i);
            int byDay = Integer.compare(cur.get("dayNo").asInt(), prev.get("dayNo").asInt());
            assertThat(byDay > 0 || (byDay == 0 && cur.get("seq").asInt() > prev.get("seq").asInt())).isTrue();
        }
    }

    @Test
    void 현으로_거르고_없는_현이면_404() throws Exception {
        JsonNode data = body(mockMvc.perform(get("/api/courses").param("prefecture", prefecture))
                .andExpect(status().isOk())).get("data");
        data.forEach(c -> assertThat(c.get("prefecture").asText()).isEqualTo(prefecture));

        expectError(mockMvc.perform(get("/api/courses").param("prefecture", "없는현")), 404, "PREFECTURE_NOT_FOUND");
    }

    @Test
    void 없는_코스는_404() throws Exception {
        expectError(mockMvc.perform(get("/api/courses/{id}", MISSING_ID)), 404, "COURSE_NOT_FOUND");
    }

    // ---- 작성 ----

    @Test
    void 정류장은_일차별로_배열_순서대로_seq_가_매겨지고_일차순으로_나온다() throws Exception {
        long d1 = destinationId(), d2 = otherDestinationId(), f1 = festivalId();
        List<Map<String, Object>> stops = List.of(
                stop(2, "DESTINATION", d2),
                stop(1, "DESTINATION", d1),
                stop(1, "FESTIVAL", f1));

        mockMvc.perform(auth(withJson(post("/api/courses"), course("내 코스", true, stops)), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ownerId").value(userId("tanaka")))
                .andExpect(jsonPath("$.data.ownerNickname").value("tanaka-닉"))
                .andExpect(jsonPath("$.data.isPublic").value(true))
                .andExpect(jsonPath("$.data.stops.length()").value(3))
                .andExpect(jsonPath("$.data.stops[0].dayNo").value(1))
                .andExpect(jsonPath("$.data.stops[0].seq").value(1))
                .andExpect(jsonPath("$.data.stops[0].targetId").value(d1))
                .andExpect(jsonPath("$.data.stops[1].dayNo").value(1))
                .andExpect(jsonPath("$.data.stops[1].seq").value(2))
                .andExpect(jsonPath("$.data.stops[1].type").value("FESTIVAL"))
                .andExpect(jsonPath("$.data.stops[2].dayNo").value(2))
                .andExpect(jsonPath("$.data.stops[2].seq").value(1))
                .andExpect(jsonPath("$.data.stops[2].targetId").value(d2));
    }

    @Test
    void 필수_값이_빠지면_400() throws Exception {
        Map<String, Object> noTitle = course(" ", true, List.of());
        expectError(mockMvc.perform(auth(withJson(post("/api/courses"), noTitle), token)), 400, "INVALID_COURSE");

        Map<String, Object> noStops = course("t", true, List.of());
        noStops.remove("stops");
        expectError(mockMvc.perform(auth(withJson(post("/api/courses"), noStops), token)), 400, "INVALID_COURSE");

        Map<String, Object> noPublic = course("t", true, List.of());
        noPublic.remove("isPublic");
        expectError(mockMvc.perform(auth(withJson(post("/api/courses"), noPublic), token)), 400, "INVALID_COURSE");

        Map<String, Object> dayZero = course("t", true, List.of(stop(0, "DESTINATION", destinationId())));
        expectError(mockMvc.perform(auth(withJson(post("/api/courses"), dayZero), token)), 400, "INVALID_COURSE");
    }

    @Test
    void 정류장_종류가_틀리면_400_MALFORMED() throws Exception {
        List<Map<String, Object>> stops = new ArrayList<>();
        stops.add(Map.of("dayNo", 1, "type", "BUS", "targetId", destinationId()));

        expectError(mockMvc.perform(auth(withJson(post("/api/courses"), course("t", true, stops)), token)),
                400, "MALFORMED_REQUEST");
    }

    @Test
    void 없는_현이나_없는_정류장_대상이면_404() throws Exception {
        Map<String, Object> badPrefecture = course("t", true, List.of());
        badPrefecture.put("prefecture", "없는현");
        expectError(mockMvc.perform(auth(withJson(post("/api/courses"), badPrefecture), token)),
                404, "PREFECTURE_NOT_FOUND");

        expectError(mockMvc.perform(auth(withJson(post("/api/courses"),
                course("t", true, List.of(stop(1, "FESTIVAL", MISSING_ID)))), token)), 404, "FESTIVAL_NOT_FOUND");
    }

    // ---- 비공개 (D-041 · D-047) ----

    @Test
    void 비공개_코스는_주인만_보고_남에게는_404() throws Exception {
        long id = create(token, "비밀 코스", false);
        String other = signup("suzuki");

        mockMvc.perform(auth(get("/api/courses/{id}", id), token)).andExpect(status().isOk());
        expectError(mockMvc.perform(auth(get("/api/courses/{id}", id), other)), 404, "COURSE_NOT_FOUND");
        expectError(mockMvc.perform(get("/api/courses/{id}", id)), 404, "COURSE_NOT_FOUND");

        JsonNode publicIds = body(mockMvc.perform(get("/api/courses"))).get("data");
        assertThat(publicIds.findValues("id")).extracting(JsonNode::asLong).doesNotContain(id);
    }

    @Test
    void 내_코스는_공개_비공개_모두_나온다() throws Exception {
        long open = create(token, "공개", true);
        long secret = create(token, "비공개", false);
        create(signup("suzuki"), "남의 것", true);

        mockMvc.perform(auth(get("/api/me/courses"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(secret))
                .andExpect(jsonPath("$.data[1].id").value(open));
    }

    // ---- 수정 · 삭제 ----

    @Test
    void 수정은_정류장까지_전부_덮어쓴다() throws Exception {
        long id = body(mockMvc.perform(auth(withJson(post("/api/courses"),
                        course("처음", true, List.of(stop(1, "DESTINATION", destinationId()),
                                stop(1, "DESTINATION", otherDestinationId())))), token))
                .andExpect(status().isCreated())).at("/data/id").asLong();

        mockMvc.perform(auth(withJson(put("/api/courses/{id}", id),
                        course("바꿈", false, List.of(stop(3, "FESTIVAL", festivalId())))), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("바꿈"))
                .andExpect(jsonPath("$.data.isPublic").value(false))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.stops.length()").value(1))
                .andExpect(jsonPath("$.data.stops[0].dayNo").value(3))
                .andExpect(jsonPath("$.data.stops[0].seq").value(1));
    }

    @Test
    void 남의_공개_코스와_기본_제공_코스는_403() throws Exception {
        long id = create(token, "공개", true);
        String other = signup("suzuki");

        expectError(mockMvc.perform(auth(withJson(put("/api/courses/{id}", id), course("x", true, List.of())), other)),
                403, "FORBIDDEN");
        expectError(mockMvc.perform(auth(delete("/api/courses/{id}", id), other)), 403, "FORBIDDEN");
        expectError(mockMvc.perform(auth(withJson(put("/api/courses/{id}", builtInCourseId()),
                course("x", true, List.of())), token)), 403, "FORBIDDEN");
    }

    @Test
    void 남의_비공개_코스는_수정해도_403_이_아니라_404() throws Exception {
        long id = create(token, "비공개", false);

        expectError(mockMvc.perform(auth(withJson(put("/api/courses/{id}", id), course("x", true, List.of())),
                signup("suzuki"))), 404, "COURSE_NOT_FOUND");
    }

    @Test
    void 삭제는_멱등이_아니다() throws Exception {
        long id = create(token, "지울 것", true);

        mockMvc.perform(auth(delete("/api/courses/{id}", id), token)).andExpect(status().isOk());
        expectError(mockMvc.perform(auth(delete("/api/courses/{id}", id), token)), 404, "COURSE_NOT_FOUND");
    }

    @Test
    void 로그인하지_않으면_쓰기는_401() throws Exception {
        expectError(mockMvc.perform(withJson(post("/api/courses"), course("t", true, List.of()))),
                401, "UNAUTHORIZED");
        expectError(mockMvc.perform(get("/api/me/courses")), 401, "UNAUTHORIZED");
    }
}
