# 백엔드 그린필드 재작성 설계

작성일: 2026-09-17
대상: `backend/` 전체 + `schema.sql` + (2차) `frontend/`

> 이 문서는 `2026-09-15-backend-rebuild-design.md` 와 `2026-09-16-task1-destination.md` 를
> **대체한다.** 그 둘은 "프론트·API 계약·스키마는 고정, 백엔드 내부만 새로 짓는다" 가
> 전제였다. 전제 셋이 모두 풀렸으므로 폐기한다.

## 목적

기존 앱을 **기능 명세서로 삼아** 백엔드와 DB 스키마를 처음부터 새로 짓는다.
프론트엔드는 새 백엔드에 맞춰 나중에 고친다.

1순위 목표는 **코드 이해**다. 구조 개선은 그 수단이다.

**코드는 사용자가 직접 쓴다.** Claude 는 가이드·검증·리뷰를 맡는다. 명세서를 던지는
대신 계층 단위로 "왜 이렇게 하는가 → 무엇을 쓸 것인가 → 직접 작성 → 확인" 순으로
진행한다. 구현 코드는 사용자가 막혔을 때만 제공한다.

## 무엇이 자유롭고 무엇이 제약인가

| 자유 | 제약 |
|---|---|
| DB 스키마 전체 | 기술 스택 (Spring Boot 3.3.4 · Java 17 · Gradle · JPA · SQLite) |
| API 경로 · 요청/응답 모양 · 상태코드 | 기존 앱의 **기능**이 무엇을 하는지 (명세서 역할) |
| 패키지 · 클래스 · 메서드 이름 | 프로젝트 `CLAUDE.md` 방침 (보안·로깅·검증·재시도·성능 추가 금지) |
| 프론트엔드 (2차에서 맞춤) | |

### 데이터가 더미라는 사실의 의미

현재 DB 의 내용은 테스트용 더미다. 따라서:

- **스키마를 갈아엎는 비용이 거의 0이다.** `schema.sql` 고치고 db 파일 지우고 다시
  시드하면 끝난다. 지금 완벽한 설계를 할 필요가 없고, 만들다가 잘못을 발견하면 바꾼다.
- 기존 id 를 보존할 이유가 없다.
- 마이그레이션 스크립트가 필요 없다. 대신 **일회성 시드 변환**을 한다 (아래 "시드").

## 범위

### 1차 — 지금 짓는 것 (도메인 10개)

| 도메인 | 내용 |
|---|---|
| `user` | 가입 · 로그인 · 로그아웃 · 내 정보 |
| `place` | 관광지·축제 목록 · 상세 · 필터 (둘을 한 테이블로 통합) |
| `course` | 코스 목록 · 상세 · 사용자 코스 CRUD |
| `favorite` | 즐겨찾기 |
| `review` | 리뷰 |
| `history` | 방문 기록 |
| `post` | 게시판 글 + 댓글 |
| `search` | 통합 검색 |
| `weather` | 날씨 위젯 (Open-Meteo 프록시) |
| `exchange` | 환율 위젯 (Frankfurter 프록시) |

### 제외 — 나중에 필요한 것만 추려서 다시

- **관리자 기능 전부** (화면·API·`/api/admin/**`)
- **Wikipedia 수집기** (`collector`, `client` 858줄)
- **수집 모니터링** (`bulk_runs` · `collector_runs` · `ZombieWatcher` · 수집 매트릭스 ·
  `BulkProgress` 360줄 · `CollectionStatus` 470줄)
- **감사 로그** (`audit_log`)
- **캐시 통계/무효화 창구** (`/api/admin/cache/**`)
- **관리자 대시보드 통계** (`/api/admin/stats`)
- **`collections` · `collection_items` · `achievements`** — 0행이고 자바 코드에서
  한 번도 참조하지 않는 죽은 테이블
- **코스 즐겨찾기·리뷰·방문기록** — 아래 "활동 대상" 참조

엔드포인트가 약 55개에서 약 30개로 줄어든다.

`Top100` · `Hotplace` · `News` · `Tip` 프론트 페이지는 API 호출이 0개인 순수 정적
화면이므로 애초에 백엔드 범위 밖이다.

`role` 컬럼은 남긴다. 나중에 관리자 기능을 붙일 때 필요하고 컬럼 하나는 비용이 아니다.
다만 1차에서는 권한 분기를 쓰지 않는다.

## 스키마

