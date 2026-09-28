package com.japantravel.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.common.web.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtProvider jwtProvider,
                                           ObjectMapper objectMapper) throws Exception {
        http
//              쿠키로 인증하지 않으므로 CSRF 대상이 아니다
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/destinations/**", "/api/festivals/**").permitAll()
//                      처리되지 않은 예외는 /error 로 포워드된다. 이걸 열지 않으면 500 이 401 로 바뀌어 나간다.
                        .requestMatchers("/error").permitAll()
//                      기본을 닫는다. 새 도메인은 공개할 것만 위에 추가한다.
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(unauthorizedEntryPoint(objectMapper)))
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//  필터 단계의 401 은 @RestControllerAdvice 까지 오지 않으므로 여기서 같은 모양의 JSON 을 쓴다.
    private AuthenticationEntryPoint unauthorizedEntryPoint(ObjectMapper objectMapper) {
        return (request, response, ex) -> {
            response.setStatus(ErrorCode.UNAUTHORIZED.getStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ErrorCode.UNAUTHORIZED));
        };
    }
}
