package com.japantravel.user;

import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//  계정 관리 (D-056)
class AccountApiTest extends IntegrationTest {

    @Test
    void 닉네임을_바꾼다() throws Exception {
        String token = signup("tanaka");

        mockMvc.perform(auth(withJson(patch("/api/me/profile"), Map.of("nickname", "새닉")), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("새닉"));
        mockMvc.perform(auth(get("/api/auth/me"), token))
                .andExpect(jsonPath("$.data.nickname").value("새닉"));
    }

    @Test
    void 비밀번호를_바꾸면_새_비밀번호로만_로그인된다() throws Exception {
        String token = signup("tanaka");

        mockMvc.perform(auth(withJson(put("/api/me/password"),
                        Map.of("currentPassword", "pw1234", "newPassword", "new5678")), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());

        mockMvc.perform(withJson(post("/api/auth/login"), Map.of("username", "tanaka", "password", "new5678")))
                .andExpect(status().isOk());
        expectError(mockMvc.perform(withJson(post("/api/auth/login"),
                Map.of("username", "tanaka", "password", "pw1234"))), 401, "LOGIN_FAILED");
    }

    @Test
    void 현재_비밀번호가_틀리면_401_이_아니라_400() throws Exception {
        String token = signup("tanaka");

        expectError(mockMvc.perform(auth(withJson(put("/api/me/password"),
                Map.of("currentPassword", "wrong", "newPassword", "new5678")), token)), 400, "WRONG_PASSWORD");
    }

    @Test
    void 탈퇴하면_계정과_활동이_함께_지워진다() throws Exception {
        String token = signup("tanaka");
        long destinationId = destinationId();
        mockMvc.perform(auth(withJson(post("/api/destinations/{id}/reviews", destinationId),
                Map.of("rating", 5)), token)).andExpect(status().isCreated());
        mockMvc.perform(auth(post("/api/me/favorites/destinations/{id}", destinationId), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(withJson(delete("/api/me"), Map.of("password", "pw1234")), token))
                .andExpect(status().isOk());

        expectError(mockMvc.perform(auth(get("/api/auth/me"), token)), 401, "UNAUTHORIZED");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM review_destinations", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM favorite_destinations", Long.class)).isZero();
    }

    @Test
    void 탈퇴_비밀번호가_틀리면_400_이고_계정은_남는다() throws Exception {
        String token = signup("tanaka");

        expectError(mockMvc.perform(auth(withJson(delete("/api/me"), Map.of("password", "wrong")), token)),
                400, "WRONG_PASSWORD");
        mockMvc.perform(auth(get("/api/auth/me"), token)).andExpect(status().isOk());
    }

    @Test
    void 로그인하지_않으면_401() throws Exception {
        expectError(mockMvc.perform(withJson(patch("/api/me/profile"), Map.of("nickname", "x"))),
                401, "UNAUTHORIZED");
    }
}
