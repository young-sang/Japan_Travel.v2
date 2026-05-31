# 데이터베이스 스키마

Japan Travel v2 — **SQLite** (`data/japan_travel.db`)
정의 위치: `backend/src/main/resources/schema.sql`
부팅 시마다 `CREATE TABLE IF NOT EXISTS`로 멱등 적용. JPA 미사용, 손코드 JDBC + RowMapper.

> 마이그레이션 도구 없음 → 스키마 변경 시 DB 파일 삭제 후 재기동.
> 삭제 전 반드시 백업 (`japan_travel.db.bak_YYYYMMDD_HHMMSS`).

---

## 테이블 한눈에 보기

| # | 테이블 | 분류 | 역할 |
|---|---|---|---|
| 1 | `users` | 인증 | 회원 계정 (BCrypt 해시) |
| 2 | `destinations` | 콘텐츠 | 일본 여행지 (Wikipedia 적재) |
| 3 | `festivals` | 콘텐츠 | 일본 축제 (Wikipedia 적재) |
| 4 | `courses` | 콘텐츠 | 여행 코스 (시스템 + 사용자) |
| 5 | `favorites` | 사용자 데이터 | 즐겨찾기 |
| 6 | `reviews` | 사용자 데이터 | 리뷰·평점 |
| 7 | `history` | 사용자 데이터 | 방문 기록 |
| 8 | `collections` | 사용자 데이터 | 모음집 (장소 큐레이션) |
| 9 | `collection_items` | 사용자 데이터 | 모음집 안의 장소들 |
| 10 | `achievements` | 사용자 데이터 | 사용자 업적/배지 |
| 11 | `posts` | 자유게시판 | 게시글 |
| 12 | `post_comments` | 자유게시판 | 댓글 |
| 13 | `bulk_runs` | 운영 | 일괄 수집 실행 (47×2 단위) |
| 14 | `collector_runs` | 운영 | 단일 수집 실행 (도도부현 × 타입) |
| 15 | `audit_log` | 운영 | 관리자/사용자 행동 감사 로그 |

---

## 🔐 인증

### 1. `users` — 회원 계정

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `id` | INTEGER | PK AUTOINCREMENT | 사용자 고유 번호 |
| `username` | TEXT | NOT NULL UNIQUE | 로그인 아이디 |
| `password_hash` | TEXT | NOT NULL | BCrypt로 해시된 비밀번호 |
| `nickname` | TEXT | NOT NULL DEFAULT '관리자' | 화면에 표시되는 이름 |
| `role` | TEXT | NOT NULL DEFAULT 'USER' | 권한 — `USER` 또는 `ADMIN` |
| `avatar_path` | TEXT | | 프로필 사진 경로 |
| `bio` | TEXT | | 자기소개 |
| `default_prefecture` | TEXT | | 즐겨 방문하는 도도부현 (기본 필터) |
| `theme` | TEXT | DEFAULT 'light' | 다크/라이트 UI 모드 |
| `created_at` | TEXT | DEFAULT datetime('now') | 가입 일시 |

> admin 시드(id=1, `admin`/`admin1234`)는 `StartupRunner`가 BCrypt로 생성.

---

## 🗾 콘텐츠

### 2. `destinations` — 여행지

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `id` | INTEGER | PK AUTOINCREMENT | 여행지 고유 번호 |
| `name` | TEXT | NOT NULL | 장소 이름 (예: "오사카성") |
| `prefecture` | TEXT | NOT NULL | 도도부현 (예: "오사카부") |
| `tags` | TEXT | NOT NULL DEFAULT '[]' | 태그 JSON 배열 (예: `["#역사","#관광지"]`) |
| `lat` | REAL | | 위도 |
| `lng` | REAL | | 경도 |
| `image_path` | TEXT | | 대표 이미지 URL |
| `description` | TEXT | | 설명 (Wikipedia 요약) |
| `wiki_title` | TEXT | UNIQUE | Wikipedia 페이지 제목 (중복 적재 방지 키) |
| `last_refreshed_at` | TEXT | | 마지막으로 Wikipedia에서 갱신한 시각 |

**인덱스**: `idx_dest_prefecture (prefecture)` — 도도부현 필터 가속

