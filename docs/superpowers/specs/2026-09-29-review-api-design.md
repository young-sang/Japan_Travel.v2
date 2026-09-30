# review API 설계

2026-09-29 · 확정 · greenfield 브랜치 · Task 3 의 두 번째 (favorite → **review** → history)

누구나 여행지·축제의 리뷰를 읽고, 로그인한 사용자가 별점과 글을 남기고, 작성자만 고치고 지운다.

관련 결정: [D-022](../../DECISIONS.md#d-022--places-를-destinations-와-festivals-로-다시-나눈다) ·
[D-036](../../DECISIONS.md#d-036--활동-테이블은-6개-키는-id--unique) (대상별 테이블) ·
[D-013](../../DECISIONS.md#d-013--리뷰-중복을-허용한다) (중복 허용) ·
[D-037](../../DECISIONS.md#d-037--리뷰-경로는-대상-아래에-중첩한다-favorite-와-다르게) (경로 중첩)

---

## 1. 사용자가 정한 것 (2026-09-29)

| 항목 | 결정 | 기각한 대안 |
|---|---|---|
| 경로 | **대상 아래 중첩** — `/api/destinations/{id}/reviews` | favorite 처럼 `/api/reviews/...` — 공개 GET 에 `SecurityConfig` 규칙 추가가 필요 |
| 수정 | **넣는다** (PUT) | 빼기 — 고치려면 지우고 다시 써야 한다 |
| 목록 모양 | **배열만** | `{ average, count, reviews }` — 페이징이 없으므로 프론트가 배열로 계산할 수 있다 |

---

## 2. 범위

| 메서드 | 경로 | 권한 | 성공 |
|---|---|---|---|
| GET | `/api/destinations/{id}/reviews` | 공개 | 200 |
| POST | `/api/destinations/{id}/reviews` | 로그인 | 201 |
| PUT | `/api/destinations/{id}/reviews/{reviewId}` | 작성자 | 200 |
| DELETE | `/api/destinations/{id}/reviews/{reviewId}` | 작성자 | 200 |

`/api/festivals/{id}/reviews...` 도 같은 4개. **총 8개.**

`SecurityConfig` 는 고치지 않는다. GET 은 기존 `GET /api/destinations/**` · `/api/festivals/**` 공개 규칙에
들어가고, 나머지는 `anyRequest().authenticated()` 가 막는다.

넣지 않는 것 — 내가 쓴 리뷰 목록, 평균 별점, 페이징, 관리자 삭제 (D-009).

---

## 3. 스키마 — 테이블 2개 추가

```sql
CREATE TABLE IF NOT EXISTS review_destinations (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT   NOT NULL,
  destination_id BIGINT   NOT NULL,
  rating         INT      NOT NULL,
  comment        TEXT,
  created_at     DATETIME NOT NULL,
  updated_at     DATETIME,
  FOREIGN KEY (user_id)        REFERENCES users(id)        ON DELETE CASCADE,
  FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE,
  CHECK (rating BETWEEN 1 AND 5)
);
-- review_festivals 는 destination_id → festival_id 만 다르다
```

- **UNIQUE 없음** — 한 사용자가 같은 대상에 여러 개 쓸 수 있다 (D-013).
- **`updated_at` 은 NULL 로 시작**하고 수정할 때만 채운다. 프론트는 이 값으로 "수정됨" 을 표시할 수 있다.
- 목록 조회(`WHERE destination_id = ?`)의 인덱스는 FK 에 InnoDB 가 자동으로 만든 것을 쓴다.
- 기존 테이블을 바꾸지 않으므로 DB 를 지울 필요가 없다.

---

## 4. 엔드포인트 계약

### 공통 DTO

```json
// ReviewResponse — 목록 요소, POST · PUT 의 응답
{ "id": 7, "userId": 1, "nickname": "다나카",
  "rating": 5, "comment": "...",
  "createdAt": "2026-09-29T20:00:00", "updatedAt": null }

// ReviewRequest — POST · PUT 본문. PUT 은 두 필드를 모두 덮어쓴다 (부분 수정 아님)
{ "rating": 5, "comment": "..." }
```

- `userId` 를 싣는 이유 — 프론트가 `/api/auth/me` 의 `id` 와 비교해 "내 리뷰" 에만 수정·삭제 버튼을 보인다.
- `comment` 는 선택이다 (`null` 가능). 별점만 남길 수 있다.

### GET /api/destinations/{id}/reviews

| 상태 | 조건 | 본문 |
|---|---|---|
| 200 | | `ReviewResponse[]`, 최신순 (`id DESC`). 0건이면 `[]` |
| 404 | 여행지가 없음 | `DESTINATION_NOT_FOUND` |

### POST /api/destinations/{id}/reviews

| 상태 | 조건 | 본문 |
|---|---|---|
| 201 | | 만든 `ReviewResponse` |
| 400 | `rating` 이 없거나 1~5 밖 | `INVALID_RATING` **(신규)** |
| 404 | 여행지가 없음 | `DESTINATION_NOT_FOUND` |
| 401 | 토큰 없음 · 무효, 또는 사용자가 삭제됨 | `UNAUTHORIZED` |

- 별점 검사를 서비스에서 먼저 한다. DB `CHECK` 에만 맡기면 위반이 500 으로 나간다.
  `@Valid` 는 아직 쓰지 않는다 (D-035 — 도입 시 `error.fields` 로 확장).

### PUT /api/destinations/{id}/reviews/{reviewId}

| 상태 | 조건 | 본문 |
|---|---|---|
| 200 | | 고친 `ReviewResponse` (`updatedAt` 이 채워짐) |
| 400 | `rating` 이 없거나 1~5 밖 | `INVALID_RATING` |
| 404 | 리뷰가 없거나, **경로의 여행지에 딸린 리뷰가 아님** | `REVIEW_NOT_FOUND` **(신규)** |
| 403 | 남의 리뷰 | `FORBIDDEN` **(신규)** |
| 401 | 토큰 없음 · 무효 | `UNAUTHORIZED` |

- 판정 순서: 별점 → 리뷰 존재(대상 일치 포함) → 작성자. 없는 리뷰에 403 을 주지 않는다.
- 경로의 여행지가 아예 없는 경우도 "그 여행지에 딸린 리뷰가 없음" 이므로 `REVIEW_NOT_FOUND` 다.

### DELETE /api/destinations/{id}/reviews/{reviewId}

| 상태 | 조건 | 본문 |
|---|---|---|
| 200 | | `data: null` |
| 404 | 리뷰가 없거나 경로의 여행지에 딸리지 않음 | `REVIEW_NOT_FOUND` |
| 403 | 남의 리뷰 | `FORBIDDEN` |
| 401 | 토큰 없음 · 무효 | `UNAUTHORIZED` |

- **멱등이 아니다.** 두 번째 삭제는 404 (D-037).

### festivals

같다. 없는 축제는 `FESTIVAL_NOT_FOUND`.

### 새 `ErrorCode` 3개

| code | 상태 | 문구 |
|---|---|---|
| `INVALID_RATING` | 400 | 별점은 1~5 여야 합니다 |
| `REVIEW_NOT_FOUND` | 404 | 리뷰를 찾을 수 없습니다 |
| `FORBIDDEN` | 403 | 권한이 없습니다 |

`FORBIDDEN` 은 ERRORS.md 가 "course 의 소유권 판정을 만들 때" 추가한다고 예고했던 것이다. review 가 먼저
쓰므로 여기서 추가한다. 도메인 이름을 붙이지 않은 범용 코드로 두어 course · post 가 그대로 쓴다.

---

## 5. 계층

```
review/
├── controller/ReviewController.java                클래스 수준 @RequestMapping 없음 — 메서드마다 전체 경로
├── service/ReviewService.java
├── repository/ReviewDestinationRepository.java · ReviewFestivalRepository.java
├── entity/ReviewDestination.java · ReviewFestival.java
└── dto/ReviewRequest.java · ReviewResponse.java
```

- **엔티티** — `@ManyToOne(LAZY) User user` · 대상 · `rating` · `comment` · `createdAt` · `updatedAt`.
  생성자 `(User, 대상, rating, comment)`, 수정 메서드 `update(rating, comment)` 가 `updatedAt` 을 채운다.
  서비스는 `save` 를 다시 부르지 않는다 — 트랜잭션 안의 변경 감지(dirty checking)가 UPDATE 를 낸다.
- **작성자 판정** — `review.getUser().getId().equals(userId)`. LAZY 프록시에서 `getId()` 는 조회 없이 나온다.
- **리포지토리** — 목록은 `findByDestinationIdOrderByIdDesc` + `@EntityGraph("user")` (닉네임).
  단건은 `findByIdAndDestinationId` (경로의 대상과 일치 확인을 쿼리 하나로).
- **`ReviewResponse.from(ReviewDestination)` · `from(ReviewFestival)`** — 두 엔티티에서 같은 DTO 를 만든다.
- **별점 검사**는 서비스의 private 메서드 하나 (`FestivalService.validateMonth` 와 같은 모양).

---

## 6. 검증 (스모크)

시드가 없으므로 (favorite 와 같은 사정) **대상이 필요한 성공 경로는 확인할 수 없다.** 확인 가능한 것:

1. `GET /api/destinations/999999/reviews` (토큰 없이) → 404 `DESTINATION_NOT_FOUND` — 공개 규칙 통과 확인
2. 토큰 없이 `POST /api/destinations/1/reviews` → 401
3. 로그인 후 `POST .../999999/reviews` `{rating: 5}` → 404
4. `POST ...` `{rating: 6}` · `{}` → 400 `INVALID_RATING`
5. `PUT` · `DELETE /api/destinations/1/reviews/999999` → 404 `REVIEW_NOT_FOUND`
6. festivals 로 1 · 3 반복 → `FESTIVAL_NOT_FOUND`

시드 후 확인할 것 — 작성 201 · 목록 반영 · 수정 후 `updatedAt` · 남의 리뷰 수정 403 · 삭제 후 재삭제 404 ·
다른 여행지 경로로 수정 시 404.

**결과 (2026-09-29)** — 1 ~ 6 모두 통과. 추가로 `{rating: 0}` → 400 `INVALID_RATING`,
`{rating: "abc"}` → 400 `MALFORMED_REQUEST`, 토큰 없이 `DELETE` → 401, favorite 회귀(`GET /api/favorites`) 200 확인.
**위 "시드 후 확인할 것" 은 아직 확인하지 못했다** (favorite 의 3 · 4 와 같은 사정).

**재확인 (2026-09-30, 시드 후 · D-050)** — 여행지 · 축제 양쪽 모두 통과: 작성 201 (`updatedAt` null) · 목록 반영 ·
수정 200 (`updatedAt` 채워짐) · B 의 수정 · 삭제 403 · 다른 대상 경로로 수정 404 · 삭제 200 후 재삭제 404.
