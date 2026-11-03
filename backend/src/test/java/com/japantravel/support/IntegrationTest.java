package com.japantravel.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//  API 통합 테스트의 공통 바탕 (D-062). 모든 하위 클래스가 스프링 컨텍스트 하나를 같이 쓴다.
//  트랜잭션 롤백을 쓰지 않는다 — 요청마다 커밋해야 @Formula 별점 집계가 실제 앱과 같이 보인다.
//  대신 매 테스트 전에 사용자를 지우고 FK CASCADE 가 찜 · 리뷰 · 기록 · 사용자 코스를 지운다. 시드는 읽기만 한다.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTest {

    protected static final long MISSING_ID = 999_999_999L;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void cleanUserData() {
//      운영 DB 를 지우지 않도록 — application-test.yml 이 빠지면 여기서 멈춘다
        String db = jdbc.queryForObject("SELECT DATABASE()", String.class);
        if (db == null || !db.endsWith("_test")) {
            throw new IllegalStateException("테스트 DB 가 아니다: " + db);
        }
        jdbc.update("DELETE FROM users");
    }

    // ---- 요청 ----

    protected String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    protected MockHttpServletRequestBuilder withJson(MockHttpServletRequestBuilder req, Object body) throws Exception {
        return req.contentType(MediaType.APPLICATION_JSON).content(json(body));
    }

    protected MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder req, String token) {
        return req.header("Authorization", "Bearer " + token);
    }

//  문자셋을 주지 않으면 MockHttpServletResponse 가 ISO-8859-1 로 읽어 한글이 깨진다
    protected JsonNode body(ResultActions result) throws Exception {
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    // ---- 판정 ----

//  실패 봉투: 상태 코드 + { success: false, data: null, error.code } (D-035)
    protected ResultActions expectError(ResultActions result, int status, String code) throws Exception {
        return result.andExpect(status().is(status))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value(code));
    }

    // ---- 사용자 ----

//  가입하고 토큰을 돌려준다. 비밀번호는 "pw1234"
    protected String signup(String username) throws Exception {
        ResultActions result = mockMvc.perform(withJson(post("/api/auth/signup"),
                        Map.of("username", username, "password", "pw1234", "nickname", username + "-닉")))
                .andExpect(status().isCreated());
        return body(result).at("/data/token").asText();
    }

    protected long userId(String username) {
        return jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);
    }

    // ---- 시드 조회 (id 를 테스트에 박지 않는다) ----

    protected long destinationId() {
        return jdbc.queryForObject("SELECT MIN(id) FROM destinations", Long.class);
    }

    protected long otherDestinationId() {
        return jdbc.queryForObject("SELECT MAX(id) FROM destinations", Long.class);
    }

    protected long festivalId() {
        return jdbc.queryForObject("SELECT MIN(id) FROM festivals", Long.class);
    }

    protected String prefectureOfDestination(long id) {
        return jdbc.queryForObject("""
                SELECT p.name FROM destinations d JOIN prefectures p ON p.id = d.prefecture_id
                WHERE d.id = ?""", String.class, id);
    }

    protected long builtInCourseId() {
        return jdbc.queryForObject("SELECT MIN(id) FROM courses WHERE owner_user_id IS NULL", Long.class);
    }
}
