package com.japantravel.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

//  @Component 를 달지 않는다. 달면 Spring Boot 가 서블릿 필터로도 따로 등록해
//  Security 체인 밖에서 한 번 더 걸린다. SecurityConfig 에서 직접 new 해서 체인에만 넣는다.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(PREFIX)) {
            try {
                Claims claims = jwtProvider.parse(header.substring(PREFIX.length()));
                Long userId = Long.valueOf(claims.getSubject());
                String role = claims.get("role", String.class);

//              principal 은 userId 하나. 컨트롤러에서 @AuthenticationPrincipal Long userId 로 받는다.
                var authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException e) {
//              여기서 401 을 쓰지 않고 익명으로 통과시킨다. 공개 경로에 만료 토큰을 붙여 와도
//              조회는 되어야 하므로, 막을지는 SecurityConfig 의 URL 규칙이 정한다.
            }
        }

        chain.doFilter(request, response);
    }
}