```sql
PRAGMA foreign_keys = ON;   -- SQLite 는 이것 없이는 FK 를 전부 무시한다

CREATE TABLE users (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  username      TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  nickname      TEXT NOT NULL,
  role          TEXT NOT NULL DEFAULT 'USER' CHECK (role IN ('USER','ADMIN')),
  created_at    TEXT NOT NULL
);

CREATE TABLE places (
  id                INTEGER PRIMARY KEY AUTOINCREMENT,
  kind              TEXT NOT NULL CHECK (kind IN ('destination','festival')),
  name              TEXT NOT NULL,
  prefecture        TEXT NOT NULL,
  lat               REAL,
  lng               REAL,
  image_path        TEXT,
  description       TEXT,
  wiki_title        TEXT UNIQUE,
  last_refreshed_at TEXT,
  month             INTEGER,   -- 축제만
  date_text         TEXT,      -- 축제만
  CHECK (kind <> 'destination' OR (month IS NULL AND date_text IS NULL))
);
CREATE INDEX idx_places_kind_pref ON places(kind, prefecture);

CREATE TABLE place_tags (
  place_id INTEGER NOT NULL REFERENCES places(id) ON DELETE CASCADE,
  tag      TEXT NOT NULL,
  PRIMARY KEY (place_id, tag)
);
CREATE INDEX idx_place_tags_tag ON place_tags(tag);

CREATE TABLE courses (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  title         TEXT NOT NULL,
  prefecture    TEXT NOT NULL,
  duration      TEXT,
  image_path    TEXT,
  center_lat    REAL,
  center_lng    REAL,
  owner_user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,  -- NULL = 기본 제공 코스
  status        TEXT NOT NULL DEFAULT 'published'
                CHECK (status IN ('draft','published')),
  created_at    TEXT NOT NULL
);

CREATE TABLE course_tags (
  course_id INTEGER NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  tag       TEXT NOT NULL,
  PRIMARY KEY (course_id, tag)
);

CREATE TABLE course_stops (
  course_id INTEGER NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  seq       INTEGER NOT NULL,
  place_id  INTEGER NOT NULL REFERENCES places(id) ON DELETE CASCADE,
  time_text TEXT,                     -- '09:00' 표시용
  PRIMARY KEY (course_id, seq)
);

CREATE TABLE favorites (
  user_id    INTEGER NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
  place_id   INTEGER NOT NULL REFERENCES places(id) ON DELETE CASCADE,
  created_at TEXT NOT NULL,
  PRIMARY KEY (user_id, place_id)
);

CREATE TABLE reviews (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id    INTEGER NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
  place_id   INTEGER NOT NULL REFERENCES places(id) ON DELETE CASCADE,
  rating     INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment    TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT
);
CREATE INDEX idx_reviews_place ON reviews(place_id);
-- UNIQUE(user_id, place_id) 는 걸지 않는다. 옛 코드가 중복 검사 없이 INSERT 하므로
-- 한 사용자가 같은 장소에 리뷰를 여러 개 쓸 수 있다. 그 동작을 유지한다.

CREATE TABLE history (
  user_id    INTEGER NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
  place_id   INTEGER NOT NULL REFERENCES places(id) ON DELETE CASCADE,
  visited_at TEXT NOT NULL,
  PRIMARY KEY (user_id, place_id)     -- 대상당 마지막 방문 1행 (현재 동작과 동일)
);

CREATE TABLE posts (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  title      TEXT NOT NULL,
  body       TEXT NOT NULL,
  created_at TEXT NOT NULL,
  updated_at TEXT
);
CREATE INDEX idx_posts_created ON posts(created_at DESC);

CREATE TABLE post_comments (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  post_id    INTEGER NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
  user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  body       TEXT NOT NULL,
  created_at TEXT NOT NULL
);
CREATE INDEX idx_post_comments_post ON post_comments(post_id, created_at);
```

### 결정 근거

**관광지와 축제를 `places` 한 테이블로 합친다.**
옛 `destinations` 와 `festivals` 는 컬럼이 거의 같았다 — `name` · `prefecture` · `lat` ·
`lng` · `image_path` · `description` · `wiki_title` · `last_refreshed_at`. 축제에만
`month` · `date_text` 가 더 있었다. 사실상 같은 테이블이었다.

대가는 `month` · `date_text` 에 `NOT NULL` 을 걸 수 없다는 것이다. 관광지 행에서는
NULL 이기 때문이다. 반대 방향(관광지 행에 축제 컬럼이 채워지는 것)은 `CHECK` 로 막는다.

**활동 대상은 `places` 뿐이다 — 코스는 제외한다.**
즐겨찾기·리뷰·방문기록이 "관광지·축제·코스 중 아무거나"를 가리켜야 하면 FK 를 걸 수
없다. FK 컬럼 하나는 테이블 하나만 가리키기 때문이다. 옛 스키마는 그래서
`target_type` + `target_id` 로 우회했고, 그 결과 관광지를 지울 때 자바가 손으로
`favorites` · `reviews` · `history` 를 지우고 있었다 — **DB 가 할 일을 자바가 대신했다.**

