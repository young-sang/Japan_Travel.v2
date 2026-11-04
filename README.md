# Japan Travel v2 — 사내 프로토타입

graduate project의 디자인을 가져와 React + Spring Boot + MySQL 로 다시 지은 일본 여행 정보 서비스.
백엔드와 DB 스키마는 `greenfield` 브랜치에서 처음부터 새로 지었다 ([재작성 설계](docs/superpowers/specs/2026-09-17-greenfield-rebuild-design.md)).

> 사내 프로토타입 — 외부 노출 없음. rate limiting · 정밀 입력 검증은 의도적으로 넣지 않았다.

## 디렉토리

```
Japan_Travel.v2/
├── backend/     Spring Boot 3.3 (Java 17, Gradle, Spring Data JPA, MySQL 8, JWT)
├── frontend/    Vite + React 18 + react-router + react-leaflet
├── scripts/     smoke/*.sh — API 스모크 스크립트
└── docs/        API · 스키마 · 에러 코드 · 의사결정 기록 · 설계 스펙
```

백엔드 패키지는 도메인 단위다 — `com.japantravel.<domain>.{controller,service,repository,entity,dto}`.
도메인: `destination` `festival` `prefecture` `user` `favorite` `review` `history` `course` `search`, 공통은 `common`.

## 사전 요구사항

- **Java 17+** — Gradle 은 래퍼(`backend/gradlew`)가 있어 설치하지 않아도 된다
- **Node 18+**
- **MySQL 8** — `localhost:3306` 에 `japan_travel` 데이터베이스가 있어야 한다

```sql
CREATE DATABASE japan_travel DEFAULT CHARACTER SET utf8mb4;
```

## 실행

### 1) 백엔드 (port 8080)

| 환경변수 | 용도 | 기본값 |
|---|---|---|
| `DB_PASSWORD` | MySQL `root` 비밀번호 | 빈 값 |
| `JWT_SECRET` | JWT 서명 키 (32바이트 이상) | 개발용 키 |

```bash
cd backend
./gradlew bootRun
```

> Windows 에서는 `.\gradlew.bat bootRun`.

기동할 때마다 `schema.sql` 로 테이블을 만들고 `data.sql` 로 시드를 넣는다 (`INSERT IGNORE` 라 중복되지 않는다, D-050).
시드는 도도부현 47 · 여행지 219 · 축제 84 · 기본 제공 코스 26(정류장 110)이다. 기동 시드에 계정은 없다.

### 시연 데이터 (선택)

백엔드를 한 번 띄워 시드가 들어간 뒤에 돌린다. 몇 번을 돌려도 같은 상태로 돌아간다 (D-070).

```bash
mysql -uroot -p --default-character-set=utf8mb4 japan_travel < scripts/demo_seed.sql
```

시연 계정 `demo` / `demo1234` (닉네임 "하루") — 즐겨찾기 10 · 내 코스 2 · 최근 본 장소 10 · 리뷰 2.
리뷰어 계정 4개(`kyoto_lover` · `foodie_park` · `photo_bear` · `weekend_trip`, 비밀번호 같음)가 주요 장소에 리뷰 33건을 더 남기고,
최근 본 장소 · 즐겨찾기로 TOP 25 가 채워진다(점수 있는 여행지 32곳).
관리자 계정 `admin` / `demo1234` (닉네임 "편집부") — 헤더 사용자 메뉴의 "관리자" 로 뉴스를 쓴다. 시드에 뉴스 4건이 들어 있다.
다시 돌리면 계정 id 가 바뀌므로 브라우저에서 다시 로그인한다.

### 2) 프론트엔드 (port 5173)

```bash
cd frontend
npm install
npm run dev
```

http://localhost:5173 에 접속한다. `/api` 요청은 Vite 프록시가 `:8080` 으로 넘긴다.

## 기능

| 기능 | 로그인 |
|---|---|
| 여행지 · 축제 · 코스 조회 (도도부현 · 월 필터), 상세 페이지 `/detail/:type/:id` | 불필요 |
| 통합 검색 | 불필요 |
| 회원가입 · 로그인 (JWT, 로그아웃은 클라이언트에서 토큰 삭제) | — |
| 즐겨찾기 · 리뷰(평균 별점 · 리뷰 수 집계) · 최근 본 장소 | 필요 |
| 내 코스 만들기 · 편집 (여행지 · 축제 정류장) | 필요 |
| 마이페이지 — 즐겨찾기 · 리뷰 · 코스 · 최근 본 장소 · 계정(프로필 · 비밀번호 · 탈퇴) | 필요 |

**하지 않는 것** — 게시판(D-061), 관리자 API(D-009 · D-030), 날씨 · 환율 위젯(D-032).
`Tip` 페이지는 API 없이 화면에 내용이 고정되어 있다. `/top25` 는 여행지 종합 점수 순위 API 를 쓴다 (D-076). 핫플레이스는 지웠다 (D-073). 뉴스는 관리자가 `/admin/news` 에서 직접 쓴다 (D-075 · D-079).

## 문서

| 문서 | 내용 |
|---|---|
| [docs/API.md](docs/API.md) | 엔드포인트 · 응답 봉투 · 인증 |
| [docs/SCHEMA.md](docs/SCHEMA.md) | 테이블 |
| [docs/ERRORS.md](docs/ERRORS.md) | `ErrorCode` 별 의미 |
| [docs/DECISIONS.md](docs/DECISIONS.md) | 의사결정 기록 (D-001 ~) |

## 검증

백엔드 자동 테스트 (D-062) — 로컬 MySQL 이 켜져 있고 `DB_PASSWORD` 가 설정돼 있어야 한다.
같은 서버의 `japan_travel_test` DB 를 쓰고(없으면 만든다), 운영 `japan_travel` 은 건드리지 않는다.

```bash
cd backend && ./gradlew test
```

| 층 | 위치 | 내용 |
|---|---|---|
| API 통합 | `src/test/java/.../*ApiTest.java` | MockMvc 로 엔드포인트 전부의 성공 경로와 401 · 403 · 404 · 400 규칙 |
| 서비스 단위 | `src/test/java/.../*ServiceTest.java` | Mockito 로 판정 순서 · 멱등 · seq 매기기 · 정렬 규칙 |

앱을 띄운 상태에서 보는 curl 스모크 스크립트도 남아 있다 — `scripts/smoke/` 의 `account` · `error` · `history_search` · `rating`.

## 디자인 보존

graduate project 의 CSS · 이미지 · 마크업 구조를 그대로 가져왔고 새 디자인 시스템(Tailwind, CSS-in-JS 등)은 넣지 않았다.
새 화면도 기존 클래스명 · 시각 톤을 재사용한다.
