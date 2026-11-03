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
시드는 도도부현 47 · 여행지 219 · 축제 84 · 기본 제공 코스 26(정류장 110)이다. **계정은 시드하지 않는다** — 화면에서 가입한다.

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
`Top100` · `Hotplace` · `News` · `Tip` 페이지는 API 없이 화면에 내용이 고정되어 있다.

## 문서

| 문서 | 내용 |
|---|---|
| [docs/API.md](docs/API.md) | 엔드포인트 · 응답 봉투 · 인증 |
| [docs/SCHEMA.md](docs/SCHEMA.md) | 테이블 |
| [docs/ERRORS.md](docs/ERRORS.md) | `ErrorCode` 별 의미 |
| [docs/DECISIONS.md](docs/DECISIONS.md) | 의사결정 기록 (D-001 ~) |

## 검증

자동 테스트는 아직 없다. 백엔드를 띄운 상태에서 스모크 스크립트를 돌린다.

```bash
bash scripts/smoke/account.sh
```

`scripts/smoke/` 에 `account` · `error` · `history_search` · `rating` 이 있다.

## 디자인 보존

graduate project 의 CSS · 이미지 · 마크업 구조를 그대로 가져왔고 새 디자인 시스템(Tailwind, CSS-in-JS 등)은 넣지 않았다.
새 화면도 기존 클래스명 · 시각 톤을 재사용한다.