### 3. `festivals` — 축제

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `id` | INTEGER | PK AUTOINCREMENT | 축제 고유 번호 |
| `name` | TEXT | NOT NULL | 축제 이름 |
| `prefecture` | TEXT | NOT NULL | 개최 도도부현 |
| `month` | INTEGER | | 개최 월 (1~12) |
| `date_text` | TEXT | | 자유 텍스트 날짜 (예: "7월 중순") |
| `image_path` | TEXT | | 대표 이미지 |
| `description` | TEXT | | 설명 |
| `wiki_title` | TEXT | UNIQUE | Wikipedia 제목 (중복 방지) |
| `lat` | REAL | | 위도 |
| `lng` | REAL | | 경도 |
| `last_refreshed_at` | TEXT | | 마지막 갱신 시각 |

**인덱스**: `idx_fest_prefecture (prefecture)`

### 4. `courses` — 여행 코스

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `id` | INTEGER | PK AUTOINCREMENT | 코스 고유 번호 |
| `title` | TEXT | NOT NULL | 코스 제목 |
| `prefecture` | TEXT | NOT NULL | 도도부현 |
| `tags` | TEXT | NOT NULL DEFAULT '[]' | 태그 JSON 배열 |
| `duration` | TEXT | | 소요 시간/일정 (예: "1박 2일") |
| `image_path` | TEXT | | 대표 이미지 |
| `center_lat` | REAL | | 지도 중심 위도 |
| `center_lng` | REAL | | 지도 중심 경도 |
| `timeline_json` | TEXT | NOT NULL DEFAULT '[]' | 일정 JSON (장소·시간·메모) |
| `is_user_created` | INTEGER | DEFAULT 0 | 0=시스템 추천, 1=사용자 작성 |
| `owner_user_id` | INTEGER | | 사용자 코스의 작성자 ID |
| `status` | TEXT | DEFAULT 'published' | 공개 상태 (`draft`/`published`) |
| `created_at` | TEXT | DEFAULT datetime('now') | 생성 시각 |

---

## 💗 사용자 데이터

### 5. `favorites` — 즐겨찾기 (복합 PK)

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `user_id` | INTEGER | PK, NOT NULL | 사용자 ID |
| `target_type` | TEXT | PK, NOT NULL | 대상 종류 (`destination`/`festival`/`course`) |
| `target_id` | INTEGER | PK, NOT NULL | 대상 ID |
| `created_at` | TEXT | DEFAULT datetime('now') | 추가 시각 |

> 같은 사용자가 같은 장소를 두 번 즐겨찾기 불가 (복합 PK).

### 6. `reviews` — 리뷰·평점

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `id` | INTEGER | PK AUTOINCREMENT | 리뷰 고유 번호 |
| `user_id` | INTEGER | NOT NULL | 작성자 ID |
| `target_type` | TEXT | NOT NULL | 대상 종류 |
| `target_id` | INTEGER | NOT NULL | 대상 ID |
| `rating` | INTEGER | NOT NULL | 별점 (1~5) |
| `comment` | TEXT | | 리뷰 본문 |
| `created_at` | TEXT | DEFAULT datetime('now') | 작성 시각 |

**인덱스**: `idx_reviews_target (target_type, target_id)` — 장소별 리뷰 조회 가속

### 7. `history` — 방문 기록 (복합 PK)

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `user_id` | INTEGER | PK, NOT NULL | 사용자 ID |
| `target_type` | TEXT | PK, NOT NULL | 대상 종류 |
| `target_id` | INTEGER | PK, NOT NULL | 대상 ID |
| `visited_at` | TEXT | DEFAULT datetime('now') | 마지막 방문 시각 |

> 같은 장소 재방문 시 새 행 추가가 아닌 `visited_at` 갱신 (복합 PK).

### 8. `collections` — 모음집

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `id` | INTEGER | PK AUTOINCREMENT | 모음집 고유 번호 |
| `user_id` | INTEGER | NOT NULL | 소유자 ID |
| `name` | TEXT | NOT NULL | 모음집 이름 (예: "꼭 가볼 온천") |
| `description` | TEXT | | 설명 |
| `cover_path` | TEXT | | 표지 이미지 |
| `created_at` | TEXT | DEFAULT datetime('now') | 생성 시각 |

### 9. `collection_items` — 모음집 항목 (복합 PK)

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `collection_id` | INTEGER | PK, NOT NULL | 모음집 ID |
| `target_type` | TEXT | PK, NOT NULL | 대상 종류 |
| `target_id` | INTEGER | PK, NOT NULL | 대상 ID |
| `added_at` | TEXT | DEFAULT datetime('now') | 추가 시각 |

