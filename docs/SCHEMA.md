# 데이터베이스 스키마

Japan Travel v2 — **SQLite** (`data/japan_travel.db`)
정의 위치: `backend/src/main/resources/schema.sql`
부팅 시마다 `CREATE TABLE IF NOT EXISTS`로 멱등 적용 (마이그레이션 도구 없음 — 스키마 변경 시 DB 파일 삭제 후 재기동).
JPA 미사용, 손코드 JDBC + RowMapper.

## 테이블 목록

| 분류 | 테이블 | 용도 |
|---|---|---|
| 인증 | `users` | 계정 |
| 콘텐츠 | `destinations` | 여행지 (Wikipedia 적재) |
| 콘텐츠 | `festivals` | 축제 (Wikipedia 적재) |
| 콘텐츠 | `courses` | 여행 코스 (사용자/시스템) |
| 사용자 데이터 | `favorites` | 즐겨찾기 |
| 사용자 데이터 | `reviews` | 리뷰·평점 |
| 사용자 데이터 | `history` | 방문 기록 |
| 사용자 데이터 | `collections` | 모음집 |
| 사용자 데이터 | `collection_items` | 모음집 항목 |
| 사용자 데이터 | `achievements` | 업적 |
| 자유게시판 | `posts` | 게시글 |
| 자유게시판 | `post_comments` | 댓글 |
| 운영 | `bulk_runs` | 대량 수집 실행 |
| 운영 | `collector_runs` | 단일 수집 실행 |
| 운영 | `audit_log` | 감사 로그 |

---

## 🔐 인증

### `users`
```sql
id              INTEGER PK AUTOINCREMENT
username        TEXT NOT NULL UNIQUE
password_hash   TEXT NOT NULL          -- BCrypt
nickname        TEXT NOT NULL DEFAULT '관리자'
role            TEXT NOT NULL DEFAULT 'USER'   -- USER | ADMIN
avatar_path     TEXT
bio             TEXT
default_prefecture TEXT
theme           TEXT DEFAULT 'light'   -- light | dark (UI 설정)
created_at      TEXT DEFAULT (datetime('now'))
```
- admin 시드(id=1, admin/admin1234)는 `StartupRunner`가 BCrypt로 생성.

---

## 🗾 콘텐츠

### `destinations`
```sql
id                 INTEGER PK AUTOINCREMENT
name               TEXT NOT NULL
prefecture         TEXT NOT NULL
tags               TEXT NOT NULL DEFAULT '[]'   -- JSON 배열
lat, lng           REAL
image_path         TEXT
description        TEXT
wiki_title         TEXT UNIQUE                  -- Wikipedia 제목 (중복 방지 키)
last_refreshed_at  TEXT
```
- 인덱스: `idx_dest_prefecture (prefecture)`
- 적재: `WikipediaCollector`가 도도부현별 카테고리 수집 → `wiki_title` 기반 upsert.

### `festivals`
```sql
id                 INTEGER PK AUTOINCREMENT
name               TEXT NOT NULL
prefecture         TEXT NOT NULL
month              INTEGER          -- 1~12
date_text          TEXT             -- "7월 중순" 등 자유 텍스트
image_path         TEXT
description        TEXT
wiki_title         TEXT UNIQUE
lat, lng           REAL
last_refreshed_at  TEXT
```
- 인덱스: `idx_fest_prefecture (prefecture)`

### `courses`
```sql
id              INTEGER PK AUTOINCREMENT
title           TEXT NOT NULL
prefecture      TEXT NOT NULL
tags            TEXT NOT NULL DEFAULT '[]'   -- JSON
duration        TEXT              -- "1박 2일" 등
image_path      TEXT
center_lat,
center_lng      REAL
timeline_json   TEXT NOT NULL DEFAULT '[]'   -- 일정 JSON
is_user_created INTEGER DEFAULT 0  -- 0=시스템, 1=사용자
owner_user_id   INTEGER            -- 사용자 코스 작성자
status          TEXT DEFAULT 'published'
created_at      TEXT DEFAULT (datetime('now'))
```

