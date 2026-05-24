# Japan Travel v2 — 사내 프로토타입

graduate project(`D:/01_Program/00_Project/새 폴더/graduate project/`)의 디자인을 가져와 React + Spring Boot + SQLite로 재구축한 완성형 시스템.

> 사내 프로토타입 — 외부 노출 없음. rate limiting·정밀 입력 검증은 의도적으로 제외(데모 수준 로그인은 포함).

## 디렉토리

```
Japan_Travel_v2/
├── backend/     Spring Boot 3.3 (Java 17, SQLite, Caffeine)
├── frontend/    Vite + React 18 + react-router + react-leaflet
├── data/        SQLite 파일 위치 (런타임 생성)
└── docs/        기획서·아키텍처 문서
```

## 사전 요구사항

- **Java 17+** (`java -version`)
- **Maven 3.9+** (`mvn -v`)
- **Node 18+** (`node -v`)

## 실행

### 1) 백엔드 (port 8080)

```bash
cd backend
mvn spring-boot:run
```

첫 부팅 시 `data/japan_travel.db` 가 자동 생성되고, schema.sql 이 적재됩니다. DB는 **비어 있는 상태로 시작**하며, `admin` 계정(비밀번호 `admin1234`, role=ADMIN)이 자동 시드됩니다.

> **마이그레이션 주의**: 기존 v2 DB에서 업그레이드하는 경우 `users` 테이블 스키마가 바뀌었습니다. `data/japan_travel.db`를 삭제하고 재기동하세요.

### 2) 프론트엔드 (port 5173)

```bash
cd frontend
npm install
npm run dev
```

브라우저에서 http://localhost:5173 접속.

### 3) 첫 데이터 수집

데이터가 비어 있으므로 어느 페이지든 들어가면 "지금 수집하기" 버튼이 나옵니다. 또는 직접 호출:

```bash
curl -X POST http://localhost:8080/api/admin/collector/run \
  -H "Content-Type: application/json; charset=utf-8" \
  --data-binary '{"type":"destination","prefecture":"도쿄도"}'
```

수집 진행 상황은 `/admin` 대시보드에서 실시간으로 확인할 수 있습니다.

## 주요 기능

- **회원가입·로그인·로그아웃**: 서버 세션(HttpOnly 쿠키) + BCrypt 해시. `USER`/`ADMIN` 역할.
- **여행지/축제/코스 조회**: 47 도도부현 + 태그 필터 (조회는 비로그인 허용)
- **즐겨찾기·리뷰·히스토리**: 로그인 사용자별 분리
- **내 코스 만들기**: 장소 검색·추가·정렬 + 임시저장/발행
- **통합 상세 페이지**: `/detail/:type/:id` 단일 컴포넌트
- **검색·지도 오버레이**: 전역 검색, Leaflet+OSM 지도
- **위젯**: 도쿄 날씨(Open-Meteo), 100엔 환율(Frankfurter)
- **다크모드 / 데이터 내보내기 / 발자국·배지**
- **관리자**: 대시보드, 콘텐츠 관리, 사용자 데이터 관리, 캐시·시스템 관리 (ADMIN 전용 `/api/admin/*`, `/admin`)

## 외부 API

모두 무료·키 불필요:

| 용도 | API |
|---|---|
| 관광지·축제 콘텐츠 | Wikipedia REST + MediaWiki Action API (한국어) |
| 좌표 보강 | OpenStreetMap Nominatim |
| 지도 타일 | OpenStreetMap |
| 날씨 | Open-Meteo |
| 환율 | Frankfurter |

## 아키텍처

- **Wikipedia Collector**: 첫 부팅 시 빈 DB → `POST /api/admin/collector/run`으로 도도부현·카테고리별 수집 → UPSERT(destinations/festivals).
- **Caffeine 캐시**: 외부 API 응답 7일 TTL, in-memory.
- **CORS**: `:5173` → `:8080` 허용.

## 검증

기본 흐름:

1. 백엔드 부팅 → `curl http://localhost:8080/api/destinations` → `[]`
2. Collector 실행 → 진행률 폴링 → `status: success|partial`
3. `curl http://localhost:8080/api/destinations?prefecture=도쿄도` → row 반환
4. 프론트에서 `/destination` → 카드 그리드 표시
5. `/login`에서 `admin` / `admin1234` 로 로그인 → ♥/리뷰 가능
6. 상세 페이지 진입 → `/mypage/history` 에 자동 기록
7. 리뷰 작성 → `/mypage/reviews` 통계 반영
8. `/admin` → KPI · Collector 이력 · 캐시 통계 확인 (ADMIN 전용)

### 인증 확인 (curl)

```bash
# 로그인 (쿠키 저장)
curl -i -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin1234"}'

# 세션 확인
curl -b cookies.txt http://localhost:8080/api/auth/me

# 가입
curl -b cookies.txt -c cookies-alice.txt -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"alice1234","nickname":"Alice"}'

# 로그아웃
curl -b cookies.txt -X POST http://localhost:8080/api/auth/logout
```

## 디자인 보존

`graduate project/` 의 CSS·이미지·마크업 구조를 그대로 복사해서 사용했으며, 새 디자인 시스템(Tailwind, CSS-in-JS 등)은 도입하지 않았습니다. 신규 기능(관리자/마이페이지 세부 탭/배지 등)은 기존 클래스명·시각 톤을 재사용합니다.
