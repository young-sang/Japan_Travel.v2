package com.japantravel.user.dto;

//  탈퇴 확인창 전에 비밀번호만 확인한다 (D-084)
public record PasswordCheckRequest(
        String password
) {
}