---

## 💗 사용자 데이터

### `favorites` (PK 복합)
```sql
user_id      INTEGER NOT NULL
target_type  TEXT NOT NULL    -- destination | festival | course
target_id    INTEGER NOT NULL
created_at   TEXT DEFAULT (datetime('now'))
PRIMARY KEY (user_id, target_type, target_id)
```

### `reviews`
```sql
id           INTEGER PK AUTOINCREMENT
user_id      INTEGER NOT NULL
target_type  TEXT NOT NULL
target_id    INTEGER NOT NULL
rating       INTEGER NOT NULL    -- 1~5
comment      TEXT
created_at   TEXT DEFAULT (datetime('now'))
```
- 인덱스: `idx_reviews_target (target_type, target_id)`

### `history` (PK 복합 — 같은 대상 재방문 시 갱신)
```sql
user_id      INTEGER NOT NULL
target_type  TEXT NOT NULL
target_id    INTEGER NOT NULL
visited_at   TEXT DEFAULT (datetime('now'))
PRIMARY KEY (user_id, target_type, target_id)
```

### `collections`
```sql
id           INTEGER PK AUTOINCREMENT
user_id      INTEGER NOT NULL
name         TEXT NOT NULL
description  TEXT
cover_path   TEXT
created_at   TEXT DEFAULT (datetime('now'))
```

### `collection_items` (PK 복합)
```sql
collection_id  INTEGER NOT NULL
target_type    TEXT NOT NULL
target_id      INTEGER NOT NULL
added_at       TEXT DEFAULT (datetime('now'))
PRIMARY KEY (collection_id, target_type, target_id)
```

### `achievements`
```sql
id              INTEGER PK AUTOINCREMENT
user_id         INTEGER NOT NULL
code            TEXT NOT NULL       -- 업적 코드
unlocked_at     TEXT
progress_value  INTEGER
UNIQUE (user_id, code)
```

---

## 📝 자유게시판

### `posts`
```sql
id          INTEGER PK AUTOINCREMENT
user_id     INTEGER NOT NULL  REFERENCES users(id)
title       TEXT NOT NULL
body        TEXT NOT NULL
created_at  TEXT DEFAULT CURRENT_TIMESTAMP
updated_at  TEXT
```
- 인덱스: `idx_posts_created (created_at DESC)` — 목록 정렬용

### `post_comments`
```sql
id          INTEGER PK AUTOINCREMENT
post_id     INTEGER NOT NULL  REFERENCES posts(id) ON DELETE CASCADE
user_id     INTEGER NOT NULL  REFERENCES users(id)
body        TEXT NOT NULL
created_at  TEXT DEFAULT CURRENT_TIMESTAMP
```
- 인덱스: `idx_post_comments_post (post_id, created_at)`
- 글 삭제 시 댓글 자동 삭제 (CASCADE).

---

## 🛠 운영

### `bulk_runs` — 대량 수집 실행 단위
```sql
id              INTEGER PK AUTOINCREMENT
started_at      TEXT NOT NULL
finished_at     TEXT
status          TEXT NOT NULL     -- running | success | partial | failed | aborted
total_tasks     INTEGER NOT NULL
tasks_success   INTEGER NOT NULL DEFAULT 0
tasks_partial   INTEGER NOT NULL DEFAULT 0
tasks_failed    INTEGER NOT NULL DEFAULT 0
tasks_aborted   INTEGER NOT NULL DEFAULT 0
notes           TEXT
```