### 10. `achievements` — 업적/배지

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `id` | INTEGER | PK AUTOINCREMENT | 업적 기록 고유 번호 |
| `user_id` | INTEGER | NOT NULL | 사용자 ID |
| `code` | TEXT | NOT NULL | 업적 코드 (예: `FIRST_REVIEW`) |
| `unlocked_at` | TEXT | | 달성 시각 |
| `progress_value` | INTEGER | | 진행도 (예: 누적 리뷰 수) |

**제약**: `UNIQUE (user_id, code)` — 같은 업적을 두 번 달성하지 않음.

---

## 📝 자유게시판

### 11. `posts` — 게시글

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `id` | INTEGER | PK AUTOINCREMENT | 게시글 고유 번호 |
| `user_id` | INTEGER | NOT NULL FK→users | 작성자 ID |
| `title` | TEXT | NOT NULL | 제목 |
| `body` | TEXT | NOT NULL | 본문 |
| `created_at` | TEXT | DEFAULT CURRENT_TIMESTAMP | 작성 시각 |
| `updated_at` | TEXT | | 마지막 수정 시각 (수정 시에만 채워짐) |

**인덱스**: `idx_posts_created (created_at DESC)` — 목록 최신순 정렬 가속

### 12. `post_comments` — 댓글

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `id` | INTEGER | PK AUTOINCREMENT | 댓글 고유 번호 |
| `post_id` | INTEGER | NOT NULL FK→posts ON DELETE CASCADE | 부모 게시글 ID |
| `user_id` | INTEGER | NOT NULL FK→users | 작성자 ID |
| `body` | TEXT | NOT NULL | 댓글 본문 |
| `created_at` | TEXT | DEFAULT CURRENT_TIMESTAMP | 작성 시각 |

**인덱스**: `idx_post_comments_post (post_id, created_at)` — 글 상세에서 댓글 정렬 조회
**삭제 동작**: 게시글 삭제 시 댓글 자동 삭제 (CASCADE)

---

## 🛠 운영

### 13. `bulk_runs` — 일괄 수집 실행

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `id` | INTEGER | PK AUTOINCREMENT | 실행 고유 번호 |
| `started_at` | TEXT | NOT NULL | 시작 시각 |
| `finished_at` | TEXT | | 종료 시각 |
| `status` | TEXT | NOT NULL | 상태 — `running`/`success`/`partial`/`failed`/`aborted` |
| `total_tasks` | INTEGER | NOT NULL | 총 작업 수 (보통 94 = 47×2) |
| `tasks_success` | INTEGER | DEFAULT 0 | 성공한 작업 수 |
| `tasks_partial` | INTEGER | DEFAULT 0 | 일부 성공(부분 실패) 작업 수 |
| `tasks_failed` | INTEGER | DEFAULT 0 | 실패한 작업 수 |
| `tasks_aborted` | INTEGER | DEFAULT 0 | 중단된 작업 수 |
| `notes` | TEXT | | 자유 메모 |

### 14. `collector_runs` — 단일 수집 실행

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `id` | INTEGER | PK AUTOINCREMENT | 실행 고유 번호 |
| `type` | TEXT | NOT NULL | 수집 종류 (`destination`/`festival`) |
| `prefecture` | TEXT | | 대상 도도부현 |
| `source` | TEXT | NOT NULL | 출처 (예: `wikipedia`) |
| `status` | TEXT | NOT NULL | `running`/`success`/`partial`/`failed`/`empty`/`aborted` |
| `items_added` | INTEGER | DEFAULT 0 | 신규 추가 항목 수 |
| `items_updated` | INTEGER | DEFAULT 0 | 갱신 항목 수 |
| `items_failed` | INTEGER | DEFAULT 0 | 실패 항목 수 |
| `errors_json` | TEXT | | 실패 항목 상세 JSON |
| `started_at` | TEXT | DEFAULT datetime('now') | 시작 시각 |
| `finished_at` | TEXT | | 종료 시각 |
| `bulk_run_id` | INTEGER | FK→bulk_runs | 묶음 실행 ID (단독 실행 시 NULL) |
| `last_heartbeat_at` | TEXT | | 마지막 작업 시각 (좀비 감지용) |
| `stage` | TEXT | | 현재 단계 (`list`/`summary`/`upsert`) |

**인덱스**: `idx_runs_bulk (bulk_run_id)`, `idx_runs_heartbeat (status, last_heartbeat_at)`
**좀비 감지**: `ZombieWatcher`가 heartbeat이 멈춘 `running` 작업을 `aborted`로 정리

