package com.japantravel.user;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.common.security.JwtProvider;
import com.japantravel.user.dto.AuthResponse;
import com.japantravel.user.dto.LoginRequest;
import com.japantravel.user.dto.SignupRequest;
import com.japantravel.user.entity.User;
import com.japantravel.user.repository.UserRepository;
import com.japantravel.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.japantravel.support.Fixtures.user;
import static com.japantravel.support.Fixtures.withId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtProvider jwtProvider;

    @InjectMocks AuthService authService;

    private static void assertCode(Runnable call, ErrorCode code) {
        assertThatThrownBy(call::run).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(code);
    }

    @Test
    void 가입은_비밀번호를_해시로_저장하고_USER_로_토큰을_낸다() {
        when(userRepository.existsByUsername("tanaka")).thenReturn(false);
        when(passwordEncoder.encode("pw")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0), 1L));
        when(jwtProvider.createToken(1L, "USER")).thenReturn("token");

        AuthResponse res = authService.signup(new SignupRequest("tanaka", "pw", "다나카"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("hashed");
        assertThat(res.token()).isEqualTo("token");
        assertThat(res.user().role()).isEqualTo("USER");
    }

    @Test
    void 아이디가_있으면_저장하지_않고_USERNAME_TAKEN() {
        when(userRepository.existsByUsername("tanaka")).thenReturn(true);

        assertCode(() -> authService.signup(new SignupRequest("tanaka", "pw", "x")), ErrorCode.USERNAME_TAKEN);
        verify(userRepository, never()).save(any());
    }

    @Test
    void 없는_아이디와_틀린_비밀번호는_같은_LOGIN_FAILED() {
        User tanaka = user(1L);
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("tanaka")).thenReturn(Optional.of(tanaka));
        when(passwordEncoder.matches("wrong", tanaka.getPasswordHash())).thenReturn(false);

        assertCode(() -> authService.login(new LoginRequest("nobody", "pw")), ErrorCode.LOGIN_FAILED);
        assertCode(() -> authService.login(new LoginRequest("tanaka", "wrong")), ErrorCode.LOGIN_FAILED);
    }

    @Test
    void 토큰의_사용자가_지워졌으면_404_가_아니라_UNAUTHORIZED() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertCode(() -> authService.me(1L), ErrorCode.UNAUTHORIZED);
    }
}