### `collector_runs` — 개별 (도도부현 × 타입) 수집
```sql
id                  INTEGER PK AUTOINCREMENT
type                TEXT NOT NULL     -- destination | festival
prefecture          TEXT
source              TEXT NOT NULL     -- 'wikipedia' 등
status              TEXT NOT NULL     -- running | success | partial | failed | empty | aborted
items_added         INTEGER DEFAULT 0
items_updated       INTEGER DEFAULT 0
items_failed        INTEGER DEFAULT 0
errors_json         TEXT              -- 실패 항목 JSON
started_at          TEXT DEFAULT (datetime('now'))
finished_at         TEXT
bulk_run_id         INTEGER  REFERENCES bulk_runs(id)
last_heartbeat_at   TEXT              -- ZombieWatcher가 감시
stage               TEXT              -- 'list' | 'summary' | 'upsert'
```
- 인덱스: `idx_runs_bulk (bulk_run_id)`, `idx_runs_heartbeat (status, last_heartbeat_at)`

### `audit_log`
```sql
id           INTEGER PK AUTOINCREMENT
user_id      INTEGER              -- NULL 가능 (로그인 실패 등)
username     TEXT                 -- 사용자 삭제 후에도 흔적 보존
action       TEXT NOT NULL
target_type  TEXT
target_id    INTEGER
detail       TEXT                 -- 자유 텍스트 (JSON 가능)
created_at   TEXT DEFAULT CURRENT_TIMESTAMP
```
- 인덱스: `idx_audit_user (user_id, created_at DESC)`, `idx_audit_action (action, created_at DESC)`
- 사용되는 `action` 값:

| 분류 | action |
|---|---|
| 인증 | `LOGIN_SUCCESS`, `LOGIN_FAIL`, `SIGNUP`, `LOGOUT` |
| 리뷰 | `REVIEW_CREATE`, `REVIEW_UPDATE`, `REVIEW_DELETE` |
| 코스 | `COURSE_CREATE`, `COURSE_UPDATE`, `COURSE_DELETE` |
| 수집기 | `COLLECTOR_RUN`, `COLLECTOR_BULK` |
| 사용자 | `USER_ROLE_CHANGE`, `USER_DELETE` |
| 콘텐츠 | `CONTENT_CREATE`, `CONTENT_UPDATE`, `CONTENT_DELETE` |
| 게시판 | `POST_DELETE`, `POST_COMMENT_DELETE` |

---

## 외래키 / 참조 관계

```
users ─┬─< posts            (user_id)
       ├─< post_comments    (user_id)
       ├─< reviews          (user_id)              [논리적]
       ├─< favorites        (user_id)              [논리적]
       ├─< history          (user_id)              [논리적]
       ├─< courses          (owner_user_id)        [논리적]
       └─< collections      (user_id)              [논리적]

posts ──< post_comments     (post_id, CASCADE)

bulk_runs ──< collector_runs (bulk_run_id)

destinations, festivals, courses
   └─ 다대다 ↔ users via favorites / reviews / history / collection_items
      (target_type + target_id로 polymorphic 참조)
```

명시적 FK 선언이 있는 테이블: `posts`, `post_comments`, `collector_runs`.
나머지 사용자 데이터(`favorites`/`reviews`/`history` 등)는 FK 없이 application-side로 무결성을 관리.

---

## 운영 메모

- **마이그레이션 도구 없음**: Flyway/Liquibase 미사용. 스키마 변경 시 `data/japan_travel.db` (+ `-shm`, `-wal`) 삭제 후 백엔드 재기동 필요.
- **백업 권장**: DB 삭제 전 사본을 만들어 둘 것 (`CLAUDE.md` 정책).
- **시드**: 코드/SQL 시드 없음. admin 1명만 `StartupRunner`가 생성. 나머지 콘텐츠는 관리자 페이지의 Wikipedia 수집기로 적재.
- **JSON 컬럼**: `destinations.tags`, `courses.tags`, `courses.timeline_json`, `collector_runs.errors_json`, `audit_log.detail`은 SQLite 텍스트로 JSON 직렬화 저장. 애플리케이션이 파싱.
- **시간 형식**: 모두 SQLite TEXT (`YYYY-MM-DD HH:MM:SS`), `datetime('now')` 또는 `CURRENT_TIMESTAMP` 기본값.
