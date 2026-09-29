# REST API

Japan Travel v2 백엔드 — Spring Boot 3.3 / Java 17 / MySQL 8, `greenfield` 브랜치.
**지금 실제로 동작하는 엔드포인트만** 적는다. 아직 만들지 않은 도메인은 맨 아래 "예정" 에만 둔다.

- 에러 코드 전체 목록 → [ERRORS.md](ERRORS.md)
- 테이블 → [SCHEMA.md](SCHEMA.md)
- 결정 배경 → [DECISIONS.md](DECISIONS.md)

---

## 공통 규칙

### 응답 봉투 (D-035)

성공이든 에러든 모든 응답은 `ApiResponse` 에 담긴다. 키는 항상 세 개이고 빈 쪽은 `null` 이다.
상태 코드는 본문에 싣지 않고 HTTP 상태 줄에만 있다.

```json
// 성공
{ "success": true,  "data": { ... },  "error": null }

// 에러
{ "success": false, "data": null,
  "error": { "code": "DESTINATION_NOT_FOUND", "message": "여행지를 찾을 수 없습니다" } }
```

프론트는 `error.code` 로 분기한다 (D-034). 코드별 설명은 [ERRORS.md](ERRORS.md).

### 인증 (D-033)

- JWT Access 토큰 하나(24시간). 가입·로그인 응답의 `data.token` 을 받아
  `Authorization: Bearer <token>` 헤더로 보낸다.
- **로그아웃 API 는 없다.** 프론트가 토큰을 버리는 것이 로그아웃이다. 서버는 토큰을 무효화하지 않는다.
- 기본은 **닫혀 있다.** 아래 표에서 🌐 인 경로만 토큰 없이 열리고, 나머지는 전부 🔑 이다
  (`SecurityConfig` 의 `anyRequest().authenticated()`).
- 토큰이 없거나 · 위조 · 만료면 `401 UNAUTHORIZED`. 로그인 전에는 없는 경로·틀린 메서드도
  404/405 가 아니라 401 이 나간다 ([ERRORS.md](ERRORS.md) 의 `UNAUTHORIZED` 항목).

권한 표기: 🌐 공개 · 🔑 로그인 필요

---

## 인증 — `AuthController` (`/api/auth`)

| 메서드 | 경로 | 권한 | 성공 | 설명 |
|---|---|---|---|---|
| POST | `/api/auth/signup` | 🌐 | 201 | 회원가입. 가입과 동시에 토큰을 준다 |
| POST | `/api/auth/login` | 🌐 | 200 | 로그인 |
| GET | `/api/auth/me` | 🔑 | 200 | 토큰 주인의 정보 |

**요청**

```json
// POST /api/auth/signup — SignupRequest
{ "username": "tanaka", "password": "pw1234", "nickname": "다나카" }

// POST /api/auth/login — LoginRequest
{ "username": "tanaka", "password": "pw1234" }
```

**응답 `data`**

```json
// signup · login — AuthResponse
{ "token": "eyJhbGciOi...",
  "user": { "id": 1, "username": "tanaka", "nickname": "다나카", "role": "USER" } }

// me — UserResponse
{ "id": 1, "username": "tanaka", "nickname": "다나카", "role": "USER" }
```

**에러** — `USERNAME_TAKEN`(409, signup) · `LOGIN_FAILED`(401, login) · `UNAUTHORIZED`(401, me)

---

## 여행지 — `DestinationController` (`/api/destinations`)

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/destinations?prefecture=` | 🌐 | 목록. 최신순(`id` 내림차순) |
| GET | `/api/destinations/{id}` | 🌐 | 상세 |

- `prefecture` — 선택. 현 **이름**(예: `교토부`). 없는 이름이면 `404 PREFECTURE_NOT_FOUND`,
  실재하는 현인데 0건이면 빈 목록 `[]`.
- 태그 필터(`?tag=`)는 1차에서 뺐다 (D-024).

**응답 `data`** — 목록은 배열, 상세는 객체 하나. 둘 다 `DestinationResponse` 이다.

```json
{ "id": 12, "name": "...", "prefecture": "교토부",
  "description": "...", "lat": 34.99, "lng": 135.78,
  "imagePath": "...", "createdAt": "2026-09-22T10:00:00" }
```

**에러** — `PREFECTURE_NOT_FOUND`(404) · `DESTINATION_NOT_FOUND`(404) · `TYPE_MISMATCH`(400, `id` 가 숫자 아님)

---

## 축제 — `FestivalController` (`/api/festivals`)

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/festivals?prefecture=&month=` | 🌐 | 목록. 월 오름차순, 같은 월은 최신순 |
| GET | `/api/festivals/{id}` | 🌐 | 상세 |

- `prefecture` — 선택. 규칙은 여행지와 같다.
- `month` — 선택. 1~12. 범위 밖이면 `400 INVALID_MONTH`, 숫자가 아니면 `400 TYPE_MISMATCH`.
- 두 필터는 함께 쓸 수 있다 (AND).

**응답 `data`** — `FestivalResponse`

```json
{ "id": 3, "name": "...", "prefecture": "교토부",
  "month": 7, "dateText": "7월 중",
  "description": "...", "lat": 35.00, "lng": 135.77,
  "imagePath": "...", "createdAt": "2026-09-22T10:00:00" }
```

**에러** — `PREFECTURE_NOT_FOUND`(404) · `FESTIVAL_NOT_FOUND`(404) · `INVALID_MONTH`(400) · `TYPE_MISMATCH`(400)

---

## 예정 (아직 없음)

[재작성 설계 문서](superpowers/specs/2026-09-17-greenfield-rebuild-design.md) 의 Task 순서를 따른다.
경로와 모양은 각 Task 를 시작할 때 설계 스펙에서 정하고, **구현이 끝나면 위로 옮긴다.**

| Task | 도메인 | 대략의 범위 |
|---|---|---|
| 3 | `favorite` · `review` · `history` | 즐겨찾기 · 리뷰 · 방문기록 |
| 4 | `course` | 코스 목록 · 상세 · 사용자 코스 CRUD, 소유권 판정 |
| 5 | `post` | 게시판 글 + 댓글 |
| 6 | `search` | 통합 검색 |
| — | destination 제안·승인 | 인증 이후로 미룸 (D-030) |

1차에서 뺀 것 — 날씨·환율 프록시(D-032), 관리자 API 전부(D-009).