해결책으로 레지스트리 상위 테이블(class table inheritance), nullable FK + CHECK,
테이블 분리, 단일 테이블 상속, 트리거를 검토했다. 모두 동작하지만 모두 새로 배울
것을 요구한다. 1순위 목표가 코드 이해이고 DB 를 붙이는 것 자체가 처음이므로,
**이해 안 되는 구조 위에 10개 도메인을 쌓지 않기로 했다.**

대상을 `places` 로 한정하면 문제가 사라진다. 평범한 FK 하나씩이고, CHECK 도 NULL
컬럼도 새 개념도 없다. 코스 즐겨찾기가 나중에 필요해지면 `course_favorites` 테이블을
**추가만** 하면 된다 — 기존 테이블을 건드리지 않는다.

**태그를 JSON TEXT 에서 행으로 뺀다.**
옛 스키마는 `tags TEXT NOT NULL DEFAULT '[]'` 였고 검색이 `LIKE '%"온천"%'` 였다.
`place_tags` 로 빼면 `WHERE tag = '온천'` 이 되고 인덱스가 먹는다. JPA 쪽에서도
`AttributeConverter` 가 필요 없어진다.

**코스 타임라인을 `course_stops` 로 뺀다.**
옛 `timeline_json` 은 `title` · `desc` · `lat` · `lng` 를 통째로 복사해 두고 있었고,
그래서 코스와 장소 사이에 링크가 없었다. 확인해 보니 `CourseBuilder.jsx` 는 정류장을
만들 때 항상 실제 장소에서 가져오며 `targetId` 를 이미 들고 있다. 따라서
`place_id NOT NULL` 로 참조만 하면 되고, 표시용 이름·좌표는 조인으로 얻는다.

**`is_user_created` 를 버린다.** `owner_user_id IS NULL` 로 알 수 있으므로 중복이다.

**`users` 의 죽은 컬럼 4개를 버린다.** `avatar_path` · `bio` · `default_prefecture` ·
`theme` — 백엔드가 읽지도 쓰지도 않는다 (`theme` 은 grep 결과 자바 코드에 아예 없다).

### 날짜 — 확정하지 않고 첫 도메인에서 검증한다

모든 시각 컬럼은 TEXT 에 **ISO-8601 UTC** 로 저장한다. 옛 스키마는
`datetime('now')` 가 만드는 `yyyy-MM-dd HH:mm:ss` (UTC) 였다.

자바 쪽 타입은 `Instant` 매핑을 **시도**하되, SQLite 는 날짜 타입이 없고 이 프로젝트는
Hibernate 커뮤니티 dialect 를 쓰므로 실제 저장 결과를 Task 1 에서 눈으로 확인한
다음 확정한다. 예상과 다르면 `String` 으로 둔다. 여기서 단언하지 않는다.

## 기술 스택

| 항목 | 값 |
|---|---|
| 빌드 | Gradle 8.14.5 (Groovy DSL) |
| 프레임워크 | Spring Boot 3.3.4 · Java 17 |
| 영속성 | Spring Data JPA + Hibernate 6.5.3 |
| DB | SQLite (`hibernate-community-dialects` 의 `SQLiteDialect`) |

`ddl-auto: none` · `open-in-view: false`. 스키마의 주인은 `schema.sql` 이고 Hibernate 는
스키마를 건드리지 않는다. `open-in-view: false` 는 엔티티가 컨트롤러까지 흘러나가면
예외로 알려주는 안전장치다.

**JDBC URL 에 `foreign_keys=on` 을 넣어야 한다.** SQLite 는 연결마다 이 PRAGMA 가
켜져 있어야 FK 를 강제한다. 빠뜨리면 FK 를 다 걸어놓고도 cascade 가 동작하지 않는다.

## 아키텍처

도메인마다 5계층, 계층마다 하위 패키지.

```
com.japantravel.<domain>/
  controller/   <Domain>Controller
  service/      <Domain>Service
  repository/   <Domain>Repository        (JpaRepository)
  entity/       <Domain>                  (@Entity)
  dtos/         <Domain>Dtos              (record 컨테이너)
```

의존 방향은 한 방향이다. `controller → service → repository`. 역방향과 건너뛰기는 없다.

