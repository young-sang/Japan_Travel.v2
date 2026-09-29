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

## 즐겨찾기 — `FavoriteController` (`/api/favorites`)

설계: [2026-09-29-favorite-api-design.md](superpowers/specs/2026-09-29-favorite-api-design.md) · 테이블 분리 D-022 · D-036

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/favorites` | 🔑 | 내 즐겨찾기. 각 배열은 최근에 추가한 순서 |
| POST | `/api/favorites/destinations/{id}` | 🔑 | 여행지 즐겨찾기 추가 |
| DELETE | `/api/favorites/destinations/{id}` | 🔑 | 여행지 즐겨찾기 삭제 |
| POST | `/api/favorites/festivals/{id}` | 🔑 | 축제 즐겨찾기 추가 |
| DELETE | `/api/favorites/festivals/{id}` | 🔑 | 축제 즐겨찾기 삭제 |

- **추가·삭제는 멱등이다.** 이미 있는 것을 다시 추가해도, 없는 것을 삭제해도 `200` + `data: null`.
  그래서 추가도 201 이 아니라 200 이다.
- 요청 본문은 없다.
- 삭제는 대상이 실재하는지 확인하지 않는다.

**응답 `data`** — `GET` 만 데이터가 있다. 요소는 위의 `DestinationResponse` · `FestivalResponse` 그대로다.

```json
{ "destinations": [ { "id": 12, "name": "...", "prefecture": "교토부", ... } ],
  "festivals":    [ { "id": 3,  "name": "...", "month": 7, ... } ] }
```

**에러** — `DESTINATION_NOT_FOUND` · `FESTIVAL_NOT_FOUND`(404, 추가할 대상이 없음) ·
`UNAUTHORIZED`(401, 토큰 없음 또는 토큰의 사용자가 삭제됨) · `TYPE_MISMATCH`(400)

---

## 리뷰 — `ReviewController` (대상 아래 중첩)

설계: [2026-09-29-review-api-design.md](superpowers/specs/2026-09-29-review-api-design.md) · 경로 D-037 · 중복 허용 D-013

| 메서드 | 경로 | 권한 | 성공 | 설명 |
|---|---|---|---|---|
| GET | `/api/destinations/{id}/reviews` | 🌐 | 200 | 여행지의 리뷰 목록. 최신순 |
| POST | `/api/destinations/{id}/reviews` | 🔑 | 201 | 리뷰 작성 |
| PUT | `/api/destinations/{id}/reviews/{reviewId}` | 🔑 작성자 | 200 | 리뷰 수정 (두 필드 모두 덮어씀) |
| DELETE | `/api/destinations/{id}/reviews/{reviewId}` | 🔑 작성자 | 200 | 리뷰 삭제. 멱등 아님 — 두 번째는 404 |

`/api/festivals/{id}/reviews...` 도 같은 4개.

- 즐겨찾기(`/api/favorites/...`)와 경로 모양이 다른 이유는 D-037 — 리뷰 목록은 공개라 대상 아래에 두면
  기존 공개 규칙(`GET /api/destinations/**`)에 그대로 들어간다.
- 리뷰 id 는 대상 종류별로 따로 매겨진다. 경로의 대상에 딸리지 않은 리뷰 id 는 404 다.
- 한 사용자가 같은 대상에 여러 개 쓸 수 있다.

**요청** — `ReviewRequest` (POST · PUT). `comment` 는 선택.

```json
{ "rating": 5, "comment": "..." }
```

작성자와 대상은 본문에서 받지 않는다 — 작성자는 토큰, 대상은 경로에서 온다. 본문에 넣어도 무시된다.

**응답 `data`** — `ReviewResponse` (목록은 배열, POST · PUT 은 하나, DELETE 는 `null`)

```json
{ "id": 7, "userId": 1, "nickname": "다나카",
  "rating": 5, "comment": "...",
  "createdAt": "2026-09-29T20:00:00", "updatedAt": null }
```

- `userId` — 프론트가 `/api/auth/me` 의 `id` 와 비교해 내 리뷰에만 수정·삭제 버튼을 보인다.
- `updatedAt` — 수정한 적이 없으면 `null`.

**에러**
- `INVALID_RATING`(400, 별점 없음 또는 1~5 밖) · `MALFORMED_REQUEST`(400, 별점이 숫자 아님)
- `DESTINATION_NOT_FOUND` · `FESTIVAL_NOT_FOUND`(404, 목록 · 작성에서 대상 없음)
- `REVIEW_NOT_FOUND`(404, 수정 · 삭제에서 리뷰 없음 또는 대상 불일치)
- `FORBIDDEN`(403, 남의 리뷰) · `UNAUTHORIZED`(401)
- 판정 순서: 별점 → 리뷰 존재 → 작성자

---

## 예정 (아직 없음)

[재작성 설계 문서](superpowers/specs/2026-09-17-greenfield-rebuild-design.md) 의 Task 순서를 따른다.
경로와 모양은 각 Task 를 시작할 때 설계 스펙에서 정하고, **구현이 끝나면 위로 옮긴다.**

| Task | 도메인 | 대략의 범위 |
|---|---|---|
| 보류 | `history` | 최근 본 장소. Task 4 ~ 6 이후로 미룸 (D-038) |
| 4 | `course` | 코스 목록 · 상세 · 사용자 코스 CRUD, 소유권 판정 |
| 5 | `post` | 게시판 글 + 댓글 |
| 6 | `search` | 통합 검색 |
| — | destination 제안·승인 | 인증 이후로 미룸 (D-030) |

1차에서 뺀 것 — 날씨·환율 프록시(D-032), 관리자 API 전부(D-009).
