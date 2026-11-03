package com.japantravel.user;

import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiTest extends IntegrationTest {

    @Test
    void 가입하면_201_과_토큰_사용자를_돌려준다() throws Exception {
        mockMvc.perform(withJson(post("/api/auth/signup"),
                        Map.of("username", "tanaka", "password", "pw1234", "nickname", "다나카")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("tanaka"))
                .andExpect(jsonPath("$.data.user.nickname").value("다나카"))
                .andExpect(jsonPath("$.data.user.role").value("USER"));
    }

    @Test
    void 같은_아이디로_가입하면_409() throws Exception {
        signup("tanaka");

        expectError(mockMvc.perform(withJson(post("/api/auth/signup"),
                Map.of("username", "tanaka", "password", "x", "nickname", "x"))), 409, "USERNAME_TAKEN");
    }

    @Test
    void 로그인하면_토큰을_돌려준다() throws Exception {
        signup("tanaka");

        mockMvc.perform(withJson(post("/api/auth/login"), Map.of("username", "tanaka", "password", "pw1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("tanaka"));
    }

    @Test
    void 틀린_비밀번호와_없는_아이디는_같은_401() throws Exception {
        signup("tanaka");

        expectError(mockMvc.perform(withJson(post("/api/auth/login"),
                Map.of("username", "tanaka", "password", "wrong"))), 401, "LOGIN_FAILED");
        expectError(mockMvc.perform(withJson(post("/api/auth/login"),
                Map.of("username", "nobody", "password", "pw1234"))), 401, "LOGIN_FAILED");
    }

    @Test
    void 토큰으로_내_정보를_본다() throws Exception {
        String token = signup("tanaka");

        mockMvc.perform(auth(get("/api/auth/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId("tanaka")))
                .andExpect(jsonPath("$.data.username").value("tanaka"));
    }

    @Test
    void 토큰이_없거나_가짜면_401() throws Exception {
        expectError(mockMvc.perform(get("/api/auth/me")), 401, "UNAUTHORIZED");
        expectError(mockMvc.perform(auth(get("/api/auth/me"), "not-a-jwt")), 401, "UNAUTHORIZED");
    }

    @Test
    void 토큰은_유효한데_사용자가_지워졌으면_401() throws Exception {
        String token = signup("tanaka");
        jdbc.update("DELETE FROM users");

        expectError(mockMvc.perform(auth(get("/api/auth/me"), token)), 401, "UNAUTHORIZED");
    }
}
