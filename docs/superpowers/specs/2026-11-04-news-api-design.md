# news API 설계

- 날짜: 2026-11-04
- 결정: [D-075](../../DECISIONS.md#d-075--관리자-기능을-뉴스-작성-하나로-되살리고-뉴스는-직접-쓴다) (범위) ·
  [D-079](../../DECISIONS.md#d-079--뉴스-계약-news-테이블--공개-조회--admin-쓰기--별도-관리자-페이지) (계약 · 화면)
- 진행: 보완 계획 S3c. AGILE 계획 승인 + 결정 게이트를 `spring-domain-skeleton` 0 ~ 2단계로 갈음했다(사용자 승인).
  3 ~ 6단계는 조각별 TDD 안에서 이어서 구현 모드(D-049)로 한다.

## 옛 동작

옛 뉴스 화면(D-073 으로 삭제)은 언론사 기사 5건을 하드코딩한 링크였다. API · 테이블은 없었다.
옛 관리자 화면 파일(`pages/Admin*.jsx`, D-054)은 쓰지 않는다.

## 테이블

```sql
CREATE TABLE IF NOT EXISTS news (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  title          VARCHAR(200) NOT NULL,
  body           TEXT         NOT NULL,
  image_path     VARCHAR(500),
  image_credit   VARCHAR(300),
  author_user_id BIGINT,                       -- 작성한 관리자. 계정을 지우면 NULL 로 남긴다
  created_at     DATETIME(6)  NOT NULL,        -- 목록 정렬 기준 (D-065 와 같은 이유로 마이크로초)
  updated_at     DATETIME,
  FOREIGN KEY (author_user_id) REFERENCES users(id) ON DELETE SET NULL
);
```

## 계약

| 메서드 | 경로 | 권한 | 상태 | 응답 `data` |
|---|---|---|---|---|
| GET | `/api/news` | 🌐 | 200 | `NewsSummaryResponse[]` — `created_at` 내림차순 |
| GET | `/api/news/{id}` | 🌐 | 200 | `NewsResponse` |
| POST | `/api/news` | 🔑 ADMIN | 201 | `NewsResponse` |
| PUT | `/api/news/{id}` | 🔑 ADMIN | 200 | `NewsResponse` (네 필드 모두 덮어씀) |
| DELETE | `/api/news/{id}` | 🔑 ADMIN | 200 | `null` |

- `NewsRequest` = `{title, body, imagePath?, imageCredit?}` — 빈 문자열인 `imagePath` · `imageCredit` 은 null 로 저장
- `NewsSummaryResponse` = `{id, title, excerpt, imagePath, createdAt}` — `excerpt` 는 본문 앞 100자(줄바꿈은 공백), 넘으면 `…`
- `NewsResponse` = `{id, title, body, imagePath, imageCredit, authorNickname, createdAt, updatedAt}`

## 판정

- 권한은 `SecurityConfig` 가 한다. 쓰기 3개는 `hasRole("ADMIN")`. 토큰 없음 → 401 `UNAUTHORIZED`,
  토큰은 있는데 ADMIN 아님 → 403 `FORBIDDEN` (새 `AccessDeniedHandler` 가 공용 봉투로 쓴다).
- POST: 검증 → 작성자 조회(`UNAUTHORIZED`) → 저장.
- PUT · DELETE: 뉴스 조회(`NEWS_NOT_FOUND` 404) → (PUT 만) 검증 → 변경.
- 검증(`INVALID_NEWS` 400): `title` · `body` 가 null · 공백뿐, 또는 `title` 이 200자 초과.

## 새 에러 코드

| 코드 | 상태 | 언제 |
|---|---|---|
| `NEWS_NOT_FOUND` | 404 | 없는 뉴스 id |
| `INVALID_NEWS` | 400 | 위 검증 실패 |

## 화면

| 경로 | 권한 | 내용 |
|---|---|---|
| `/news` | 공개 | 졸업작품 레이아웃 — 최신 1건 큰 카드, 나머지 작은 카드 그리드 |
| `/news/:id` | 공개 | 사진 · 출처 · 제목 · 작성일 · 본문(줄바꿈 유지) |
| `/admin/news` | ADMIN | 표 목록 + 쓰기 · 수정 · 삭제. 헤더 사용자 메뉴의 "관리자" 로 들어간다 |
| `/admin/news/new` · `/admin/news/:id/edit` | ADMIN | 작성 · 수정 폼 |

## 시드

`scripts/demo_seed.sql` 에 `admin` / `demo1234` (ADMIN) 와 직접 쓴 뉴스 4건. 사진은 DB 여행지의 Commons 사진 · 출처를 재사용한다.

## 검증 절차

- `NewsApiTest` — 조회(최신순 · 미리보기 · 404) / 쓰기(401 · 403 · 201 · 400) / 수정 · 삭제(403 · 404 · 200)
- 브라우저 — 비로그인으로 `/news` · 상세, `admin` 로그인으로 작성 → 목록 맨 위, 수정 · 삭제
