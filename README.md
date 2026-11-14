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

## 빠른 실행 — Docker Compose (D-085)

Docker Desktop 만 있으면 된다. Java · Node · MySQL 은 설치하지 않아도 된다. 저장소 맨 위에서:

```bash
docker compose up -d --build
```

처음에는 이미지 빌드와 시드 때문에 몇 분 걸린다. 끝나면 http://localhost 에 접속한다.
MySQL · 백엔드 · 화면이 뜨고, 시연 데이터(아래 "시연 데이터")가 처음 한 번 자동으로 들어간다. `demo` / `demo1234` 로 로그인한다.

| 명령 | 하는 일 |
|---|---|
| `docker compose ps` | 상태 보기. `seed` 는 시연 데이터를 넣고 끝나는 것이 정상이다 |
| `docker compose logs -f backend` | 백엔드 로그 |
| `docker compose exec -T db sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 japan_travel' < scripts/demo_seed.sql` | 시연 직전 상태로 되돌리기 |
| `docker compose down` | 끄기. 데이터는 볼륨에 남는다 |
| `docker compose down -v` | 끄고 데이터까지 지우기. 다음 `up` 때 처음부터 다시 넣는다 |

80 포트를 이미 쓰고 있으면 `WEB_PORT=8088 docker compose up -d --build` 처럼 바꾼다. DB 비밀번호 기본값은 `japantravel` 이고 `DB_PASSWORD` 로 바꾼다.
사진(Wikimedia Commons)과 지도 타일은 인터넷에서 불러온다.

## 서버에 올리기 — AWS EC2 (D-086 · D-087)

리눅스 서버 한 대(RAM 2GB 이상, Ubuntu 24.04)에 위의 compose 를 그대로 띄운다. 보안 그룹은 22(관리자 IP 만) · 80(전체)만 연다.

```bash
# 서버에서 한 번 — Docker 설치는 docs.docker.com 의 Ubuntu 안내를 따른다
git clone -b greenfield https://github.com/young-sang/Japan_Travel.v2.git japan-travel
cd japan-travel

# 공개 서버용 비밀 값 (.env.example 참고). admin 비밀번호 원문은 서버의 파일에만 둔다
openssl rand -base64 12 > ~/admin-password.txt && chmod 600 ~/admin-password.txt
HASH=$(docker run --rm httpd:2.4-alpine htpasswd -nbBC 10 "" "$(cat ~/admin-password.txt)" | tr -d ':\n')
printf "DB_PASSWORD=%s\nJWT_SECRET=%s\nADMIN_PASSWORD_HASH='%s'\nWEB_PORT=80\n" "$(openssl rand -hex 16)" "$(openssl rand -hex 32)" "$HASH" > .env && chmod 600 .env

docker compose up -d --build
```

| 할 일 | 명령 |
|---|---|
| 새 코드로 바꾸기 | `git pull && docker compose up -d --build` |
| 시연 직전 되돌리기 | `docker compose run --rm -e FORCE_SEED=1 seed` (admin 비밀번호는 `.env` 값 그대로) |
| 상태 · 로그 | `docker compose ps -a` · `docker compose logs -f backend` |

서버에서는 `admin` 이 README 의 `demo1234` 로 로그인되지 않는다. `demo` 와 리뷰어 계정은 그대로 `demo1234` 다.
`db` · `backend` · `web` 은 `restart: unless-stopped` 라 서버를 껐다 켜도 다시 뜬다.
비용: 시연이 없으면 EC2 콘솔에서 인스턴스를 **중지**한다. 다 쓰면 인스턴스 **종료** 후 **탄력적 IP 릴리스**까지 해야 요금이 멈춘다.

아래는 Docker 없이 개발 모드로 띄우는 방법이다.

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
| `DB_URL` | JDBC 주소 | `localhost:3306/japan_travel` |
| `DB_USERNAME` | MySQL 계정 | `root` |
| `DB_PASSWORD` | MySQL `root` 비밀번호 | 빈 값 |
| `JWT_SECRET` | JWT 서명 키 (32바이트 이상) | 개발용 키 |

```bash
cd backend
./gradlew bootRun
```

> Windows 에서는 `.\gradlew.bat bootRun`.

