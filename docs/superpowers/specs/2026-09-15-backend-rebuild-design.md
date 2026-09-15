# 백엔드 재작성 설계

작성일: 2026-09-15
대상: `backend/`

> 이 문서는 같은 날짜의 `2026-09-15-backend-domain-refactoring-design.md` 와
> `plans/2026-09-15-backend-domain-refactoring.md` 를 **대체한다.** 그 둘은 기존 코드를
> 옮기는 리팩토링 계획이었다. 이 문서는 `_repo/` 아래에 **새로 짓는** 계획이다.

## 목적

백엔드를 도메인 기준으로 **처음부터 다시 설계해서 작성한다.** 옛 코드는 참고 자료이지
이식 대상이 아니다.

1순위 목표는 **코드 이해**다. 구조 개선은 그 수단이다.

**코드는 사용자가 직접 쓴다.** Claude 는 설명·명세·검증·리뷰를 맡고, 구현 코드는
사용자가 막혔을 때만 제공한다.

## 고정된 것과 자유로운 것

| 고정 (건드리지 않음) | 자유 (새로 설계) |
|---|---|
| `schema.sql` — DB 에 데이터가 이미 있다 (관광지 118건) | 패키지·클래스·파일 이름 |
| `/api/...` 경로 · HTTP 메서드 | 메서드 시그니처, 내부 구조 |
| 성공 응답 JSON 필드명 (프론트가 읽는다) | 에러 본문 모양 (상태코드 제외) |
| HTTP 상태코드 | Service 메서드 분할 방식 |
| 409 `BULK_ALREADY_RUNNING` 의 `code` 필드 | |

`frontend/` 는 한 줄도 고치지 않는다. `git diff --stat frontend/` 가 비어 있는 것이
계약 유지의 증거다.

기능도 추가하지 않는다. 현재 기능과 100% 동일하게 간다.

## 기술 스택

| 항목 | 값 | 비고 |
|---|---|---|
| 빌드 | **Gradle 8.14.5** (Groovy DSL) | Maven 에서 전환. Boot 3.3.4 는 Gradle 9 미지원 |
| 프레임워크 | Spring Boot 3.3.4 · Java 17 | |
| 영속성 | **Spring Data JPA** + Hibernate 6.5.3 | JdbcTemplate 에서 전환 |
| DB | SQLite | `hibernate-community-dialects` 의 `SQLiteDialect` |

`ddl-auto: none` · `open-in-view: false`. 스키마의 주인은 `schema.sql` 이고 Hibernate 는
스키마를 건드리지 않는다. `open-in-view: false` 는 엔티티가 컨트롤러까지 흘러나가면
예외로 알려주는 안전장치다.

SQLite 는 Hibernate 공식 dialect 가 없어 커뮤니티 구현을 쓴다. 드물게 예상 밖 동작이
나올 수 있으며, 만나면 **그 지점만** `@Query(nativeQuery = true)` 로 우회한다.

## 아키텍처

도메인마다 5계층, 계층마다 하위 패키지.

```
_repo/<domain>/
  controller/   <Domain>Controller · <Domain>AdminController
  service/      <Domain>Service
  repository/   <Domain>Repository        (JpaRepository)
  entity/       <Domain>                  (@Entity)
  dtos/         <Domain>Dtos              (record 컨테이너)
```

의존 방향은 **한 방향**이다. `controller → service → repository`. 역방향과 건너뛰기는 없다.

| 계층 | 하는 일 | 하지 않는 일 |
|---|---|---|
| `controller` | HTTP 만. 경로 매핑, 파라미터 바인딩, 결과 반환 | 조건 분기, 권한 판정, 쿼리 |
| `service` | 규칙 전부. 소유권 판정, 상태 전이, 예외 발생, 엔티티→DTO 변환 | HTTP 타입(`ResponseEntity`) 취급 |
| `repository` | 조회·저장만 | 규칙 판단 |
| `entity` | 테이블 매핑 | API 계약 노출 |
| `dtos` | API 계약 | 로직 |

### 규칙 4개

이 설계가 코드로 강제하려는 것은 이 넷이다.

1. **Repository 는 그 도메인의 Service 만 호출한다.** 다른 도메인이 필요하면 그쪽
   `Service` 를 부른다.
2. **엔티티는 도메인 밖으로 나가지 않는다.** Service 가 DTO 로 변환해서 넘긴다.
   엔티티를 그대로 반환하면 API 계약이 스키마에 묶이고, `open-in-view: false` 에서
   직렬화 중 예외가 난다.
