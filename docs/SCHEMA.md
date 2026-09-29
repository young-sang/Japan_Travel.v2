# 데이터베이스 스키마

Japan Travel v2 — **MySQL 8** (`localhost:3306/japan_travel`), `greenfield` 브랜치.
정의 위치: [`backend/src/main/resources/schema.sql`](../backend/src/main/resources/schema.sql)

- 부팅 때마다 `schema.sql` 을 실행한다 (`spring.sql.init.mode: always`). 전부
  `CREATE TABLE IF NOT EXISTS` · `INSERT IGNORE` 라 여러 번 돌아도 같다.
- Hibernate 는 테이블을 만들지 않는다 (`ddl-auto: none`). 테이블의 주인은 `schema.sql` 이고,
  엔티티는 거기에 맞춘다.
- 마이그레이션 도구가 없다. 이미 있는 테이블의 구조를 바꾸면 `IF NOT EXISTS` 때문에 반영되지 않으므로
  **DB 를 지우고 다시 띄운다. 지우기 전에 반드시 백업한다** (프로젝트 `CLAUDE.md`).
- 테이블은 그 테이블이 필요한 도메인을 시작할 때 추가한다 (D-020). 그래서 지금은 4개뿐이다.

---

## 테이블 한눈에 보기

| 테이블 | 도메인 | 역할 | 추가 시점 |
|---|---|---|---|
| `prefectures` | 공통 | 도도부현 47개 참조 테이블. `schema.sql` 이 시드까지 넣는다 | Task 1 |
| `destinations` | destination | 여행지 | Task 1 |
| `festivals` | festival | 축제 | Task 1 |
| `users` | user | 회원 계정 | Task 2 |
| `favorite_destinations` | favorite | 여행지 즐겨찾기 | Task 3 |
| `favorite_festivals` | favorite | 축제 즐겨찾기 | Task 3 |
| `review_destinations` | review | 여행지 리뷰 | Task 3 |
| `review_festivals` | review | 축제 리뷰 | Task 3 |

```
prefectures  1 ──< destinations 1 ──< favorite_destinations >── 1 users
                                1 ──< review_destinations   >── 1 users
             1 ──< festivals    1 ──< favorite_festivals    >── 1 users
                                1 ──< review_festivals      >── 1 users
```

---

## `prefectures`

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | |
| `name` | VARCHAR(50) | NOT NULL, UNIQUE | 한글 이름 (`교토부`, `홋카이도` …) |

- 자연키(`name`)가 아니라 `id` 를 키로 쓴다 (D-025, D-023 을 뒤집음).
- API 는 `id` 가 아니라 **이름**으로 주고받는다. `?prefecture=교토부` 로 필터하고, 응답의
  `prefecture` 도 이름 문자열이다.
- 전용 API 는 없다. entity · repository 만 있고 destination · festival 서비스가 존재 확인에 쓴다.

## `destinations`

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | |
| `name` | VARCHAR(200) | NOT NULL | |
| `prefecture_id` | BIGINT | NOT NULL, FK → `prefectures.id` | |
| `description` | TEXT | | |
| `lat` · `lng` | DOUBLE | | 지도 표시용 |
| `image_path` | VARCHAR(500) | | |
| `created_at` | DATETIME | NOT NULL | 자바 `LocalDateTime` (D-026 이 D-017 의 날짜 문제를 해결) |

- `UNIQUE (name, prefecture_id)` — 같은 현 안에서 이름 중복 금지.

## `festivals`

`destinations` 와 같은 컬럼에 두 개가 더 있다.

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `month` | INT | NOT NULL, `CHECK (1~12)` | 필터·정렬 기준 |
| `date_text` | VARCHAR(100) | | 사람이 읽는 날짜 (`7월 중순` 등). 정규화하지 않는다 |

- `UNIQUE (name, prefecture_id)`, FK → `prefectures.id` 는 destinations 와 같다.
- `CHECK` 는 MySQL 8.0.16 부터 실제로 강제된다.

## `users`

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | JWT 의 subject |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE | 로그인 아이디 |
| `password_hash` | VARCHAR(100) | NOT NULL | BCrypt (60자). 응답으로 내보내지 않는다 |
| `nickname` | VARCHAR(50) | NOT NULL | 화면 표시 이름 |
| `role` | VARCHAR(20) | NOT NULL, DEFAULT `'USER'`, `CHECK IN ('USER','ADMIN')` | 1차에서는 권한 분기에 쓰지 않는다 (D-009) |
| `created_at` | DATETIME | NOT NULL | |