기동할 때마다 `schema.sql` 로 테이블을 만들고 `destinations.sql` · `festivals.sql` · `data.sql` · `images.sql` 로 시드를 넣는다 (`INSERT IGNORE` 라 중복되지 않는다, D-050).
시드는 도도부현 47 · 여행지 940 · 축제 403 · 기본 제공 코스 26(정류장 110)이다. 기동 시드에 계정은 없다.

**여행지 · 축제 데이터 출처 (D-089 · D-090 · D-092 · D-093)** — 설명은 ja 위키백과 원문을 AI 가 원문 안의 내용만으로 한국어로 요약한 것이다(CC BY-SA 4.0, 상세 화면에 원문 링크).
- 여행지: 목록 · 좌표 · 사진은 [Wikidata](https://www.wikidata.org)(CC0) · Wikimedia Commons. `scripts/wiki_destinations.py` 가 만든다.
- 축제: 목록은 ja 위키백과 현별 축제 분류, 개최 시기는 원문에서 뽑았다(못 찾으면 뺐다). `scripts/wiki_festivals.py` 가 만든다.
- 사용법은 각 파일 머리말에 있다.

이미 있는 DB 에는 `INSERT IGNORE` 라 새 데이터가 반영되지 않는다. 바꾸려면 DB 를 백업한 뒤 `DELETE FROM destinations; DELETE FROM festivals; DELETE FROM courses WHERE owner_user_id IS NULL;` → 백엔드 재기동 → 시연 데이터를 다시 넣는다.

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
| 코스에 담기 — 상세의 "코스에 담기" 로 새 코스를 열거나 내 코스 끝에 더하기, 즐겨찾기에서 골라서 새 코스 (D-094) | 필요 |
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

## 추천용 임베딩 만들기 (D-095 · D-096)

여행지 · 축제의 "이름 + 지역 + 설명" 을 로컬 [Ollama](https://ollama.com) 의 `bge-m3` 로 벡터화한다. 결과는 두 곳에 나온다.

- `backend/src/main/resources/embeddings.sql` — 사이트 시드. 기동할 때 `destination_embeddings` · `festival_embeddings` 에 들어간다. **다시 만들었으면 이 파일도 커밋한다** — 공개 서버에는 Ollama 가 없어 `git pull` 로 받은 이 파일이 유일한 원천이다.
- `scripts/embedding/places.json` — 확인용(`similar_places.py`). git 에 넣지 않는다.

장소 시드(`destinations.sql` · `festivals.sql`)가 바뀌면 다시 실행한다 — 항상 전체를 다시 계산한다(GPU 로 약 3분). 임베딩이 없는 장소는 추천에서 빠질 뿐 기동에는 영향이 없다.

```bash
ollama pull bge-m3              # 처음 한 번 (약 1.2GB)
python scripts/embed_places.py  # 끝나면 여행지 · 축제 수를 출력한다
python scripts/similar_places.py "오타루 운하"  # 비슷한 여행지 4 · 축제 1 (임베딩 확인용)
```

### 옛 장소 · 옛 설명이 남은 DB 초기화 (D-097)

장소 시드는 `INSERT IGNORE` 라, 데이터 교체(D-089 · D-092) 전에 만든 DB 에는 옛 장소와 옛 설명이 남는다. 임베딩은 새 설명으로 계산했으므로 이런 DB 는 초기화한다.
**계정이 시연 계정뿐일 때만** 쓴다 — 시연 활동은 `seed` 가 다시 넣지만 그 밖의 데이터는 백업에만 남는다. Docker(로컬 · 공개 서버) 기준.

```bash
# 1. 백업 (data/*.bak_* 는 git 무시)
docker compose exec -T db sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 --single-transaction japan_travel' > data/japan_travel.docker.bak_$(date +%Y%m%d_%H%M%S).sql
# 2. 볼륨까지 지우고 다시 띄운다 — 시드 · 임베딩 · 시연 데이터가 자동으로 들어간다
docker compose down -v
docker compose up -d --build
# 3. 확인: 940 · 403 · 940 · 403
docker compose exec -T db sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N japan_travel -e "SELECT COUNT(*) FROM destinations; SELECT COUNT(*) FROM festivals; SELECT COUNT(*) FROM destination_embeddings; SELECT COUNT(*) FROM festival_embeddings"'
```

되돌리기: `docker compose exec -T db sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 japan_travel' < data/<백업 파일>` (빈 DB 에 넣는다).

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
