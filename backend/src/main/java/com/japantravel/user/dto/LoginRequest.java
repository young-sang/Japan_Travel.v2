package com.japantravel.user.dto;

public record LoginRequest(
        String username,
        String password
) {
}
