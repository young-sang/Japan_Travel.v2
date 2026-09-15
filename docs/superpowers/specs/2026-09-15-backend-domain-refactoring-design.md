# 백엔드 도메인 리팩토링 설계

작성일: 2026-09-15
대상: `backend/src/main/java/com/japantravel/**`

## 목적

계층(layer) 기준으로 묶여 있는 백엔드 패키지를 **도메인 기준**으로 재편한다.

1순위 목표는 **코드 이해**다. 구조 개선은 그 수단이다. 따라서 속도보다 각 단계에서
"이 도메인이 무슨 일을 하는가"를 짚고 넘어가는 것을 우선한다.

**코드 수정은 사용자가 직접 한다.** Claude는 설명·작업 지시·검증·리뷰를 맡는다.

## 현재 상태

패키지가 기술 계층으로 갈려 있다.

```
controller/ (9) · repository/ (9) · service/ (1) · dto/ (1) · client/ (5)
collector/ (4) · config/ (5) · security/ (3) · tools/ (1)
```

문제 세 가지:

- **service 계층이 사실상 없다.** `AuditService` 하나뿐이고 컨트롤러가 리포지토리를
  직접 호출한다. 비즈니스 규칙(`CourseController.isOwnerOrAdmin`)이 컨트롤러에 숨어 있다.
- **`Dtos.java` 한 파일에 record 24개**가 전 도메인 것이 섞여 있다.
- **`AdminController` 459줄**이 리포지토리 11개를 주입받아 posts·stats·collector·
  destinations·festivals·users·audit·cache를 전부 처리한다. 도메인이 아니라
  모든 도메인 위에 얹힌 창구다. `POST /api/admin/destinations`는 사실 destination
  도메인의 쓰기 동작이다.

테스트는 **하나도 없다** (`backend/src/test` 없음).

## 결정 사항

| 항목 | 결정 |
|---|---|
| 범위 | 백엔드만. `frontend/` 무수정 |
| 기능 동작 | 100% 보존. API 경로·메서드·JSON 불변 |
| service 계층 | **전 도메인 3계층 통일** (Controller / Service / Repository) |
| AdminController | **도메인별로 해체.** `admin/` 패키지를 남기지 않는다 |
| 즐겨찾기·리뷰·히스토리 | `favorite/` `review/` `history/` **세 도메인으로 분리** |
| 작업 주체 | 사용자가 직접 코드 수정. Claude는 설명·지시·검증 |

전 도메인 3계층을 택한 이유: `DestinationService.list()`가 `repo.findAll()`만
호출하는 껍데기가 생기는 비용을 감수하더라도, 일관된 계층 구조가 발표에서
설명하기 쉽고 "어느 도메인이든 같은 자리에 같은 것이 있다"는 예측 가능성이
이해에 도움이 된다.

## 목표 구조

```
com.japantravel/
  JapanTravelApplication.java

  destination/   Controller · AdminController · Service · Repository · Dtos
  festival/      Controller · AdminController · Service · Repository · Dtos
  course/        Controller · Service · Repository · Dtos
  post/          Controller · AdminController · Service · Repository · Dtos
  account/       AuthController · AdminController · Service · UserRepository · Dtos
                 + AppUserPrincipal · CurrentUser · UserDetailsServiceImpl
  favorite/      Controller · Service · Repository · Dtos
  review/        Controller · Service · Repository · Dtos
  history/       Controller · Service · Repository · Dtos
  collector/     AdminController · Service · CollectorRunRepository · BulkRunRepository · Dtos
                 + WikipediaCollector · WikipediaClient · NominatimClient
                 + PrefectureCatalog · TagInferrer · ZombieWatcher · BackfillCoordsRunner
  search/        Controller · Service
  weather/       Controller · Service · OpenMeteoClient
  exchange/      Controller · Service · FrankfurterClient
  audit/         AdminController · Service · Repository
  system/        AdminController · Service

  common/        FetchResult · config/{AsyncConfig, CacheConfig, SecurityConfig,
                                       WebConfig, StartupRunner}
```

### 특이 사항

- **`ProxyController` 분해**: weather와 exchange는 서로 무관한데 한 파일에 있었다.
  두 도메인으로 가른다. URL은 `/api/weather`·`/api/fx` 그대로다.
- **`search/`는 교차 도메인**: destination·festival·course Service 셋에 의존한다.
  현재는 리포지토리 3개를 직접 읽어 메모리에서 필터링한다. 이 동작은 유지하되
  의존 대상만 Service로 바꾼다.
