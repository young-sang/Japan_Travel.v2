package com.japantravel.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    private String nickname;

    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

//  가입 경로는 하나뿐이라 role 은 항상 USER 로 시작한다.
//  해시는 호출하는 쪽(AuthService)이 만들어 넘긴다 — 엔티티는 PasswordEncoder 를 모른다.
    public User(String username, String passwordHash, String nickname) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.role = "USER";
        this.createdAt = LocalDateTime.now();
    }
}
