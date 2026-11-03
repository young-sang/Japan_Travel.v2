package com.japantravel.common;

import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

//  Spring 이 컨트롤러 전에 던지는 오류도 같은 봉투로 나간다 (D-034 · D-035). scripts/smoke/error.sh 를 옮긴 것.
class ErrorEnvelopeApiTest extends IntegrationTest {

    @Test
    void 성공_봉투는_error_가_null_이고_JSON_이다() throws Exception {
        mockMvc.perform(get("/api/destinations"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void 에러_메시지는_ErrorCode_의_고정_문구다() throws Exception {
        mockMvc.perform(get("/api/destinations/{id}", MISSING_ID))
                .andExpect(jsonPath("$.error.message").value("여행지를 찾을 수 없습니다"));
    }

    @Test
    void 없는_경로는_공개면_404_비공개면_Security_가_먼저_401() throws Exception {
        expectError(mockMvc.perform(get("/api/destinations/1/nope")), 404, "API_NOT_FOUND");
        expectError(mockMvc.perform(get("/api/nope")), 401, "UNAUTHORIZED");
        expectError(mockMvc.perform(auth(get("/api/nope"), signup("tanaka"))), 404, "API_NOT_FOUND");
    }

    @Test
    void 틀린_메서드는_토큰이_있으면_405() throws Exception {
        expectError(mockMvc.perform(auth(delete("/api/destinations/{id}", destinationId()), signup("tanaka"))),
                405, "METHOD_NOT_ALLOWED");
    }

    @Test
    void 깨진_JSON_이나_빈_본문은_400() throws Exception {
        expectError(mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{broken")),
                400, "MALFORMED_REQUEST");
        expectError(mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)),
                400, "MALFORMED_REQUEST");
    }

    @Test
    void JSON_이_아닌_Content_Type_은_415() throws Exception {
        expectError(mockMvc.perform(post("/api/auth/login").contentType(MediaType.TEXT_PLAIN).content("{}")),
                415, "UNSUPPORTED_MEDIA_TYPE");
    }
}
