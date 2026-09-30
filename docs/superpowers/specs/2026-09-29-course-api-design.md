# course API 설계

2026-09-29 · 확정 (2단계 검수 승인) · greenfield 브랜치 · Task 4 (Task 5 `post` 는 D-040 으로 보류)

누구나 기본 제공 코스와 공개된 사용자 코스를 보고, 로그인한 사용자가 여행지·축제를 일차별로 엮은 코스를
만들고, 작성자만 고치고 지운다. 비공개 코스는 작성자만 본다.

관련 결정: [D-041](../../DECISIONS.md#d-041--코스는-사용자--기본-제공-공개-범위는-코스마다-임시저장은-없다) (코스의 정체) ·
[D-042](../../DECISIONS.md#d-042--정류장은-여행지축제를-가리키고-일차--순서--시간--메모를-담는다) ·
[D-043](../../DECISIONS.md#d-043--정류장에서-시간을-뺀다) (정류장) ·
[D-044](../../DECISIONS.md#d-044--정류장은-한-테이블에-nullable-fk-2개--cascade-정확히-하나-는-서비스가-판정한다) (테이블) ·
[D-045](../../DECISIONS.md#d-045--로그인-사용자의-내-것-은-apime-아래에-둔다--favorite-경로도-옮긴다) (`/api/me`) ·
[D-046](../../DECISIONS.md#d-046--코스-api-는-정류장을-type--targetid-로-받고-목록은-요약--상세는-정류장까지-준다) (요청·응답) ·
[D-047](../../DECISIONS.md#d-047--코스-수정삭제의-판정은-값--존재비공개는-숨김--작성자-본문-에러는-invalid_course-하나로-묶는다) (판정·에러)

---

## 1. 정한 것 (2026-09-29)

0단계에서 옛 동작과 D-012 의 코스 부분을 **전부 "처음부터 다시 설계"** 로 받고 1단계에서 정했다.
"누가 정했나" 는 각 D 항목에 적혀 있다 — Claude 제안으로 시작한 것도 전부 사용자 승인을 받았다.

| 항목 | 결정 | 기각한 대안 | 근거 |
|---|---|---|---|
| 만드는 주체 | 사용자 + 기본 제공 (`owner_user_id NULL`) | 사용자만 · 기본 제공만 | D-041 |
| 공개 범위 | 코스마다 `is_public` | 모두 공개 · 나만 · `visibility VARCHAR` | D-041 · D-044 |
| 임시저장 | 없음 | draft/published 유지 | D-041 |
| 정류장 대상 | 여행지 또는 축제 하나 | 여행지만 · 자유 입력 | D-042 |
| 정류장 내용 | 일차 · 순서 · 메모 (이름·좌표·사진은 조인) | 시간 (D-043 으로 뺌) · 이름 덮어쓰기 · 설명 스냅샷 | D-042 · D-043 |
| 원본 삭제 | 정류장도 삭제 (CASCADE) | RESTRICT | D-042 |
| 정류장 테이블 | 한 테이블, nullable FK 2개 + CASCADE, "정확히 하나" 는 서비스 | FK 2개 + CHECK · 대상별 테이블 2개 · `target_type` | D-044 |
| 지역 | `prefecture_id NOT NULL` (대표 현 하나) | 정류장에서 도출 | D-044 |
| 내 코스 경로 | `GET /api/me/courses` | `/api/courses/me` · `?mine=true` | D-045 |
| 요청 정류장 | `{dayNo, type, targetId, memo}`, `seq` 는 서버가 매김 | 테이블 그대로 · 일차 중첩 | D-046 |
| 목록 / 상세 | 나눔 (요약 / 상세) | 같은 모양 · 요약에 `days`·`stopCount` | D-046 |
| 응답 정류장 | 원본 장소 평탄화 | 기존 DTO 재사용 | D-046 |
| `isPublic` | 필수 | 기본 비공개 · 기본 공개 | D-046 |
| 수정 | PUT 전체 덮어쓰기, 정류장 전부 교체 | 정류장 별도 API | 1단계 |
| 남의 비공개 코스 | 404 (존재를 숨김) | 403 | D-047 |
| 본문 에러 | `INVALID_COURSE` 하나 | 필드별 코드 | D-047 |

---

## 2. 범위

| 메서드 | 경로 | 권한 | 성공 |
|---|---|---|---|
| GET | `/api/courses?prefecture=` | 공개 | 200 요약 배열 |
| GET | `/api/courses/{id}` | 공개 (토큰이 있으면 읽는다) | 200 상세 |
| POST | `/api/courses` | 로그인 | 201 상세 |
| PUT | `/api/courses/{id}` | 작성자 | 200 상세 |
| DELETE | `/api/courses/{id}` | 작성자 | 200 `data: null` |
| GET | `/api/me/courses` | 로그인 | 200 요약 배열 |

**총 6개.** `SecurityConfig` 에 `GET /api/courses/**` 공개 규칙을 한 줄 추가한다. `/api/me/courses` 는 기존
`anyRequest().authenticated()` 가 막는다.

넣지 않는 것 — 태그 · 테마 필터 (D-024) · 임시저장 (D-041) · 정류장 시간 (D-043) · 관리자 수정·삭제 (D-009) ·
페이징 · 코스 즐겨찾기·리뷰 (D-011) · 요약의 일수·정류장 수 (D-046) · 코스 복사.

---

## 3. 스키마 — 테이블 2개 추가 (D-044)

```sql
-- course (Task 4) — 정류장은 여행지·축제 중 하나를 가리킨다 (D-042 · D-044)
CREATE TABLE IF NOT EXISTS courses (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  title         VARCHAR(200) NOT NULL,
  description   TEXT,
  prefecture_id BIGINT       NOT NULL,
  image_path    VARCHAR(500),
  owner_user_id BIGINT,                          -- NULL = 기본 제공 코스 (D-041)
  is_public     BOOLEAN      NOT NULL,
  created_at    DATETIME     NOT NULL,
  updated_at    DATETIME,                        -- 수정할 때만 채운다
  FOREIGN KEY (prefecture_id) REFERENCES prefectures(id),
  FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- destination_id · festival_id 중 정확히 하나는 서비스가 판정한다.
-- MySQL 은 참조 동작(CASCADE)이 걸린 컬럼을 CHECK 에 쓸 수 없다 (에러 3823).
CREATE TABLE IF NOT EXISTS course_stops (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  course_id      BIGINT       NOT NULL,
  day_no         INT          NOT NULL,
  seq            INT          NOT NULL,
  destination_id BIGINT,
  festival_id    BIGINT,
  memo           VARCHAR(500),
  UNIQUE (course_id, day_no, seq),
  FOREIGN KEY (course_id)      REFERENCES courses(id)      ON DELETE CASCADE,
  FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE,
  FOREIGN KEY (festival_id)    REFERENCES festivals(id)    ON DELETE CASCADE,
  CHECK (day_no >= 1),
  CHECK (seq >= 1)
);
```

- `CREATE TABLE IF NOT EXISTS` 로 추가만 하므로 **기존 테이블 · 데이터를 지우지 않는다** (DB 삭제 · 백업 불필요).
- FK 컬럼에는 MySQL 이 인덱스를 자동으로 만든다. 따로 인덱스를 추가하지 않는다.

---

## 4. 엔드포인트 계약

모든 응답은 `ApiResponse` 봉투(D-035)다. 아래 예시는 `data` 만 보인다. 예시 코스는 "교토 1박 2일".

### DTO

**요청 `CourseRequest`** — POST · PUT 공통

```json
{
  "title": "교토 1박 2일",
  "description": "절과 축제를 함께 보는 코스",
  "prefecture": "교토부",
  "imagePath": null,
  "isPublic": true,
  "stops": [
    { "dayNo": 1, "type": "DESTINATION", "targetId": 7,  "memo": "아침 일찍" },
    { "dayNo": 1, "type": "FESTIVAL",    "targetId": 3,  "memo": null },
    { "dayNo": 2, "type": "DESTINATION", "targetId": 12, "memo": null }
  ]
}
```

- `type` 은 `DESTINATION | FESTIVAL` (Java enum). 그 밖의 값은 역직렬화 실패로 400 `MALFORMED_REQUEST`.
- 서버는 `dayNo` 별로 묶고, 그날 안에서 배열에 나온 순서대로 `seq` 를 1 부터 매긴다.
- `prefecture` 는 현 이름 문자열 — 응답의 `prefecture` 와 같은 값 공간 (destination 관례).

**요약 `CourseSummaryResponse`** — 공개 목록 · 내 코스 목록

```json
{ "id": 5, "title": "교토 1박 2일", "prefecture": "교토부", "imagePath": null,
  "isPublic": true, "ownerId": 11, "ownerNickname": "smoke", "createdAt": "2026-09-29T15:00:00" }
```

**상세 `CourseResponse`** — 상세 · 작성 · 수정

```json
{ "id": 5, "title": "교토 1박 2일", "description": "절과 축제를 함께 보는 코스", "prefecture": "교토부",
  "imagePath": null, "isPublic": true, "ownerId": 11, "ownerNickname": "smoke",
  "createdAt": "2026-09-29T15:00:00", "updatedAt": null,
  "stops": [
    { "dayNo": 1, "seq": 1, "type": "DESTINATION", "targetId": 7,  "name": "기요미즈데라", "prefecture": "교토부",
      "lat": 34.9949, "lng": 135.7850, "imagePath": "...", "memo": "아침 일찍" },
    { "dayNo": 1, "seq": 2, "type": "FESTIVAL",    "targetId": 3,  "name": "기온 마쓰리",   "prefecture": "교토부",
      "lat": 35.0037, "lng": 135.7788, "imagePath": "...", "memo": null },
    { "dayNo": 2, "seq": 1, "type": "DESTINATION", "targetId": 12, "name": "후시미이나리", "prefecture": "교토부",
      "lat": 34.9671, "lng": 135.7727, "imagePath": "...", "memo": null }
  ] }
```

- `stops` 는 `(dayNo, seq)` 오름차순.
- `ownerId` · `ownerNickname` 은 기본 제공 코스에서 둘 다 `null`. 프론트는 `/api/auth/me` 의 id 와 비교해
  내 코스에만 수정·삭제 버튼을 보인다 (review 의 `userId` 관례).
- 정류장의 `prefecture` 는 원본 장소의 현이다 — 코스의 대표 현과 다를 수 있다.

### GET /api/courses?prefecture=

- 기본 제공 코스 + 공개 사용자 코스. **로그인 여부와 무관하게 같은 결과** — 토큰을 보지 않는다.
- 최신순 (`id DESC`). `prefecture` 는 선택.
- 없는 현 이름 → 404 `PREFECTURE_NOT_FOUND` (destination 관례). 결과 0건 → 200 `[]`.

### GET /api/courses/{id}

- 공개 코스 · 기본 제공 코스 → 누구나 200.
- 비공개 코스 → 토큰의 주인이 작성자면 200, 아니면(토큰 없음 포함) **404 `COURSE_NOT_FOUND`**.
- 없는 id → 404 `COURSE_NOT_FOUND`.

### POST /api/courses

- 로그인 필요. 201 + 상세. `owner` 는 토큰의 사용자, `createdAt` 은 서버 시각.
- 판정 순서: 본문 값(400 `INVALID_COURSE`) → 현(404 `PREFECTURE_NOT_FOUND`) → 정류장 대상(404 `DESTINATION_NOT_FOUND` / `FESTIVAL_NOT_FOUND`).

### PUT /api/courses/{id}

- 작성자만. 200 + 상세. 모든 필드를 덮어쓰고 **정류장은 전부 지우고 요청대로 다시 만든다.** `updatedAt` 을 채운다.
- 판정 순서 (D-047):
  1. 본문 값 → 400 `INVALID_COURSE`
  2. 코스 존재 → 없거나 **남의 비공개 코스**면 404 `COURSE_NOT_FOUND`
  3. 작성자 → 남의 공개 코스 · 기본 제공 코스면 403 `FORBIDDEN`
  4. 현 → 404 `PREFECTURE_NOT_FOUND`, 정류장 대상 → 404 `DESTINATION_NOT_FOUND` / `FESTIVAL_NOT_FOUND`

### DELETE /api/courses/{id}

- 작성자만. 200 + `data: null`. 정류장은 FK `ON DELETE CASCADE` 로 DB 가 지운다.
- 판정 순서: 코스 존재(404, 남의 비공개 포함) → 작성자(403).
- **멱등 아님** — 두 번째 요청은 404 (review 관례).

### GET /api/me/courses

- 로그인 필요. 내가 만든 코스 전부 (공개 · 비공개). 최신순. 요약 모양. 필터 없음.

### 본문 규칙 (D-047)

| 규칙 | 위반 시 |
|---|---|
| `title` 없음 · 빈 문자열 | 400 `INVALID_COURSE` |
| `prefecture` · `isPublic` · `stops` 없음 | 400 `INVALID_COURSE` |
| 정류장의 `dayNo` · `type` · `targetId` 없음, `dayNo < 1` | 400 `INVALID_COURSE` |
| 빈 `stops` · 일차 건너뜀(1 → 3) · 같은 장소 두 번 · 배열 안에서 `dayNo` 섞임 | **허용** |
| `description` · `imagePath` · `memo` | 선택. 길이 판정 없음 (프로젝트 `CLAUDE.md`) |

### 새 `ErrorCode` 2개

| code | 상태 | 문구 |
|---|---|---|
| `COURSE_NOT_FOUND` | 404 | 코스를 찾을 수 없습니다 |
| `INVALID_COURSE` | 400 | 코스 요청 값이 올바르지 않습니다 |

`FORBIDDEN` 은 review 가 이미 추가했다 — course 는 그대로 쓴다.

---

## 5. 계층

```
course/
├── controller/CourseController.java     공개·작성·수정·삭제 5개 + /api/me/courses 1개
├── service/CourseService.java
├── repository/CourseRepository.java · CourseStopRepository.java (필요 여부는 5a 에서)
├── entity/Course.java · CourseStop.java
└── dto/CourseRequest.java · CourseStopRequest.java · CourseSummaryResponse.java ·
        CourseResponse.java · CourseStopResponse.java · StopType.java (enum 위치는 5a 에서)
```

확정된 것만 적는다. 나머지는 5단계의 각 계층 5a 에서 틀 · 제안으로 승인받는다.

- 엔티티는 `@Getter` + `@NoArgsConstructor(PROTECTED)` 까지 (D-027). 연관은 전부 `LAZY`.
- 서비스는 DTO 를 반환한다 (`open-in-view: false`). 쓰기 메서드만 `@Transactional`.
- 연관 엔티티를 응답에 쓰는 조회는 `@EntityGraph` 로 한 번에 가져온다.

**5a 에서 정할 것** — `Course` → `CourseStop` 을 `@OneToMany(cascade, orphanRemoval)` 로 둘지 리포지토리를 따로 둘지 ·
정류장 전부 교체 시 `UNIQUE(course_id, day_no, seq)` 충돌을 피하는 순서 (삭제 flush 후 삽입) ·
상세 조회의 조인 범위 · 공개 경로에서 토큰을 읽는 방법 (`JwtAuthenticationFilter` 가 공개 경로에서도 인증을 채우는지) ·
`StopType` enum 의 위치 · 목록 필터 조합별 리포지토리 메서드.

---

## 6. 검증 (스모크)

**여행지·축제 시드가 없으므로** (favorite · review 와 같은 사정) 정류장이 있는 코스는 만들 수 없다.
빈 `stops` 가 허용되므로(D-047) **코스 자체의 흐름은 확인할 수 있다.**

1. 토큰 없이 `GET /api/courses` → 200 `[]` — 공개 규칙 통과
2. 토큰 없이 `POST /api/courses` · `GET /api/me/courses` → 401
3. A 로그인 → `POST` 공개 코스 `stops: []` → 201, `ownerId` = A. 공개 목록 · A 의 `/api/me/courses` 에 보임
4. A → `POST` 비공개 코스 → 201. 공개 목록에 **안** 보이고 `/api/me/courses` 에는 보임
5. 비공개 코스 상세: A → 200 · B → 404 · 토큰 없이 → 404
6. B → A 의 공개 코스 `PUT` · `DELETE` → 403 `FORBIDDEN`, A 의 비공개 코스 `PUT` · `DELETE` → 404
7. A → `PUT` (제목 · 공개 여부 변경) → 200, `updatedAt` 채워짐
8. 본문 에러 → 400 `INVALID_COURSE`: `title` 없음 · `""` · `isPublic` 없음 · `stops` 없음 ·
   정류장 `dayNo: 0` · `targetId` 없음. `type: "HOTEL"` → 400 `MALFORMED_REQUEST`
9. 정류장 대상 없음 `{type: DESTINATION, targetId: 999999}` → 404 `DESTINATION_NOT_FOUND`, FESTIVAL 도 같게
10. `prefecture: "없는현"` → 404 `PREFECTURE_NOT_FOUND` (작성 · 목록 필터 둘 다)
11. A → `DELETE` → 200, 두 번째 → 404 `COURSE_NOT_FOUND`
12. 회귀 — `GET /api/me/favorites` 200

**시드 후 확인할 것** — 정류장이 섞인 코스 작성 · 상세의 `(dayNo, seq)` 정렬과 평탄화 필드 · 배열 안 `dayNo` 섞임 시
`seq` 매김 · 수정 시 정류장 전부 교체 · 기본 제공 코스 수정 403 · 원본 장소 삭제 시 정류장만 빠짐.

**결과 (2026-09-29)** — 1 ~ 12 모두 통과. `courses` · `course_stops` 가 부팅 때 에러 없이 만들어졌고, 응답의
`isPublic` 키가 그대로 나가는 것(`public` 으로 바뀌지 않음)도 확인했다. 추가로 B 가 A 의 비공개 코스에 잘못된 본문으로
`PUT` → 400 `INVALID_COURSE` (값 판정이 존재 판정보다 먼저), `GET /api/courses/999999` → 404,
`?prefecture=교토부` 필터 동작을 확인했다.
**위 "시드 후 확인할 것" 은 아직 확인하지 못했다** — 특히 정류장 전부 교체의 `clearStops → flush → addStop` 순서(UNIQUE 충돌 방지)는
정류장이 있는 코스가 있어야 실제로 검증된다.

**재확인 (2026-09-30, 시드 후 · D-050)** — 통과: 정류장이 섞인 코스 작성 201 · `dayNo` 를 섞어 보낸 배열이
`(dayNo, seq)` 로 정렬되고 같은 `dayNo` 안에서 배열 순서대로 `seq` 가 매겨짐 · 평탄화 필드(`name` · `prefecture` ·
`lat` · `lng` · `memo`)가 원본과 일치 · 정류장 전부 교체(같은 `(dayNo, seq)` 자리에 다른 대상) 200, 같은 본문으로 한 번 더 200,
빈 배열로 교체 200 — **UNIQUE 충돌 없음** · 삭제 200 후 재삭제 404.
**아직 남은 것** — 기본 제공 코스 수정 403 (코스 시드 전, D-048) · 원본 장소 삭제 시 정류장만 빠짐 (여행지·축제 삭제 API 가 없어 DB 에서 직접 지워야 한다).

> 스모크 요령 — Windows 의 Git Bash 에서 `curl -d '{"prefecture":"교토부"}'` 처럼 한글을 명령행 인자로 넘기면
> UTF-8 이 아닌 인코딩으로 넘어가 400 `MALFORMED_REQUEST` 가 난다. 본문을 UTF-8 파일로 쓰고 `--data-binary @file` 로 보낸다.

---

## 7. 검수에서 정한 것

- **기본 제공 코스 시드는 Task 4 에서 만들지 않는다** —
  [D-048](../../DECISIONS.md#d-048--기본-제공-코스-시드는-task-4-에서-만들지-않고-여행지축제-시드와-함께-따로-한다).
  정류장이 여행지·축제를 참조하므로 여행지·축제 시드와 함께 따로 한다. Task 4 는 구조(`owner_user_id NULL` · 수정·삭제 403)까지.
