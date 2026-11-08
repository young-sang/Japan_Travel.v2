package com.japantravel.user.service;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.user.dto.PasswordChangeRequest;
import com.japantravel.user.dto.ProfileUpdateRequest;
import com.japantravel.user.dto.UserResponse;
import com.japantravel.user.dto.WithdrawRequest;
import com.japantravel.user.entity.User;
import com.japantravel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse updateProfile(Long userId, ProfileUpdateRequest req) {
        User user = find(userId);
        user.changeNickname(req.nickname());
        return UserResponse.from(user);
    }

    public void changePassword(Long userId, PasswordChangeRequest req) {
        User user = find(userId);
        checkPassword(user, req.currentPassword());
        user.changePasswordHash(passwordEncoder.encode(req.newPassword()));
    }

//  찜 · 리뷰 · 코스 · 기록은 FK ON DELETE CASCADE 가 지운다.
    public void withdraw(Long userId, WithdrawRequest req) {
        User user = find(userId);
        checkPassword(user, req.password());
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public void checkPassword(Long userId, String raw) {
        checkPassword(find(userId), raw);
    }

//  토큰은 유효한데 사용자가 없으면 AuthService.me() 와 같이 401.
    private User find(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
    }

//  401 이 아니라 400 — 401 이면 프론트가 로그인이 풀린 것으로 읽는다 (D-056).
    private void checkPassword(User user, String raw) {
        if (raw == null || !passwordEncoder.matches(raw, user.getPasswordHash())) {
            throw new ApiException(ErrorCode.WRONG_PASSWORD);
        }
    }
}
