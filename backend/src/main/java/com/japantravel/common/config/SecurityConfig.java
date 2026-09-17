package com.japantravel.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 임시 설정 - Task 2(user 도메인)에서 다시 쓴다.
 *
 * <p>spring-boot-starter-security 가 의존성에 있으므로 이 빈이 없으면 자동설정이
 * HTTP Basic 을 켜고 모든 엔드포인트가 401 이 된다. 지금은 인증 기능 자체가 없으므로
 * 전부 열어두고, 로그인/세션/권한은 Task 2 에서 만든다.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(c -> {})                      // CORS 는 WebConfig 가 담당
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .formLogin(f -> f.disable())
            .httpBasic(b -> b.disable())
            .logout(l -> l.disable());
        return http.build();
    }
}
