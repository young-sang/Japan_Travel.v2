package com.japantravel.user.dto;

public record PasswordChangeRequest(
        String currentPassword,
        String newPassword
) {
}
