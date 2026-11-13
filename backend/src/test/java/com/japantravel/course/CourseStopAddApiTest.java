package com.japantravel.course;

import com.fasterxml.jackson.databind.JsonNode;
import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//  상세 · 즐겨찾기에서 내 코스에 장소 하나를 더한다 (D-094). 마지막 일차의 끝에 붙고,
//  판정 순서는 수정과 같다 — 본문 → 코스 존재(남의 비공개도 404) → 작성자 → 대상 장소 (D-047)
class CourseStopAddApiTest extends IntegrationTest {

    private String token;
    private String prefecture;

    @BeforeEach
    void setUp() throws Exception {
        token = signup("tanaka");
        prefecture = prefectureOfDestination(destinationId());
    }

    private long create(String owner, boolean isPublic, List<Map<String, Object>> stops) throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("title", "코스");
        req.put("prefecture", prefecture);
        req.put("isPublic", isPublic);
        req.put("stops", stops);
        return body(mockMvc.perform(auth(withJson(post("/api/courses"), req), owner))
                .andExpect(status().isCreated())).at("/data/id").asLong();
    }

    private Map<String, Object> add(String type, long targetId) {
        return Map.of("type", type, "targetId", targetId);
    }

    @Test
    void 빈_코스에_더하면_1일차_첫_정류장이_된다() throws Exception {
        long id = create(token, true, List.of());

        JsonNode stops = body(mockMvc.perform(auth(withJson(post("/api/me/courses/{id}/stops", id),
                add("DESTINATION", destinationId())), token)).andExpect(status().isCreated())).at("/data/stops");

        assertThat(stops.size()).isEqualTo(1);
        assertThat(stops.get(0).get("dayNo").asInt()).isEqualTo(1);
        assertThat(stops.get(0).get("seq").asInt()).isEqualTo(1);
        assertThat(stops.get(0).get("targetId").asLong()).isEqualTo(destinationId());
    }

    @Test
    void 마지막_일차의_끝에_붙고_메모도_받는다() throws Exception {
        long id = create(token, false, List.of(
                Map.of("dayNo", 1, "type", "DESTINATION", "targetId", destinationId()),
                Map.of("dayNo", 2, "type", "DESTINATION", "targetId", otherDestinationId())));

        JsonNode stops = body(mockMvc.perform(auth(withJson(post("/api/me/courses/{id}/stops", id),
                Map.of("type", "FESTIVAL", "targetId", festivalId(), "memo", "저녁에")), token))
                .andExpect(status().isCreated())).at("/data/stops");

        JsonNode last = stops.get(stops.size() - 1);
        assertThat(stops.size()).isEqualTo(3);
        assertThat(last.get("dayNo").asInt()).isEqualTo(2);
        assertThat(last.get("seq").asInt()).isEqualTo(2);
        assertThat(last.get("type").asText()).isEqualTo("FESTIVAL");
        assertThat(last.get("memo").asText()).isEqualTo("저녁에");
    }

    @Test
    void 종류나_대상이_빠지면_400() throws Exception {
        long id = create(token, true, List.of());

        expectError(mockMvc.perform(auth(withJson(post("/api/me/courses/{id}/stops", id),
                Map.of("type", "DESTINATION")), token)), 400, "INVALID_COURSE");
        expectError(mockMvc.perform(auth(withJson(post("/api/me/courses/{id}/stops", id),
                Map.of("targetId", destinationId())), token)), 400, "INVALID_COURSE");
    }

    @Test
    void 남의_비공개는_404_남의_공개와_기본_제공은_403() throws Exception {
        String other = signup("suzuki");
        long hidden = create(other, false, List.of());
        long open = create(other, true, List.of());

        expectError(mockMvc.perform(auth(withJson(post("/api/me/courses/{id}/stops", hidden),
                add("DESTINATION", destinationId())), token)), 404, "COURSE_NOT_FOUND");
        expectError(mockMvc.perform(auth(withJson(post("/api/me/courses/{id}/stops", open),
                add("DESTINATION", destinationId())), token)), 403, "FORBIDDEN");
        expectError(mockMvc.perform(auth(withJson(post("/api/me/courses/{id}/stops", builtInCourseId()),
                add("DESTINATION", destinationId())), token)), 403, "FORBIDDEN");
    }

    @Test
    void 없는_코스나_없는_장소면_404_로그인하지_않으면_401() throws Exception {
        long id = create(token, true, List.of());

        expectError(mockMvc.perform(auth(withJson(post("/api/me/courses/{id}/stops", MISSING_ID),
                add("DESTINATION", destinationId())), token)), 404, "COURSE_NOT_FOUND");
        expectError(mockMvc.perform(auth(withJson(post("/api/me/courses/{id}/stops", id),
                add("DESTINATION", MISSING_ID)), token)), 404, "DESTINATION_NOT_FOUND");
        expectError(mockMvc.perform(auth(withJson(post("/api/me/courses/{id}/stops", id),
                add("FESTIVAL", MISSING_ID)), token)), 404, "FESTIVAL_NOT_FOUND");
        expectError(mockMvc.perform(withJson(post("/api/me/courses/{id}/stops", id),
                add("DESTINATION", destinationId()))), 401, "UNAUTHORIZED");
    }
}
