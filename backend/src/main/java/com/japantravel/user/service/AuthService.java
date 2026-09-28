package com.japantravel.user.service;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.common.security.JwtProvider;
import com.japantravel.user.dto.AuthResponse;
import com.japantravel.user.dto.LoginRequest;
import com.japantravel.user.dto.SignupRequest;
import com.japantravel.user.dto.UserResponse;
import com.japantravel.user.entity.User;
import com.japantravel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponse signup(SignupRequest req) {
//      UNIQUE 위반 예외를 잡아 바꾸지 않고 먼저 확인한다. 동시 가입은 DB UNIQUE 가 막는다 (500).
        if (userRepository.existsByUsername(req.username())) {
            throw new ApiException(ErrorCode.USERNAME_TAKEN);
        }
        User user = userRepository.save(
                new User(req.username(), passwordEncoder.encode(req.password()), req.nickname()));
        return issue(user);
    }

//  아이디가 없는 경우와 비밀번호가 틀린 경우를 같은 메시지로 묶는다.
//  나누면 "이 아이디는 가입돼 있다" 를 알려주게 된다.
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(ErrorCode.LOGIN_FAILED));
        return issue(user);
    }

//  토큰은 유효한데 사용자가 지워졌으면 404 가 아니라 401 — 프론트에게는 "로그인이 풀린 것" 이다.
    public UserResponse me(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
    }

    private AuthResponse issue(User user) {
        return new AuthResponse(jwtProvider.createToken(user.getId(), user.getRole()), UserResponse.from(user));
    }
}
