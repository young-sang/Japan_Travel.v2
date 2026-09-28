package com.japantravel.user.dto;

//  가입 · 로그인 공용. 프론트는 token 을 저장하고 user 로 화면을 그린다.
public record AuthResponse(
        String token,
        UserResponse user
) {
}
