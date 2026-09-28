package com.japantravel.user.dto;

import com.japantravel.user.entity.User;

//  password_hash · created_at 은 내보내지 않는다.
public record UserResponse(
        Long id,
        String username,
        String nickname,
        String role
) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getNickname(),
                u.getRole()
        );
    }
}
