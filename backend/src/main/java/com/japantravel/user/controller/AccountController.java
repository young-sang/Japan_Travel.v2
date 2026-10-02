package com.japantravel.user.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.user.dto.PasswordChangeRequest;
import com.japantravel.user.dto.ProfileUpdateRequest;
import com.japantravel.user.dto.UserResponse;
import com.japantravel.user.dto.WithdrawRequest;
import com.japantravel.user.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//  계정 관리 (D-056). 전부 로그인 필요 — SecurityConfig 의 anyRequest().authenticated() 가 막는다.
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PatchMapping("/profile")
    public ApiResponse<UserResponse> updateProfile(@AuthenticationPrincipal Long userId,
                                                   @RequestBody ProfileUpdateRequest req) {
        return ApiResponse.ok(accountService.updateProfile(userId, req));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal Long userId,
                                           @RequestBody PasswordChangeRequest req) {
        accountService.changePassword(userId, req);
        return ApiResponse.ok();
    }

//  본문에 비밀번호를 싣는다. 토큰만으로 계정을 지울 수 없게 한다.
    @DeleteMapping
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal Long userId,
                                     @RequestBody WithdrawRequest req) {
        accountService.withdraw(userId, req);
        return ApiResponse.ok();
    }
}
