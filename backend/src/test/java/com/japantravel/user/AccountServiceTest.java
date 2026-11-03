package com.japantravel.user;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.user.dto.PasswordChangeRequest;
import com.japantravel.user.dto.WithdrawRequest;
import com.japantravel.user.entity.User;
import com.japantravel.user.repository.UserRepository;
import com.japantravel.user.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.japantravel.support.Fixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

//  본인 확인 실패는 401 이 아니라 400 WRONG_PASSWORD (D-056)
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AccountService accountService;

    private static void assertCode(Runnable call, ErrorCode code) {
        assertThatThrownBy(call::run).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(code);
    }

    @Test
    void 현재_비밀번호가_틀리거나_없으면_바꾸지_않는다() {
        User me = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(passwordEncoder.matches("wrong", me.getPasswordHash())).thenReturn(false);

        assertCode(() -> accountService.changePassword(1L, new PasswordChangeRequest("wrong", "new")),
                ErrorCode.WRONG_PASSWORD);
        assertCode(() -> accountService.changePassword(1L, new PasswordChangeRequest(null, "new")),
                ErrorCode.WRONG_PASSWORD);
        assertThat(me.getPasswordHash()).isEqualTo("hash-1");
    }

    @Test
    void 비밀번호를_새_해시로_바꾼다() {
        User me = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(passwordEncoder.matches("pw", "hash-1")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("new-hash");

        accountService.changePassword(1L, new PasswordChangeRequest("pw", "new"));

        assertThat(me.getPasswordHash()).isEqualTo("new-hash");
    }

    @Test
    void 탈퇴는_비밀번호가_맞을_때만_지운다() {
        User me = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(passwordEncoder.matches("wrong", "hash-1")).thenReturn(false);
        when(passwordEncoder.matches("pw", "hash-1")).thenReturn(true);

        assertCode(() -> accountService.withdraw(1L, new WithdrawRequest("wrong")), ErrorCode.WRONG_PASSWORD);
        verify(userRepository, never()).delete(any());

        accountService.withdraw(1L, new WithdrawRequest("pw"));
        verify(userRepository).delete(me);
    }

    @Test
    void 사용자가_지워졌으면_비밀번호를_보기_전에_UNAUTHORIZED() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertCode(() -> accountService.withdraw(1L, new WithdrawRequest("pw")), ErrorCode.UNAUTHORIZED);
        verifyNoInteractions(passwordEncoder);
    }
}
