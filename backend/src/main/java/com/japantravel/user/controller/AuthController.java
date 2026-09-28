package com.japantravel.user.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.user.dto.AuthResponse;
import com.japantravel.user.dto.LoginRequest;
import com.japantravel.user.dto.SignupRequest;
import com.japantravel.user.dto.UserResponse;
import com.japantravel.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> signup(@RequestBody SignupRequest req) {
        return ApiResponse.ok(authService.signup(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

//  토큰이 없으면 SecurityConfig 에서 이미 401 로 끝나므로, 여기 도착하면 userId 는 항상 있다.
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(authService.me(userId));
    }
}
