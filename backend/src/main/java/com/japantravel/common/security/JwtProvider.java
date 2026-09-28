package com.japantravel.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final Duration expiration;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration}") Duration expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

//  sub = userId, role = 커스텀 클레임. username · nickname 은 바뀔 수 있어 넣지 않는다.
    public String createToken(Long userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration.toMillis()))
                .signWith(key)
                .compact();
    }

//  서명 불일치 · 만료 · 형식 오류는 JwtException 으로 던진다. 잡는 것은 필터의 몫이다.
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
