# favorite API 설계

2026-09-29 · 확정 (같은 날 사후 검수로 0 · 1단계를 되짚음) · greenfield 브랜치 · Task 3 의 첫 번째
(favorite → review, history 는 D-038 로 보류)

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

### 사후 검수 (2026-09-29) — 0단계 설계 재정리 결과

처음 구현 때 설계 재정리 · 스펙 검수 · 계층별 진행을 건너뛰어, 보강한 `spring-domain-skeleton` 스킬의
사후 검수 모드로 되짚었다. 옛 동작(`pre-greenfield` 태그)과 프론트 사용처를 확인한 뒤의 판정:

| # | 항목 | 판정 | 비고 |
|---|---|---|---|
| A | 목록 모양 (두 배열) | **다시 정함 → 두 배열 유지** (1단계) | 마이페이지 "전체" 탭은 여행지·축제를 섞어 최근순으로 보였다. 섞는 대신 **"전체" 탭을 없애고 여행지 / 축제 탭으로 따로 보인다** (Task 7). 그래서 백엔드 모양은 그대로다 |
| B | 목록 요소에 대상 상세 포함 (`@EntityGraph` JOIN, 요청 1번 · 쿼리 2번) | 유지 | 옛 방식은 항목마다 상세를 다시 불러 요청이 1 + N 번이었다 |
| C | 추가할 때 대상 존재 확인 → 404 | 유지 | 옛 방식은 확인하지 않았다. 지금은 FK 가 있어 확인하지 않으면 500 |
| D | 하트 켜짐 여부 확인 | **① 목록 재사용** | 단건 확인(카드 수만큼 요청) · id 전용 API · 여행지 응답에 끼워 넣기(destination → favorite 역의존)를 검토하고 기각 |
| E | 전체 삭제 API | 넣지 않음 | 설정 화면 한 곳만 쓴다 |
| F | 코스 즐겨찾기 제거 (D-011) | 일단 유지 | course 기능을 만든 뒤 다시 본다 |

**D 의 따라오는 일 (Task 7)** — 옛 `FavButton.jsx` 의 5초 캐시는 응답이 **도착한 뒤에** 채워져서, 목록
페이지의 카드 30개가 동시에 마운트되면 각자 요청을 보낸다 (실제 30번). 요청 중인 Promise 를 공유하도록
고쳐야 ① 이 "요청 1번" 이 된다.

### 사후 검수 — 1단계 계약 결정 결과

**A 를 다시 정할 때 검토한 안** — ① 한 배열에 섞어 서버가 최근순 정렬 (요소마다 `type` · `favoritedAt` ·
대상 하나는 항상 null) · ② 두 배열 + `favoritedAt` (프론트가 합쳐 정렬) · ③ 한 배열에 공통 필드만 평탄화
(축제 전용 필드가 빠지고 B 와 충돌). 사용자는 **두 배열을 유지하고 "전체" 탭을 없애는 것**으로 정했다 —
섞어 보일 필요 자체를 없앴다.

**처음 구현 때 Claude 가 정하고 승인받지 않았던 계약** — 사후 검수에서 **전부 승인**했다.

| # | 항목 | 확정 | 이유 |
|---|---|---|---|
| 1 | 추가 성공 코드 | **200** (201 아님) | 멱등이라 새로 만들었는지가 호출마다 다르다. 응답을 하나로 둔다 |
| 2 | 삭제할 때 대상 존재 확인 | **하지 않는다** — 없는 대상 id 도 200 | 멱등 삭제에서 "없는 대상" 과 "즐겨찾기 안 한 대상" 은 결과가 같다 |
| 3 | 토큰은 유효한데 사용자가 삭제됨 | **401 `UNAUTHORIZED`** | `AuthService.me` 와 같은 규칙. 확인하지 않으면 FK 위반으로 500 |
| 4 | 같은 대상 동시 두 번 추가 | UNIQUE 위반 **500, 처리하지 않음** | 가입의 동시 요청과 같은 방침. 하트 연타는 프론트 `busy` 잠금이 막는다 |
| 5 | `favoritedAt` (즐겨찾기한 시각) | **응답에 넣지 않는다** | 섞어 정렬할 일이 없어졌고, 표시하는 화면도 없다. 배열 안 순서는 `id DESC` 로 충분. 필요해지면 그때 추가 |

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

넣지 않는 것 — "이 여행지를 내가 즐겨찾기했는가" 단건 조회 (하트는 목록을 재사용한다, 사후 검수 D) ·
즐겨찾기 수 집계 · 전체 삭제 (사후 검수 E) · 페이징 · 코스 즐겨찾기 (D-011, course 이후 재검토).