- **`system/`**: `AdminController`에 남는 stats·cache 엔드포인트. 도메인이 아니라
  운영 기능이지만 갈 곳이 필요하다.
- **`collector/`는 자체 도메인**: 관리자 화면에서만 쓰이지만 수집 파이프라인이라는
  고유한 책임이 있다. Wikipedia/Nominatim 클라이언트도 여기 속한다.

## 진행 순서

의존 방향을 따라 아래에서 위로. `CurrentUser`(account)와 `AuditService`(audit)를
거의 모든 도메인이 쓰므로 이 둘을 앞쪽에 배치해 import를 두 번 고치는 일을 막는다.

| # | 단계 | 내용 | 규모 |
|---|---|---|---|
| 0 | `common/` | config 5종 + FetchResult 이동. 로직 없음 | 작음 |
| 1 | `destination/` | **템플릿 단계.** Dtos 분리 + admin CRUD 흡수 + Service 신설 | 중간 |
| 2 | `festival/` | destination의 거울. 1단계 패턴 복사 | 작음 |
| 3 | `account/` | AuthController + security 3종 + 관리자 사용자 관리 | 중간 |
| 4 | `audit/` | AuditService · AuditLogRepository + 감사 로그 조회 | 작음 |
| 5 | `course/` | 소유권 판정 로직이 Service로 올라감 | 중간 |
| 6 | `favorite/` `review/` `history/` | UserDataRepository 145줄을 셋으로 분해 | 중간 |
| 7 | `post/` | 게시글·댓글 + 관리자 삭제 | 중간 |
| 8 | `search/` `weather/` `exchange/` | 작은 것 3개 한 번에 | 작음 |
| 9 | `collector/` `system/` | AdminController 잔여분 전부 | 큼 |

1단계에 공을 들이는 이유: 여기서 정한 모양(파일 이름 규칙, Service와 Repository의
책임 경계, 관리자 API 흡수 방식)이 나머지 9단계의 틀이 된다.

## 단계별 절차

각 단계에서 이 순서를 반복한다.

1. **Claude가 설명한다** — 해당 도메인 코드를 읽고 "무슨 일을 하는가 / 어떤 규칙이
   있는가 / 누가 의존하는가"를 정리해 제시한다.
2. **Claude가 작업을 지시한다** — 어떤 파일이 어디로 가고, 새 Service에 무엇이
   들어가고, 어떤 import가 바뀌는지. 사용자 확인을 받는다.
3. **사용자가 코드를 수정한다** — `git mv`로 파일 이동(히스토리 보존), Service 추출,
   import 정리.
4. **Claude가 검증한다** — `mvn -q compile` → 앱 기동 → 해당 엔드포인트 curl 스모크 →
   diff 리뷰.
5. **커밋한다.** 단계별 1커밋. 문제 시 그 단계만 되돌린다.

### 지시의 상세도

1단계(destination)는 목표 코드를 구체적으로 제시한다. 뒤로 갈수록 방향만 주고
사용자가 패턴을 적용하게 한다. 너무 빡빡하거나 헐거우면 사용자가 조정을 요청한다.

## 검증 방법

테스트가 없으므로 안전망은 **API 계약 불변** 하나뿐이다.

- `/api/...` 경로·HTTP 메서드·JSON 필드명을 한 글자도 바꾸지 않는다.
- Jackson이 record 필드명으로 직렬화하므로 **DTO record의 필드명·순서·타입도
  그대로 둔다.** 패키지와 파일만 움직인다.
- `frontend/src/api/client.js`의 diff가 비어 있으면 계약이 안 깨졌다는 증거다.
- `schema.sql` 변경 없음. 마이그레이션 없음. DB 삭제 없음.
- 단계마다 `mvn -q compile` 통과 + 앱 기동 + 해당 도메인 엔드포인트 curl 확인.

## 범위 밖

- 프론트엔드 리팩토링
- 테스트 작성
- 죽은 테이블 3개(`achievements` · `collections` · `collection_items`) 정리 —
  Java 코드 어디에서도 참조하지 않는 스키마 잔재다. 문서에 기록만 하고 두 번째
  작업으로 남긴다.
- 보안·로깅·검증·재시도·성능 추가 (프로젝트 CLAUDE.md 방침)
- `SearchController`의 전체 조회 후 메모리 필터링 — 비효율이지만 동작 보존이 우선
