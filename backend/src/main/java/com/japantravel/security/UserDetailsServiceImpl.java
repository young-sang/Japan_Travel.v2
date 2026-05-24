package com.japantravel.security;

import com.japantravel.dto.Dtos.User;
import com.japantravel.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository users;
    public UserDetailsServiceImpl(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        String hash = users.findPasswordHash(username)
                .orElseThrow(() -> new UsernameNotFoundException("비밀번호 해시가 없습니다"));
        return new AppUserPrincipal(u.id(), u.username(), hash, u.nickname(), u.role());
    }
}