## `favorite_destinations` · `favorite_festivals`

두 테이블은 대상 컬럼만 다르다 (`destination_id` → `destinations` / `festival_id` → `festivals`).

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | 목록 정렬 기준 (`id DESC` = 최근 추가 순) |
| `user_id` | BIGINT | NOT NULL, FK → `users.id` `ON DELETE CASCADE` | |
| `destination_id` / `festival_id` | BIGINT | NOT NULL, FK → 대상 `ON DELETE CASCADE` | |
| `created_at` | DATETIME | NOT NULL | 지금은 응답에 싣지 않는다 |

- `UNIQUE (user_id, 대상_id)` — 같은 대상을 두 번 넣지 못한다. 복합 PK 대신 `id` + UNIQUE 로 둔
  이유는 D-036. `user_id` 로 시작하므로 "내 목록" 조회의 인덱스도 겸한다.
- `ON DELETE CASCADE` — 사용자나 대상이 지워지면 DB 가 즐겨찾기를 함께 지운다 (D-011 의 문제의식).

## `review_destinations` · `review_festivals`

두 테이블은 대상 컬럼만 다르다.

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | 경로의 `{reviewId}`. 테이블마다 따로 매겨진다 |
| `user_id` | BIGINT | NOT NULL, FK → `users.id` `ON DELETE CASCADE` | 작성자. 수정·삭제 권한 판정 기준 |
| `destination_id` / `festival_id` | BIGINT | NOT NULL, FK → 대상 `ON DELETE CASCADE` | |
| `rating` | INT | NOT NULL, `CHECK (1~5)` | 서비스가 먼저 검사한다 (DB 위반은 500 이 되므로) |
| `comment` | TEXT | | 선택 |
| `created_at` | DATETIME | NOT NULL | |
| `updated_at` | DATETIME | | 수정할 때만 채운다. 작성 직후에는 NULL |

- **UNIQUE 없음** — 같은 사용자가 같은 대상에 여러 개 쓸 수 있다 (D-013). 즐겨찾기와 다른 점.
- 목록 조회(`WHERE destination_id = ?`)는 FK 에 InnoDB 가 자동으로 만든 인덱스를 쓴다.

---

## MySQL 에서 주의할 것 (D-026)

SQLite 감각으로 쓰면 조용히 틀리는 것들.

- 컬럼 뒤에 붙인 `REFERENCES` 는 **무시된다.** 테이블 수준 `FOREIGN KEY (...) REFERENCES ...` 로 쓴다.
- `TEXT` 에는 길이 없이 인덱스·UNIQUE 를 걸 수 없다 → `VARCHAR(n)`.
- `CREATE INDEX IF NOT EXISTS` 문법이 없다. FK 컬럼 인덱스는 InnoDB 가 자동으로 만든다.
- `INSERT OR IGNORE` → `INSERT IGNORE`.
- 한글 시드가 깨지지 않으려면 JDBC URL 의 `characterEncoding=UTF-8` 과
  `spring.sql.init.encoding: UTF-8` 이 둘 다 필요하다.

---

## 예정 (아직 없음)

추가할 테이블의 전체 그림은 [재작성 설계 문서의 스키마 절](superpowers/specs/2026-09-17-greenfield-rebuild-design.md)
이 "지도" 로 갖고 있다 (D-020). 다만 그 문서는 destinations · festivals 를 `places` 하나로 합친
D-010 시점에 쓰였으므로, `places` 를 가리키는 부분은 아래처럼 읽는다.

| Task | 테이블 | 비고 |
|---|---|---|
| 3 | `history_destinations` · `history_festivals` | favorite · review 는 위에 있다. 모양은 favorite 와 같다 (id + UNIQUE, D-036) |
| 4 | `courses` · `course_stops` | `course_stops` 가 destination · festival 중 무엇을 가리킬지는 Task 4 에서 정한다. `course_tags` 는 태그를 뺐으므로 없다 (D-024) |
| 5 | `posts` · `post_comments` | |

1차에서 만들지 않는 것 — `tags` 계열(D-024), `collections` · `collection_items` · `achievements`,
`bulk_runs` · `collector_runs` · `audit_log` (D-009).