| 계층 | 하는 일 | 하지 않는 일 |
|---|---|---|
| `controller` | HTTP 만. 경로 매핑, 파라미터 바인딩, 결과 반환 | 조건 분기, 권한 판정, 쿼리 |
| `service` | 규칙 전부. 소유권 판정, 예외 발생, 엔티티→DTO 변환 | HTTP 타입(`ResponseEntity`) 취급 |
| `repository` | 조회·저장만 | 규칙 판단 |
| `entity` | 테이블 매핑 | API 계약 노출 |
| `dtos` | API 계약 | 로직 |

### 규칙 3개

1. **다른 도메인의 Repository 를 직접 부르지 않는다.** 필요하면 그쪽 `Service` 를 부른다.
2. **엔티티는 도메인 밖으로 나가지 않는다.** Service 가 DTO 로 변환해서 넘긴다.
   엔티티를 그대로 반환하면 API 계약이 스키마에 묶이고 `open-in-view: false` 에서
   직렬화 중 예외가 난다.
3. **컨트롤러에 `if` 가 없다.** 조건은 전부 Service 에 있고, 실패는 예외로 표현된다.

### 공통 — `common/`

```
common/error/   NotFoundException · ConflictException · ForbiddenException
common/web/     ApiExceptionHandler
```

이미 작성 완료. 현재 `com.japantravel._repo.common` 아래 있으므로 **Task 0 에서
`com.japantravel.common` 으로 옮긴다** (`_repo` 는 옛 코드와의 공존을 위한 임시
접두사였는데, 옛 코드를 통째로 지우므로 존재 이유가 없어졌다).

`@RestControllerAdvice` 하나가 예외를 상태코드로
번역한다: `NotFoundException` → 404, `ForbiddenException` → 403, `ConflictException` → 409.
`ConflictException` 은 선택적 `code` 와 `extra` 를 들고 다니며 `body()` 로 응답 맵을
직접 조립한다. 핸들러에 `if` 나 `instanceof` 가 들어가면 설계가 깨진 것이다.

`@ExceptionHandler(Exception.class)` catch-all 은 만들지 않는다.

### 도메인 간 의존

```
course   → place    (Service — 정류장 정보 조회)
search   → place · course  (Service)
favorite · review · history → place  (@ManyToOne 읽기 전용 참조 허용)
post     → user     (@ManyToOne 읽기 전용 참조 — 작성자 닉네임 표시)
```

전부 단방향이고 순환이 없다.

## 옛 코드 처리

새 백엔드에는 소비자가 없다 (프론트는 2차에서 맞춘다). 따라서 **전환 기간을 두지 않고
옛 백엔드 코드를 통째로 지운다.** `Legacy*` 이름변경도, 빈 이름 충돌 회피도 필요 없다.

지우기 전에 태그를 찍어 되돌아갈 지점을 남긴다.

```bash
git tag pre-greenfield
```

옛 코드는 git 히스토리에 남아 있고, 기능 명세가 필요할 때 `git show pre-greenfield:<path>`
로 읽는다.

`data/japan_travel.db` 원본은 **지우지 않는다.** 시드 변환의 원천이다
(프로젝트 `CLAUDE.md`: db 삭제 전 백업 필수).

## 시드

수집기가 1차 범위 밖이므로 `places` 를 채울 방법이 시드뿐이다. 개발 내내 빈 목록으로
작업하는 것을 피하기 위해 **스키마 확정 직후** 시드를 만든다.

`scripts/seed_from_old_db.py` (일회성):

```
옛 data/japan_travel.db  →  읽기 전용으로 연다
  destinations 118건  →  places (kind='destination')
  festivals     26건  →  places (kind='festival')
  tags JSON           →  place_tags 행으로 펼침
  users               →  users (password_hash 그대로)
  posts · post_comments · courses · favorites · reviews · history  →  새 id 로 재매핑
결과  →  backend/src/main/resources/seed.sql  또는 새 db 파일 직접 생성
```

관광지 118건·축제 26건은 더미가 아니라 Wikipedia 에서 온 진짜 내용이므로 그대로 쓸
값어치가 있다.

## 검증

**옛 방식(`baseline/` diff)은 더 이상 쓸 수 없다.** 응답 모양이 바뀌므로 비교 대상이
없다. `baseline/` 은 참고용으로만 남긴다.

대신 도메인마다 스모크 스크립트를 하나씩 쌓는다.

```
scripts/smoke/<domain>.sh     curl 로 엔드포인트를 두드리고 상태코드·건수를 확인
scripts/smoke/all.sh          지금까지 만든 전부를 한 번에
```

도메인을 끝낼 때마다 `all.sh` 가 통과해야 한다. 이것이 유일한 회귀 안전망이므로
**새 도메인을 시작하기 전에 반드시 이전 것들이 여전히 도는지 확인한다.**