### 15. `audit_log` — 감사 로그

| 컬럼 | 타입 | 제약 | 한글 설명 |
|---|---|---|---|
| `id` | INTEGER | PK AUTOINCREMENT | 로그 고유 번호 |
| `user_id` | INTEGER | | 사용자 ID (로그인 실패 등은 NULL) |
| `username` | TEXT | | 사용자명 (계정 삭제 후에도 흔적 보존용) |
| `action` | TEXT | NOT NULL | 행동 코드 (아래 표 참조) |
| `target_type` | TEXT | | 대상 종류 |
| `target_id` | INTEGER | | 대상 ID |
| `detail` | TEXT | | 자유 메모/JSON |
| `created_at` | TEXT | DEFAULT CURRENT_TIMESTAMP | 기록 시각 |

**인덱스**: `idx_audit_user (user_id, created_at DESC)`, `idx_audit_action (action, created_at DESC)`

#### `action` 코드표

| 분류 | 코드 | 의미 |
|---|---|---|
| 인증 | `LOGIN_SUCCESS` | 로그인 성공 |
| 인증 | `LOGIN_FAIL` | 로그인 실패 |
| 인증 | `SIGNUP` | 회원가입 |
| 인증 | `LOGOUT` | 로그아웃 |
| 리뷰 | `REVIEW_CREATE` | 리뷰 작성 |
| 리뷰 | `REVIEW_UPDATE` | 리뷰 수정 |
| 리뷰 | `REVIEW_DELETE` | 리뷰 삭제 |
| 코스 | `COURSE_CREATE` | 코스 생성 |
| 코스 | `COURSE_UPDATE` | 코스 수정 |
| 코스 | `COURSE_DELETE` | 코스 삭제 |
| 수집기 | `COLLECTOR_RUN` | 단일 수집 실행 |
| 수집기 | `COLLECTOR_BULK` | 일괄 수집 실행 |
| 사용자 | `USER_ROLE_CHANGE` | 관리자가 역할 변경 |
| 사용자 | `USER_DELETE` | 관리자가 계정 삭제 |
| 콘텐츠 | `CONTENT_CREATE` | 관리자가 여행지/축제 추가 |
| 콘텐츠 | `CONTENT_UPDATE` | 관리자가 여행지/축제 수정 |
| 콘텐츠 | `CONTENT_DELETE` | 관리자가 여행지/축제 삭제 |
| 게시판 | `POST_DELETE` | 관리자가 글 삭제 |
| 게시판 | `POST_COMMENT_DELETE` | 관리자가 댓글 삭제 |

---

## 관계도

```
users ─┬─< posts                (user_id, FK)
       ├─< post_comments        (user_id, FK)
       ├─< reviews              (user_id, 논리적)
       ├─< favorites            (user_id, 논리적)
       ├─< history              (user_id, 논리적)
       ├─< collections          (user_id, 논리적)
       ├─< achievements         (user_id, 논리적)
       └─< courses              (owner_user_id, 논리적)

posts ──< post_comments         (post_id, FK CASCADE)

bulk_runs ──< collector_runs    (bulk_run_id, FK)

destinations/festivals/courses ↔ users
   via favorites / reviews / history / collection_items
   (target_type + target_id 다형 참조)
```

> **명시적 FK 선언**: `posts`, `post_comments`, `collector_runs`
> 나머지 사용자 데이터(`favorites`/`reviews`/`history` 등)는 FK 없이 애플리케이션 레벨로 무결성 관리.

---

## 운영 메모

| 항목 | 내용 |
|---|---|
| 마이그레이션 도구 | 없음 (Flyway/Liquibase 미사용) |
| 스키마 변경 절차 | `data/japan_travel.db` + `-shm` + `-wal` 삭제 → 재기동 |
| 백업 규칙 | 삭제 전 반드시 `japan_travel.db.bak_YYYYMMDD_HHMMSS` 사본 생성 |
| 시드 데이터 | 없음. admin 1명만 `StartupRunner`가 자동 생성 |
| 콘텐츠 적재 | 관리자 페이지 → 수집 현황 → Wikipedia 47×2 일괄 수집 |
| JSON 컬럼 | `destinations.tags`, `festivals` 없음, `courses.tags`, `courses.timeline_json`, `collector_runs.errors_json`, `audit_log.detail` |
| 시간 형식 | TEXT (`YYYY-MM-DD HH:MM:SS`), `datetime('now')` 또는 `CURRENT_TIMESTAMP` |