3. **컨트롤러에 `if` 가 없다.** 조건은 전부 Service 에 있고, 실패는 예외로 표현된다.
4. **일반 API 와 관리자 API 는 같은 Service 를 공유한다.** 규칙이 두 군데로 갈라지지
   않는다.

### 명시적 예외 하나

`post` 도메인의 `Post` 엔티티는 `user` 도메인의 `User` 엔티티를 `@ManyToOne` 으로
참조한다. 글·댓글에 작성자 닉네임을 표시해야 하기 때문이다.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User author;
```

규칙 1은 이렇게 다듬어진다: **다른 도메인의 Repository·Service 는 부르지 않는다.
엔티티 참조는 읽기 전용 `@ManyToOne` 에 한해 허용한다.**

## 도메인 14개

| 도메인 | 엔드포인트 | 테이블 |
|---|---|---|
| `destination` | `/api/destinations/**` · `/api/admin/destinations/**` | `destinations` |
| `festival` | `/api/festivals/**` · `/api/admin/festivals/**` | `festivals` |
| `course` | `/api/courses/**` | `courses` |
| `post` | `/api/posts/**` · `/api/comments/{id}` · `/api/admin/posts/**` · `/api/admin/comments/{id}` | `posts` · `post_comments` |
| `user` | `/api/auth/**` · `/api/admin/users/**` | `users` |
| `favorite` | `/api/favorites/**` | `favorites` |
| `review` | `/api/reviews/**` | `reviews` |
| `history` | `/api/history` | `history` |
| `search` | `/api/search` | 없음 |
| `weather` | `/api/weather` | 없음 |
| `exchange` | `/api/fx` | 없음 |
| `collector` | `/api/admin/collector/**` · `/api/admin/collection-matrix` | `collector_runs` · `bulk_runs` |
| `audit` | `/api/admin/audit` | `audit_log` |
| `system` | `/api/admin/stats` · `/api/admin/cache/**` | 없음 |

경계 판단 근거:

- **`favorite` · `review` · `history` 를 셋으로 나눈다.** 지금은 `UserDataRepository`
  145줄에 섞여 있다. URL 이 다르고 서로 호출하지 않는다.
- **`weather` 와 `exchange` 를 나눈다.** 공통점이 "외부 API 를 부른다" 뿐인데, 그것은
  책임이 아니라 구현 방식이라 도메인 기준이 될 수 없다.
- **댓글은 `post` 안에 둔다.** `post_comments.post_id` FK 로 생명주기를 공유한다.
- **`system` 은 도메인이 아니다.** `stats` · `cache` 는 어디에도 속하지 않는 운영
  창구라, 갈 곳이 없어서 만드는 이름이다.
- **관리자 API 는 각 도메인이 흡수한다.** `POST /api/admin/destinations` 는
  destination 도메인의 쓰기 동작이다. URL 의 `admin` 은 누가 부를 수 있는지(권한)의
  문제지 무슨 일을 하는지(책임)의 문제가 아니다. **`admin/` 패키지를 남기지 않는다.**

### 도메인 간 의존 (전부)

```
search    → destination · festival · course   (Service)
collector → destination · festival            (Service)
system    → 전 도메인                          (Service, 카운트 전용)
StartupRunner → destination · user            (Service)
전 도메인 → audit                              (AuditService)
post      → user                              (@ManyToOne, 명시적 예외)
```

여섯 개뿐이고 전부 단방향이다. 순환이 없다.

## 공통 — `common/`

```
common/error/   NotFoundException · ConflictException · ForbiddenException
common/web/     ApiExceptionHandler
```

`ApiExceptionHandler` 는 `@RestControllerAdvice` 하나이고, 예외를 상태코드로 번역하는
것 외에 아무 일도 하지 않는다.

```
NotFoundException  → 404
ConflictException  → 409   (code 필드를 선택적으로 들고 다닌다)
ForbiddenException → 403
```

`ConflictException` 에 `code` 가 필요한 이유는 프론트가 유일하게 읽는 에러 본문이
`BULK_ALREADY_RUNNING` 이기 때문이다.

`AsyncConfig` · `CacheConfig` · `SecurityConfig` · `WebConfig` · `StartupRunner` 는
**지금 위치(`com.japantravel.common.config`)에 그대로 둔다.** 컴포넌트 스캔으로만
발견되어 아무도 import 하지 않으므로 옮길 필요가 없다. 이동은 Task 10 에서, 혹은
하지 않아도 동작에 지장이 없다.

## 스키마 매핑 규칙

스키마가 먼저 있고 엔티티가 거기 맞춘다. 추측하지 말고 `schema.sql` 을 읽는다.

| 스키마 | 매핑 |
|---|---|
| `id INTEGER PRIMARY KEY AUTOINCREMENT` | `@GeneratedValue(strategy = GenerationType.IDENTITY)` |
| 테이블명이 복수형·불규칙 (`destinations` · `history` · `audit_log`) | **`@Table(name = "...")` 필수.** 없으면 Hibernate 가 클래스명을 테이블명으로 쓴다 |
| snake_case 컬럼 (`image_path` · `wiki_title`) | `@Column(name = "...")` |
| `tags TEXT DEFAULT '[]'` · `timeline_json TEXT` — JSON 문자열인데 DTO 는 `List<String>` | **`AttributeConverter` 작성.** `List<String> ↔ JSON TEXT` 변환을 한 클래스에 가둔다. Service 마다 파싱 코드가 흩어지는 것보다 낫다 |
| `created_at TEXT DEFAULT (datetime('now'))` | 엔티티 필드도 `String` 유지 (아래 "기록만 해두는 것" 참조) |

## 전환 전략

### 핵심 제약 — 빈 이름 충돌

Spring 은 빈 이름을 **클래스 단순명**에서 만든다. 패키지가 달라도 같은 단순명이면
충돌하고 **앱이 아예 뜨지 않는다.**

```
ConflictingBeanDefinitionException: Annotation-specified bean name 'destinationController'
  for bean class [com.japantravel._repo.destination.controller.DestinationController]
  conflicts with existing, non-compatible bean definition of same name and class
  [com.japantravel.controller.DestinationController]
```

옛 리포지토리 9개는 전부 `@Repository` 가 붙어 있으므로 **리포지토리도 공존할 수 없다.**

### 해결 — 옛 클래스를 `Legacy*` 로 이름변경

도메인 하나를 옮길 때 하는 일은 이렇다. 예: destination

```
생성    _repo/destination/{controller,service,repository,entity,dtos}/
삭제    com.japantravel.controller.DestinationController
이름변경 com.japantravel.repository.DestinationRepository → LegacyDestinationRepository
        (사용처 4곳: AdminController · SearchController · StartupRunner · WikipediaCollector)
잘라냄  AdminController 에서 destinations 메서드만 제거 (파일은 남는다)
```

**새 코드가 아니라 옛 코드의 이름을 바꾼다.** 새 클래스가 처음부터 최종 이름을 갖고,
옛 클래스는 어차피 삭제되므로 이름이 지저분해도 손해가 없다. 그리고 `Legacy` 접두사가
**"아직 안 옮긴 것"의 목록** 역할을 한다 — `grep -rl Legacy` 로 남은 일이 보인다.

### 전환 중 상태

옛 `JdbcTemplate` 코드와 새 JPA 코드가 **같은 `DataSource` 를 공유하며 공존한다.**
JPA 를 추가해도 옛 코드는 한 줄도 바뀌지 않고 그대로 돈다 (검증 완료).

Task 1~2 는 `CurrentUser`(user) 와 `AuditService`(audit) 를 **옛 위치에서 import** 한다.
Task 3~4 에서 총 4줄이 바뀐다. 실수가 아니라 예정된 것이고 컴파일러가 잡아준다.

## Task 순서

| # | Task | 내용 | 규모 |
|---|---|---|---|
| 0 | `common/` | 예외 3종 + `ApiExceptionHandler`. **그것뿐** | 작음 |
| 1 | `destination` | **템플릿.** 여기서 정한 모양이 나머지 13개의 틀 | 중간 |
| 2 | `festival` | destination 의 거울. 패턴 적용 연습 | 작음 |
| 3 | `user` | auth 4종 + security 3종 + 관리자 사용자 관리 | 중간 |
| 4 | `audit` | `AuditService` | 작음 |
| 5 | `course` | `isOwnerOrAdmin` 이 컨트롤러 → Service 로 올라감 | 중간 |
| 6 | `favorite` `review` `history` | `UserDataRepository` 145줄을 셋으로 분해 | 중간 |
| 7 | `post` | 게시글 + 댓글 + `@ManyToOne User` | 중간 |
| 8 | `search` `weather` `exchange` | 작은 것 3개. `ProxyController` 분해 | 작음 |
| 9 | `collector` | 가장 큼. 수집 파이프라인 전부 | 큼 |
| 10 | `system` + 청소 | stats · cache + 옛 패키지 전멸 + `pom.xml` 삭제 + `_repo` → `com.japantravel` 승격 | 중간 |

순서의 근거:

- **`destination` 이 1번**인 이유는 가장 단순하면서 필요한 요소를 다 갖췄기 때문이다 —
  목록 · 상세 · 필터 · 관리자 CRUD. 여기서 Service 메서드를 어떻게 자르는지,
  AdminController 가 Service 를 어떻게 공유하는지, Dtos 를 어떻게 쓰는지를 확정한다.
  **1번에 시간을 가장 많이 쓰는 것이 맞다.**
- **`user` 와 `audit` 이 앞쪽(3·4번)** 인 이유는 거의 모든 도메인이 `CurrentUser` 와
  `AuditService` 를 쓰기 때문이다. 뒤에 두면 import 를 두 번 고친다.
- **`collector` 가 9번**인 이유는 destination · festival Service 에 의존하기 때문이다.

## 각 Task 의 진행 절차

1. **Claude 가 설명한다** — 해당 도메인이 무슨 일을 하는가 / 어떤 규칙이 있는가 /
   누가 의존하는가.
2. **Claude 가 명세를 준다** — 엔드포인트 계약, Service 메서드 목록과 시그니처,
   엔티티가 매핑할 컬럼, 주의점. 구현 코드는 주지 않는다.
3. **사용자가 코드를 쓴다.** 뼈대는 `spring-domain-skeleton` 스킬로 생성할 수 있다.
4. **Claude 가 검증한다** — 아래 루프.
5. **커밋한다.** Task 당 1커밋. 문제가 나면 그 Task 만 되돌린다.

명세의 상세도는 Task 1이 가장 높고 뒤로 갈수록 방향만 준다. 너무 빡빡하거나 헐거우면
사용자가 조정을 요청한다.

## 검증

테스트가 없다. 안전망은 이것뿐이다.

```bash
cd backend && ./gradlew compileJava       # 1. 컴파일
cd backend && ./gradlew bootRun           # 2. 기동 — 빈 이름/매핑 충돌이 여기서 드러난다
                                          # 3. 해당 도메인 엔드포인트 curl
git diff --stat frontend/                 # 4. 비어 있어야 한다 = 계약 유지
                                          # 5. 커밋
```

Task 0 시작 전에 **기준선(baseline)** 을 뜬다. 이후 눈으로 비교하지 말고 `diff` 로
판정한다.

```bash
curl -s localhost:8080/api/destinations > baseline/destinations.json   # 118건
# 이후
diff <(python -m json.tool baseline/destinations.json) \
     <(curl -s localhost:8080/api/destinations | python -m json.tool) && echo "동일"
```

`weather` · `fx` 는 외부 API 실시간 값이라 내용이 매번 다르다. **키 구조만** 비교한다.

### 환경 함정 2개

**① Windows Git Bash 에서 curl 에 한글을 그대로 쓰면 서버에 온전히 도달하지 않는다.**

```bash
curl "localhost:8080/api/destinations?prefecture=도쿄도"                       # → 400
curl "localhost:8080/api/destinations?prefecture=%EB%8F%84%EC%BF%84%EB%8F%84"  # → 200, 22건
```

퍼센트 인코딩이 필요하면 만든다:
`python -c "import urllib.parse;print(urllib.parse.quote('오사카부'))"`

응답 확인도 콘솔이 한글을 깨뜨리므로 `head -c` 로 읽지 말고 Python 으로 판정한다.

**② 옛 프로세스가 8080 을 잡고 있으면 검증이 거짓말을 한다.** 새로 띄운 앱이 죽었는데도
옛 인스턴스가 200 을 돌려주므로 성공으로 보인다. 검증 전에 확인한다.

```bash
netstat -ano | grep ":8080" | grep LISTEN
```

## 범위 밖

- 프론트엔드 수정
- 기능 추가 (프로필 수정 등)
- 테스트 작성
- 보안 · 로깅 · 검증 · 재시도 · 성능 추가 (프로젝트 `CLAUDE.md` 방침)
- 스키마 변경 · 마이그레이션 · DB 삭제

## 기록만 해두는 것 (나중에 별건으로)

- **죽은 테이블 3개**: `achievements` · `collections` · `collection_items` — Java 코드
  어디에서도 참조하지 않는다.
- **죽은 컬럼 3개**: `users.avatar_path` · `users.bio` · `users.default_prefecture` —
  읽지도 쓰지도 않는다. 프로필 수정 기능이 없다.
- **날짜가 TEXT**: `created_at TEXT DEFAULT (datetime('now'))`. 엔티티 필드도 `String`
  으로 둔다. `LocalDateTime` 매핑은 포맷 컨버터가 필요하고 잘못 건드리면 API 응답
  문자열이 달라져 계약이 깨진다.
- **`SearchController` 의 전체 조회 후 메모리 필터링** — 비효율이지만 동작 보존이 우선이다.
