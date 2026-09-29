# favorite API 설계

2026-09-29 · 확정 · greenfield 브랜치 · Task 3 의 첫 번째 (favorite → review → history)

로그인한 사용자가 여행지·축제를 즐겨찾기에 넣고 빼고, 자기 목록을 본다.

관련 결정: [D-022](../../DECISIONS.md#d-022--places-를-destinations-와-festivals-로-다시-나눈다)
(대상별 테이블 분리) · [D-036](../../DECISIONS.md#d-036--활동-테이블은-6개-키는-id--unique)
(테이블 6개, 키는 id + UNIQUE) · [D-035](../../DECISIONS.md#d-035--모든-응답을-apiresponse-봉투로-감싼다)
(DELETE 는 200 + `data: null`)

---

## 1. 사용자가 정한 것 (2026-09-29)

| 항목 | 결정 | 기각한 대안 |
|---|---|---|
| 경로 | **`/api/favorites/{destinations\|festivals}/{id}`** — 컨트롤러 하나에 모은다 | `/api/destinations/{id}/favorite` — `GET /api/destinations/**` 공개 규칙과 경로가 겹친다 · 옛 방식(본문에 `type`) — 문자열 분기와 잘못된 type 에러가 생긴다 |
| 목록 모양 | **`{ destinations: [...], festivals: [...] }`** — 기존 DTO 재사용 | 한 배열 + `type` — 두 테이블을 합쳐 정렬하는 코드가 필요 · 대상별 경로 2개 |
| 중복 추가 · 없는 삭제 | **멱등. 둘 다 200** | 409 / 404 로 알림 — 프론트가 두 에러를 처리해야 한다 |

---

## 2. 범위

| 메서드 | 경로 | 권한 | 성공 |
|---|---|---|---|
| GET | `/api/favorites` | 로그인 | 200 |
| POST | `/api/favorites/destinations/{id}` | 로그인 | 200 |
| DELETE | `/api/favorites/destinations/{id}` | 로그인 | 200 |
| POST | `/api/favorites/festivals/{id}` | 로그인 | 200 |
| DELETE | `/api/favorites/festivals/{id}` | 로그인 | 200 |

`SecurityConfig` 는 고치지 않는다. `anyRequest().authenticated()` 가 이미 막는다.

넣지 않는 것 — "이 여행지를 내가 즐겨찾기했는가" 단건 조회, 즐겨찾기 수 집계, 페이징.
프론트 정렬(Task 7)에서 필요해지면 그때 추가한다.

---

## 3. 스키마 — 테이블 2개 추가

```sql
CREATE TABLE IF NOT EXISTS favorite_destinations (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT   NOT NULL,
  destination_id BIGINT   NOT NULL,
  created_at     DATETIME NOT NULL,
  UNIQUE (user_id, destination_id),
  FOREIGN KEY (user_id)        REFERENCES users(id)        ON DELETE CASCADE,
  FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
);
-- favorite_festivals 는 destination_id → festival_id, destinations → festivals 만 다르다
```

- **`ON DELETE CASCADE`** — 사용자나 대상이 지워지면 즐겨찾기도 DB 가 지운다. 옛 코드에서
  자바가 손으로 지우던 것을 DB 가 맡는다 (D-011 의 문제의식).
- **`UNIQUE (user_id, destination_id)`** — 같은 대상을 두 번 넣지 못한다 (D-036).
  이 UNIQUE 가 `user_id` 로 시작하므로 "내 목록" 조회의 인덱스 역할도 한다.
- **기존 테이블을 바꾸지 않으므로 DB 를 지울 필요가 없다.** `IF NOT EXISTS` 로 다음 기동 때 추가된다.

---

## 4. 엔드포인트 계약

### GET /api/favorites

내 즐겨찾기. 각 배열은 **최근에 추가한 순서**(`favorite.id DESC`).

```json
{ "success": true,
  "data": { "destinations": [ DestinationResponse, ... ],
            "festivals":    [ FestivalResponse, ... ] },
  "error": null }
```

- 요소는 기존 `DestinationResponse` · `FestivalResponse` 를 그대로 쓴다. 즐겨찾기한 시각은 싣지 않는다.
- 비어 있으면 `{ "destinations": [], "festivals": [] }` — 에러가 아니다.

### POST /api/favorites/destinations/{id}

| 상태 | 조건 | 본문 |
|---|---|---|
| 200 | 추가했거나 **이미 있음** | `data: null` |
| 404 | 그 id 의 여행지가 없음 | `DESTINATION_NOT_FOUND` |
| 401 | 토큰 없음 · 무효, 또는 토큰의 사용자가 삭제됨 | `UNAUTHORIZED` |
| 400 | `id` 가 숫자가 아님 | `TYPE_MISMATCH` |

- 201 이 아니라 200 인 이유 — 멱등이라 "새로 만들었는지" 가 호출마다 다르다. 응답을 하나로 둔다.
- 사용자가 삭제된 경우를 401 로 두는 것은 `AuthService.me` 와 같은 규칙이다. 확인하지 않으면
  FK 위반으로 500 이 된다.
- 동시에 같은 대상을 두 번 추가하면 둘 다 존재 확인을 통과하고 한쪽이 UNIQUE 에 걸려 500 이 된다.
  가입의 동시 요청과 같이 따로 처리하지 않는다 (ERRORS.md `USERNAME_TAKEN` 참조).

### DELETE /api/favorites/destinations/{id}

| 상태 | 조건 | 본문 |
|---|---|---|
| 200 | 지웠거나 **원래 없음** | `data: null` |
| 401 | 토큰 없음 · 무효 | `UNAUTHORIZED` |
| 400 | `id` 가 숫자가 아님 | `TYPE_MISMATCH` |

- 대상(여행지)이 실재하는지 확인하지 않는다. 멱등 삭제에서 "없는 대상" 과 "즐겨찾기 안 한 대상" 은
  결과가 같다.

### festivals

`/api/favorites/festivals/{id}` 는 위와 같다. 404 만 `FESTIVAL_NOT_FOUND`.

**새 `ErrorCode` 는 없다.**

---

## 5. 계층

```
favorite/
├── controller/FavoriteController.java
├── service/FavoriteService.java
├── repository/FavoriteDestinationRepository.java
├── repository/FavoriteFestivalRepository.java
├── entity/FavoriteDestination.java
├── entity/FavoriteFestival.java
└── dto/FavoriteListResponse.java      record(List<DestinationResponse>, List<FestivalResponse>)
```

- **엔티티** — `@ManyToOne(LAZY) User user` · `@ManyToOne(LAZY) Destination destination` ·
  `createdAt`. 생성자는 `(User, Destination)` 하나, `createdAt` 은 생성자에서 채운다 (`User` 와 같은 방식).
- **리포지토리** — `findByUserIdOrderByIdDesc` 에 `@EntityGraph({"destination", "destination.prefecture"})`
  (목록 응답이 현 이름까지 쓰므로 둘 다 필요) · `existsByUserIdAndDestinationId` ·
  `deleteByUserIdAndDestinationId`.
- **서비스** — 대상 도메인의 리포지토리(`DestinationRepository` · `FestivalRepository`)를 읽기 전용으로
  쓴다. 설계 문서의 "favorite → place 읽기 전용 참조 허용" 에 해당한다.
- **컨트롤러** — `@AuthenticationPrincipal Long userId` 로 사용자를 받는다 (`AuthController.me` 와 같음).
- **`ApiResponse.ok()`** (인자 없음) 를 `common/web/ApiResponse` 에 추가한다. D-035 가 "첫 DELETE 를
  만들 때 추가한다" 고 적어둔 그것이다.

---

## 6. 검증 (스모크)

1. 가입 → 토큰
2. `GET /api/favorites` → 두 배열 모두 `[]`
3. `POST /api/favorites/destinations/1` 두 번 → 둘 다 200, 목록에 1건
4. `POST /api/favorites/festivals/1` → 목록 `festivals` 에 1건
5. `POST /api/favorites/destinations/999999` → 404 `DESTINATION_NOT_FOUND`
6. `DELETE /api/favorites/destinations/1` 두 번 → 둘 다 200, 목록에서 사라짐
7. 토큰 없이 `GET /api/favorites` → 401

**결과 (2026-09-29)** — 1 · 2 · 5 · 6 · 7 통과. `id=abc` → 400 `TYPE_MISMATCH` 도 확인.
**3 · 4 는 확인하지 못했다.** `destinations` · `festivals` 가 비어 있어(시드 전, D-021) 추가할 대상이
없었다. 시드를 넣은 뒤 다시 돌린다 — 추가 · 중복 추가 · 목록 반영 · 삭제 후 사라짐.
