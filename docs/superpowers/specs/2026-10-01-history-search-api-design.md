# history · search API 설계

- 날짜: 2026-10-01
- 결정: [D-052](../../DECISIONS.md#d-052--history-를-만든다--한-배열-시간순-장소-정보를-평탄화해-싣는다) (history) ·
  [D-053](../../DECISIONS.md#d-053--search-는-db-like-로-찾고-공개-코스만-대상별-20건까지) (search)
- 진행: 0단계 재검토 후보를 사용자가 승인한 뒤, 사용자 요청으로 1 ~ 5단계 게이트를 건너뛰고 이어서 구현했다.

## 옛 동작 (pre-greenfield)

| | history | search |
|---|---|---|
| 경로 | `GET/POST/DELETE /api/history` | `GET /api/search?q=` |
| 동작 | 대상당 1행 upsert, `visited_at DESC`, 전체 삭제만 | 전체 로드 후 자바 `contains`, 대상별 20건 |
| 응답 | `{targetType, targetId, visitedAt}` → 프론트가 항목마다 상세를 다시 부름 (N+1) | `{destinations, festivals, courses}` |
| 대상 | 여행지 · 축제 · 코스 | 여행지 · 축제 · 코스 (공개 여부 무시) |

## history

### 테이블

`history_destinations` · `history_festivals` — favorite 와 같은 모양 (id + `UNIQUE(user_id, 대상_id)`, FK `ON DELETE CASCADE`)
에 `visited_at DATETIME NOT NULL`. DDL 은 `schema.sql`.

### 계약

| 메서드 | 경로 | 권한 | 응답 `data` |
|---|---|---|---|
| GET | `/api/me/history` | 🔑 | `HistoryItemResponse[]` (`visitedAt DESC`, 두 대상 섞임) |
| POST | `/api/me/history/destinations/{id}` | 🔑 | `null` |
| POST | `/api/me/history/festivals/{id}` | 🔑 | `null` |
| DELETE | `/api/me/history` | 🔑 | `null` |

`HistoryItemResponse` = `{type: "DESTINATION"|"FESTIVAL", targetId, name, prefecture, imagePath, visitedAt}`

### 판정

- POST: 이미 기록이 있으면 → `visitedAt` 갱신 (대상 존재 확인 생략, FK 가 이미 보장) /
  없으면 → 대상 조회 (`DESTINATION_NOT_FOUND` · `FESTIVAL_NOT_FOUND` 404) → 사용자 조회 (`UNAUTHORIZED` 401) → INSERT
- DELETE: 두 테이블에서 `delete ... where user.id = :userId` 한 문장씩. 없어도 200.
- 새 에러 코드 없음.

### 패키지

```
history/
├── controller/HistoryController
├── service/HistoryService
├── repository/HistoryDestinationRepository · HistoryFestivalRepository
├── entity/HistoryDestination · HistoryFestival      touch() 로 visitedAt 갱신 (변경 감지)
└── dto/HistoryItemResponse · HistoryTargetType
```

## search

### 계약

| 메서드 | 경로 | 권한 | 응답 `data` |
|---|---|---|---|
| GET | `/api/search?q=` | 공개 | `{destinations: DestinationResponse[], festivals: FestivalResponse[], courses: CourseSummaryResponse[]}` |

- 매칭: 여행지·축제 `name | description | prefecture.name LIKE %q%`, 코스 `isPublic AND (title | prefecture.name LIKE %q%)`
- 정렬: 각 목록 API 와 같음. 대상별 20건 (`PageRequest.of(0, 20)`)
- `q` 없음 · 공백 → 세 배열 모두 빈 배열 (200). `q` 는 trim 해서 쓴다.
- 새 에러 코드 없음.

### 패키지 · 공용 파일

```
search/
├── controller/SearchController
├── service/SearchService
└── dto/SearchResponse
```

- 쿼리는 대상 도메인에 둔다: `DestinationRepository.search` · `FestivalRepository.search` · `CourseRepository.searchPublic`
- `SecurityConfig`: `GET /api/search` permitAll

## 검증

`bash scripts/smoke/history_search.sh` (앱이 8080 에 떠 있어야 한다)

## 프론트에 남는 일 (Task 7)

- `api/client.js`: `history` → `GET /api/me/history`, `touchHistory(type, id)` → `POST /api/me/history/{type}s/{id}`,
  `clearHistory` → `DELETE /api/me/history`. 응답 봉투(`data`)를 벗긴다.
- `HistoryTab.jsx`: 항목마다 상세를 다시 부르지 않고 `name` · `imagePath` 를 그대로 쓴다. `type` 은 대문자 enum 이다.
- `Detail.jsx`: 코스 상세에서는 `touchHistory` 를 부르지 않는다.