### 매 도메인 공통 절차

```bash
netstat -ano | grep ":8080" | grep LISTEN   # 옛 프로세스가 잡고 있으면 검증이 거짓말을 한다
cd backend && ./gradlew compileJava
cd backend && ./gradlew bootRun
bash scripts/smoke/all.sh
git commit                                   # 도메인당 1커밋
```

### 환경 함정 2개

**① Windows Git Bash 에서 curl 에 한글을 그대로 쓰면 서버에 온전히 도달하지 않는다.**

```bash
curl "localhost:8080/api/places?prefecture=도쿄도"                       # → 400
curl "localhost:8080/api/places?prefecture=%EB%8F%84%EC%BF%84%EB%8F%84"  # → 200
```

인코딩이 필요하면 만든다:
`python -c "import urllib.parse;print(urllib.parse.quote('오사카부'))"`

응답 확인도 콘솔이 한글을 깨뜨리므로 `head -c` 로 읽지 말고 Python 으로 판정한다.
`PYTHONIOENCODING=utf-8` 을 붙여야 한다.

**② 옛 프로세스가 8080 을 잡고 있으면 새 앱이 죽었는데도 200 이 돌아온다.**

## Task 순서

| # | Task | 내용 | 규모 |
|---|---|---|---|
| 0 | 기반 정리 | `git tag pre-greenfield` · 옛 백엔드 삭제 · `_repo.common` → `common` 이동 · 새 `schema.sql` · 시드 스크립트 · `foreign_keys=on` | 중간 |
| 1 | `place` | **템플릿.** 목록 · 상세 · 필터(지역·태그) · `place_tags` 조인 | 중간 |
| 2 | `user` | 가입 · 로그인 · 로그아웃 · 내 정보 · `SecurityConfig` | 중간 |
| 3 | `favorite` `review` `history` | place 를 참조하는 작은 도메인 3개. 패턴 반복 | 중간 |
| 4 | `course` | `course_stops` 조인 · 소유권 판정(`isOwner`) | 중간 |
| 5 | `post` | 글 + 댓글 · `@ManyToOne User` | 중간 |
| 6 | `search` `weather` `exchange` | 작은 것 3개. 외부 API 클라이언트 2종 | 작음 |
| 7 | 프론트 맞추기 | `api/client.js` 부터. 화면은 필요한 것만 | 큼 |

순서의 근거:

- **`place` 가 1번**인 이유는 가장 단순하면서 필요한 요소를 다 갖췄기 때문이다 —
  목록 · 상세 · 필터 · 조인. 여기서 Service 메서드를 어떻게 자르는지, Dtos 를 어떻게
  쓰는지, 날짜를 어떻게 매핑하는지를 확정한다. **1번에 시간을 가장 많이 쓰는 것이 맞다.**
- **`user` 가 2번**인 이유는 3번부터 전부 로그인 사용자가 필요하기 때문이다.
- **프론트가 마지막**인 이유는 백엔드 계약이 굳은 뒤에 한 번만 맞추기 위해서다.
  중간에 맞추면 계약이 흔들릴 때마다 두 번 고친다.

## 각 Task 의 진행 절차

도메인 단위로 잡고, 그 안에서 **계층 단위**로 끊는다: `entity → repository → service →
dtos → controller`.

1. **Claude 가 가이드한다** — 이 계층이 무슨 일을 하는가 / 왜 이렇게 하는가 /
   무엇을 쓸 것인가 / 놓치기 쉬운 것.
2. **사용자가 코드를 쓴다.**
3. **Claude 가 확인한다** — 컴파일 + 규칙 3개 기준 리뷰.
4. 다음 계층으로.
5. 도메인이 끝나면 스모크 + 커밋.

## 열린 결정 (진행하면서 정한다)

- **API 경로.** `/api/places?kind=destination` 으로 합칠지, `/api/destinations` ·
  `/api/festivals` 를 그대로 둘지. Task 1 에서 정한다.
- **날짜 자바 타입.** `Instant` vs `String`. Task 1 에서 실제 저장 결과를 보고 확정.
- **`prefecture` 참조 테이블.** 47개 고정값이지만 지금은 TEXT + `CHECK` 없이 둔다.
  필요해지면 `CHECK` 제약부터 추가한다.

## 범위 밖

- 보안 강화 · 로깅 · 모니터링 · 성능 최적화 · 재시도 전략 (프로젝트 `CLAUDE.md` 방침)
- 테스트 프레임워크 도입 (스모크 스크립트로 대신한다)
- 새 기능 추가