**Task 7 에서 프론트가 할 일** — 마이페이지 즐겨찾기 탭을 "여행지 / 축제" 두 탭으로 (전체 · 여행코스 필터 제거) ·
`FavButton` 이 요청 중인 목록 Promise 를 공유 · 상세를 다시 부르던 N+1 제거 (목록에 이미 들어 있다).

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

---

## 7. 사후 검수 — 5단계 계층별 검수

처음 구현 때 계층 단위 진행(D-007)을 건너뛰어, 계층마다 기존 코드를 설명하고 내부 설계를 승인받았다.

### entity — 전부 승인

| # | 확정 | 기각한 대안 |
|---|---|---|
| P1 | 사용자를 `@ManyToOne(LAZY) User` 로 참조. 만들 때의 사용자 조회가 "삭제된 사용자 → 401" 확인을 겸한다 | `Long userId` 필드 |
| P2 | `createdAt` 은 생성자에서 `LocalDateTime.now()` (`User` 와 같은 방식) | `@CreationTimestamp` · JPA Auditing · DB `DEFAULT` |
| P3 | `FavoriteDestination` · `FavoriteFestival` 을 복사해 둔다 (각 파일이 혼자 읽힌다) | `@MappedSuperclass` 공통 부모 |
| P4 | 단방향 연관. `User` · `Destination` 은 즐겨찾기를 모른다 | 양방향 `@OneToMany(mappedBy)` — 역의존과 `toString` 재귀 위험 |

### repository — 전부 승인

| # | 확정 | 기각한 대안 |
|---|---|---|
| R1 | 파생 쿼리(메서드 이름)만 쓴다 — destination · festival 과 같은 방식. `findByUserId` 는 `user.id` 로 해석된다 | `@Query` JPQL 직접 작성 |
| R2 | 삭제는 파생 `deleteBy…` (SELECT 후 DELETE, 쿼리 2번). 트랜잭션 안에서만 동작 | `@Modifying @Query` DELETE 한 번 — 영속성 컨텍스트와 어긋날 수 있다 |
| R3 | 목록 `@EntityGraph` 는 `{대상, 대상.prefecture}` 만. `user` 는 응답에 없어 JOIN 하지 않는다 | `user` 까지 JOIN |

### service — 전부 승인

| # | 확정 | 기각한 대안 |
|---|---|---|
| S1 | 추가의 판정 순서: 이미 있음(→ 200) → 대상 존재(→ 404) → 사용자 존재(→ 401) → 저장. 연타가 쿼리 1번으로 끝난다. 삭제된 사용자가 없는 대상을 추가하면 401 이 아니라 404 — 드문 경우라 받아들인다 | 사용자 먼저 확인 |
| S2 | 다른 도메인은 서비스가 아니라 리포지토리를 직접 쓴다. 즐겨찾기를 만들려면 엔티티가 필요한데 다른 서비스는 DTO 만 돌려준다 | `DestinationService` 경유 |
| S3 | `findUser` 를 서비스마다 private 로 둔다 (Auth · Favorite · Review). course · post 에서 늘어나면 그때 다시 본다 | 공용 메서드 추출 · `getReferenceById` (401 확인이 사라져 500) |
| S4 | 중복은 `exists` 로 먼저 확인 (`AuthService.signup` 과 같은 방식) | `save` 후 UNIQUE 예외를 잡아 무시 — rollback-only 함정 |

### 검수 종료 (dto 에서)

사용자가 코드를 직접 둘러봤고 어려운 부분이 없어, **dto 계층에서 사후 검수를 끝냈다.** 코드 변경은 없다.

- **controller** — 계층 검수를 하지 않았다.
- **미결 제안** (지금 코드 그대로 둔다, 다시 볼 때 여기서 시작)
  - T1 — `FavoriteListResponse` 에 `from()` / `of()` 팩토리가 없고 서비스가 직접 조립한다.
    "변환은 DTO 안에" 규칙의 예외다 (두 조회 결과를 묶는 틀이라 변환 로직이 없다).
  - T2 — 목록 요소는 `DestinationResponse` · `FestivalResponse` 를 재사용하므로 그 안의 `createdAt` 은
    **즐겨찾기한 시각이 아니라 여행지·축제가 등록된 시각**이다. 옛 API 의 `createdAt` 은 즐겨찾기한 시각이었다.
    지금 프론트는 쓰지 않지만 Task 7 에서 오해하기 쉽다.
