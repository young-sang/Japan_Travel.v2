package com.japantravel.user.dto;

public record SignupRequest(
        String username,
        String password,
        String nickname
) {
}
