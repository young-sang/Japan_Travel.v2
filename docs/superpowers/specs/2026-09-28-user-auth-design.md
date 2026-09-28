# user 도메인 · JWT 인증 설계

2026-09-28 · 확정 · greenfield 브랜치 · Task 2

가입 · 로그인 · 내 정보를 만들고, Spring Security 에 JWT 검증을 붙인다. 이후 도메인
(favorite · review · history · course · post)은 전부 여기서 만든 "로그인한 사용자" 를
받아 쓴다.

관련 결정: [D-033](../../DECISIONS.md#d-033--인증은-jwt--spring-security-로-한다) ·
[D-019](../../DECISIONS.md#d-019--config-클래스와-security-의존성을-지운다) (의존성과 설정을 같이 넣는다) ·
[D-008](../../DECISIONS.md#d-008--프론트를-백엔드에-맞춘다-전제-3개-해제) (프론트를 백엔드에 맞춘다)

---

## 1. 사용자가 정한 것 (2026-09-28)

| 항목 | 결정 | 기각한 대안 |
|---|---|---|
| 인증 방식 | **JWT** | 세션 쿠키 — 옛 방식이라 프론트는 그대로 쓸 수 있었다 |
| Security | **Spring Security 전체** (`SecurityConfig` + 필터 체인) | `HttpSession` 을 직접 다루는 방식 — 엔드포인트마다 로그인 검사를 따로 넣어야 한다 |
| 토큰 전달 | **`Authorization: Bearer` 헤더**, 프론트는 `localStorage` 에 보관 | HttpOnly 쿠키 — 서버가 쿠키를 읽는 필터를 따로 써야 하고, 결국 세션과 모양이 같아진다 |
| 만료 | **Access 토큰 하나, 24시간** | Access + Refresh — 저장 테이블 · 재발급 API · 프론트 재시도 로직이 추가된다 |
| 라이브러리 | **jjwt 0.12** | `oauth2-resource-server` — 코드는 짧지만 내부 동작이 가려져 발표에서 설명하기 어렵다 |

---

## 2. 범위

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| POST | `/api/auth/signup` | 공개 | 가입. 성공하면 바로 로그인된 상태로 토큰을 준다 |
| POST | `/api/auth/login` | 공개 | 로그인 |
| GET | `/api/auth/me` | 로그인 | 내 정보 |

**뺀 것**
- **`POST /api/auth/logout`** — 서버에 지울 상태가 없다. 로그아웃은 프론트가 토큰을
  버리는 것이 전부다. 204 만 돌려주는 빈 엔드포인트를 두는 안은 기각했다. 하는 일
  없이 "서버가 로그아웃을 처리한다" 는 오해만 만든다. 프론트의 `api.logout()` 호출은
  Task 7 에서 지운다.
- 관리자 사용자 관리 (`/api/admin/users/**`) — D-009.
- 비밀번호 변경 · 회원 탈퇴 · 닉네임 수정 — 옛 API 에도 없었다.
- 입력 검증 (길이 · 형식) — 프로젝트 `CLAUDE.md`.
- 테스트 코드 — destination · festival 과 같이 `curl` 스모크로 확인한다.

---

## 3. 스키마 — `users` 추가

```sql
CREATE TABLE IF NOT EXISTS users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(50)  NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  nickname      VARCHAR(50)  NOT NULL,
  role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
  created_at    DATETIME     NOT NULL,
  CHECK (role IN ('USER', 'ADMIN'))
);
```

- `CREATE TABLE IF NOT EXISTS` 로 **추가만** 한다. 기존 테이블은 건드리지 않으므로 DB 를
  비우지 않아도 된다.
- `password_hash VARCHAR(100)` — BCrypt 해시는 60자로 고정이다. 여유를 둔다.
- `role` 을 남긴 이유 — D-009 에서 이미 "컬럼 하나는 비용이 아니다" 로 정했다. 가입하면
  항상 `USER` 이고, `ADMIN` 은 1차에서 만드는 경로가 없다.
- **시드 없음** — 시연 계정은 가입 API 로 만든다. `password_hash` 를 SQL 에 직접 박으면
  BCrypt 해시를 손으로 만들어야 한다.

---

## 4. 엔드포인트 계약

### 공통 응답 DTO

```java
public record UserResponse(Long id, String username, String nickname, String role) {
    public static UserResponse from(User u) { ... }
}

public record AuthResponse(String token, UserResponse user) { }
```

`password_hash` 와 `created_at` 은 응답에 넣지 않는다.

### POST /api/auth/signup

요청 `{"username", "password", "nickname"}` → `SignupRequest` record

| 상태 | 조건 | 본문 |
|---|---|---|
| 201 | 가입 성공 | `AuthResponse` |
| 409 | `username` 중복 | `{"message": "...", "code": "USERNAME_TAKEN"}` |

- 가입 직후 토큰을 준다. 옛 프론트가 가입 후 곧바로 `setUser(u)` 를 하던 흐름과 같다.
- 중복은 **Service 에서 `existsByUsername` 로 먼저 확인**한다. UNIQUE 위반 예외를 잡아서
  바꾸는 방식보다 흐름이 코드에 그대로 드러난다. 동시에 같은 이름으로 두 번 가입하는
  경우는 DB UNIQUE 가 막고 500 이 난다. 이 경우는 따로 처리하지 않는다.

### POST /api/auth/login

요청 `{"username", "password"}` → `LoginRequest` record

| 상태 | 조건 | 본문 |
|---|---|---|
| 200 | 성공 | `AuthResponse` |
| 401 | 아이디가 없거나 비밀번호가 틀림 | `{"message": "아이디 또는 비밀번호가 올바르지 않습니다"}` |

**두 경우를 같은 메시지로 묶는다.** 나누면 "이 아이디는 가입돼 있다" 를 알려주게 된다.
이건 보안 기능을 새로 넣는 것이 아니라 메시지를 하나로 쓰는 것뿐이라 비용이 없다.

### GET /api/auth/me

| 상태 | 조건 | 본문 |
|---|---|---|
| 200 | 유효한 토큰 | `UserResponse` |
| 401 | 토큰 없음 · 만료 · 위조 | `{"message": "로그인이 필요합니다"}` |

토큰의 `sub`(userId) 로 DB 에서 다시 읽는다. 토큰이 발급된 뒤 사용자가 지워졌으면 404 가
아니라 **401** 로 본다 — 프론트 입장에서는 "로그인이 풀린 것" 이다.

### 401 을 JSON 으로 맞춘다

Spring Security 는 인증 실패 시 **필터 단계에서** 응답을 끝내므로 `@RestControllerAdvice`
까지 오지 않는다. 그래서 두 군데서 401 을 만든다.

| 발생 위치 | 처리 |
|---|---|
| 필터 체인 (토큰 없이 보호된 경로 접근) | `AuthenticationEntryPoint` 를 `SecurityConfig` 에 등록해 `{"message"}` 로 쓴다 |
| 컨트롤러 · 서비스 (로그인 실패) | `common/error/UnauthorizedException` 을 새로 만들고 `ApiExceptionHandler` 에 401 핸들러를 추가한다 |

---

## 5. JWT

| 항목 | 값 |
|---|---|
| 알고리즘 | HS256 (대칭키) |
| `sub` | userId (문자열) |
| `role` | `USER` / `ADMIN` (커스텀 클레임) |
| `iat` · `exp` | 발급 시각 · 발급 + 24시간 |
| 비밀키 | `application.yml` 의 `jwt.secret` (32바이트 이상), 기본값은 개발용 문자열 |
| 만료 | `jwt.expiration: 24h` (`Duration`) |

**`username` · `nickname` 을 토큰에 넣지 않는다.** 닉네임이 나중에 바뀌면 토큰의 값이
낡는다. 필요하면 `/me` 로 읽는다.

**`role` 은 토큰에 넣는다.** 요청마다 DB 를 읽지 않고 권한을 판정하기 위해서다. 대가로
역할이 바뀌어도 토큰이 만료될 때까지 반영되지 않는다. 1차에는 역할을 바꾸는 경로가
없으므로 문제가 되지 않는다.

---

## 6. 계층 구성

```
com/japantravel/
├── user/
│   ├── entity/User.java
│   ├── repository/UserRepository.java      findByUsername · existsByUsername
│   ├── dto/  SignupRequest · LoginRequest · UserResponse · AuthResponse
│   ├── service/AuthService.java            signup · login · me
│   └── controller/AuthController.java      /api/auth/**
└── common/
    ├── security/
    │   ├── SecurityConfig.java             필터 체인 · PasswordEncoder 빈
    │   ├── JwtProvider.java                토큰 생성 · 검증 · 클레임 꺼내기
    │   └── JwtAuthenticationFilter.java    OncePerRequestFilter
    └── error/UnauthorizedException.java    (추가)
```

**`security/` 를 `user/` 가 아니라 `common/` 에 둔 이유** — 필터는 모든 요청에 걸리고,
이후 모든 도메인이 이것에 의존한다. `user` 도메인의 일부가 아니다.

### 요청 한 건의 흐름

```
요청 ─→ JwtAuthenticationFilter
          ├ Authorization 헤더 없음 → 그냥 통과 (익명)
          ├ 토큰 검증 실패          → 그냥 통과 (익명)
          └ 검증 성공 → SecurityContext 에 Authentication(principal = userId, ROLE_xxx) 저장
      ─→ SecurityConfig 의 URL 규칙
          ├ 공개 경로 → 컨트롤러
          ├ 익명인데 보호된 경로 → AuthenticationEntryPoint → 401 JSON
          └ 인증됨 → 컨트롤러 (@AuthenticationPrincipal Long userId)
```

**필터가 검증에 실패해도 직접 401 을 쓰지 않는 이유** — 공개 경로(`GET /api/destinations`)
에 만료된 토큰을 붙여 와도 조회는 되어야 한다. 401 판정은 URL 규칙에 맡긴다.

**principal 을 `userId`(Long) 하나로 둔다.** `UserDetails` 를 구현하지 않는다. 이후
도메인에서는 `@AuthenticationPrincipal Long userId` 한 줄로 받는다.

### 로그인을 `AuthenticationManager` 에 맡기지 않는다

Spring Security 정석은 `UserDetailsService` + `AuthenticationManager.authenticate()` 다.
여기서는 **`AuthService` 가 `findByUsername` → `passwordEncoder.matches()` 를 직접 한다.**

| | 직접 비교 (채택) | AuthenticationManager |
|---|---|---|
| 코드 | Service 메서드 하나에 흐름이 다 보인다 | `UserDetailsService` · `UserDetails` 구현이 추가된다 |
| 쓰는 곳 | 로그인 한 곳뿐 | 폼 로그인 · 여러 인증 수단이 있을 때 이득 |

인증 수단이 JWT 하나라 추상화에서 얻는 것이 없다.

### SecurityConfig 규칙

```
csrf            disable     — 쿠키를 쓰지 않으므로 CSRF 대상이 아니다
sessionCreation STATELESS
formLogin · httpBasic disable
permitAll       POST /api/auth/signup, POST /api/auth/login
                GET  /api/destinations/**, GET /api/festivals/**
anyRequest      authenticated
```

- `anyRequest().authenticated()` 로 **기본을 닫는다.** 이후 도메인을 추가할 때 공개할
  것만 열면 된다. 반대로 기본을 열어두면 보호를 빼먹은 엔드포인트가 조용히 공개된다.
- CORS 는 넣지 않는다 — Vite 프록시를 거치므로 같은 출처다 (D-019).

---

## 7. 의존성 변경 — `build.gradle`

```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'   // 주석 해제
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly    'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly    'io.jsonwebtoken:jjwt-jackson:0.12.6'
```

jjwt 는 Spring Boot BOM 에 없어 버전을 직접 적는다.

---

## 8. 구현 단위 (검수 순서)

1. `schema.sql` + `User` + `UserRepository`
2. `build.gradle` (jjwt 만) + `JwtProvider` + `application.yml` 의 `jwt.*`
3. `build.gradle` (security) + `SecurityConfig` + `JwtAuthenticationFilter` +
   `UnauthorizedException` → 이 시점에 기존 destination · festival 조회가 여전히 공개인지 확인.
   security 의존성을 2단계가 아니라 여기서 넣는 이유는 D-019 — 설정 없이 의존성만
   들어가면 자동설정이 모든 엔드포인트를 401 로 막는다.
4. DTO + `AuthService` + `AuthController`
5. 스모크 + 커밋

---

## 9. 확인 방법

| 요청 | 기대 |
|---|---|
| `GET /api/destinations` (토큰 없음) | `200` — 기존 조회가 막히지 않았는가 |
| `POST /signup` 새 아이디 | `201`, `token` + `user.role = USER` |
| `POST /signup` 같은 아이디 | `409 USERNAME_TAKEN` |
| `POST /login` 맞는 비밀번호 | `200`, `token` |
| `POST /login` 틀린 비밀번호 / 없는 아이디 | 둘 다 `401`, 같은 메시지 |
| `GET /me` 토큰 없음 | `401` JSON |
| `GET /me` 정상 토큰 | `200 UserResponse` |
| `GET /me` 끝을 한 글자 바꾼 토큰 | `401` |
| DB 의 `password_hash` | `$2a$` 로 시작하는 60자 |
