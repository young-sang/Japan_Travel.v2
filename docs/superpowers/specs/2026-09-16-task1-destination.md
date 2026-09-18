# Task 1 — destination 도메인 명세

작성일: 2026-09-16
상위 문서: `2026-09-15-backend-rebuild-design.md`

destination 은 **템플릿**이다. 여기서 정한 모양이 나머지 13개 도메인의 틀이 된다.
명세가 가장 상세한 Task 이며, 뒤로 갈수록 방향만 준다.

---

## 0. 먼저 — Task 0 잔여

파일 4개가 껍데기 상태다. 이걸 먼저 끝낸다.

| 파일 | 현재 | 채울 것 |
|---|---|---|
| `common/error/NotFoundException` | `extends RuntimeException` 만 있음 | 생성자 `(String message)` → `super(message)` |
| `common/error/ForbiddenException` | **상속 없음** | `extends RuntimeException` + 생성자 `(String message)` |
| `common/error/ConflictException` | **상속 없음** | 아래 표 참조 |
| `common/web/ApiExceptionHandler` | 빈 클래스 | `@RestControllerAdvice` + `@ExceptionHandler` 3개 |

### `ConflictException` 멤버

| 멤버 | 형태 | 용도 |
|---|---|---|
| 생성자 A | `ConflictException(String message)` | code 없는 단순 충돌 (역할 변경 등) |
| 생성자 B | `ConflictException(String code, String message, Map<String,Object> extra)` | `BULK_ALREADY_RUNNING` |
| 메서드 | `Map<String,Object> body()` | `code`(null 이면 생략) + `message` + `extra` 를 합친 맵 |

핸들러가 `ResponseEntity.status(409).body(ex.body())` 한 줄로 끝나게 하는 것이 목적이다.
핸들러에 `if` 나 `instanceof` 가 들어가면 설계가 깨진 것이다.

### 금지

`@ExceptionHandler(Exception.class)` **catch-all 을 만들지 않는다.** 전환 기간 동안 옛
컨트롤러가 `ResponseStatusException` 을 계속 던지는데, catch-all 이 그걸 가로채면
상태코드가 바뀌어 계약이 깨진다. `ResponseStatusException` 은 Spring 기본 처리에 맡긴다.

404 · 403 응답 본문 모양은 자유다 (프론트가 읽지 않는다).

---

## 1. destination 이 하는 일

관광지 데이터의 조회와 관리. 데이터는 두 경로로 들어온다.

- **Wikipedia 수집기** (`WikipediaCollector`) 가 `wiki_title` 기준 UPSERT — Task 9
- **관리자 수동 CRUD** (`/api/admin/destinations`) — **Task 1 범위**

현재 DB 에 118건이 있다. 스키마와 데이터는 건드리지 않는다.

### 누가 destination 을 쓰는가

| 사용처 | 무엇을 | 언제 옮기나 |
|---|---|---|
| `SearchController` | `findAll(null, null)` | Task 8 |
| `WikipediaCollector` | `upsertByWiki(...)` | Task 9 |
| `StartupRunner` | `count()` | Task 10 |
| `AdminController` — stats | `count()` · `distinctPrefectures()` | Task 10 |
| `AdminController` — matrix | `countsByPrefecture()` · `maxRefreshedByPrefecture()` | Task 9 |
| `AdminController` — CRUD | `insert` · `update` · `deleteById` · `findById` | **Task 1 — 지금** |

즉 **Task 1 에서 옮기는 것은 CRUD 5개뿐이고, 나머지 7개 메서드는 Legacy 에 남는다.**
한꺼번에 다 옮기려 하지 말 것.

---

## 2. 고정된 계약

### 일반 API — `DestinationController`

| 메서드 | 경로 | 권한 | 성공 | 본문 |
|---|---|---|---|---|
| GET | `/api/destinations?prefecture=&tag=` | 🌐 공개 | 200 | `Destination[]` |
| GET | `/api/destinations/{id}` | 🌐 공개 | 200 / **404** | `Destination` |

두 파라미터 모두 optional. 정렬은 **`ORDER BY id DESC`** (옛 코드와 동일해야 baseline diff 가 통과한다).

### 관리자 API — `DestinationAdminController`

| 메서드 | 경로 | 성공 | 본문 | 실패 |
|---|---|---|---|---|
| POST | `/api/admin/destinations` | **201** | `{"id": <long>}` | — |
| PUT | `/api/admin/destinations/{id}` | **204** | 없음 | **404** (없는 id) |
| DELETE | `/api/admin/destinations/{id}` | **204** | 없음 | **없음 — 없는 id 도 204** |

DELETE 가 존재 확인을 하지 않는 것은 옛 코드 그대로다. **고치지 않는다.**

### 권한

`SecurityConfig` 가 URL 패턴으로 처리한다 (`/api/admin/**` → `hasRole("ADMIN")`).
**컨트롤러에 `@PreAuthorize` 를 붙이지 않는다.** 붙이면 권한 규칙이 두 군데로 갈라진다.

### 응답 JSON 필드명 (프론트가 읽는다 — 절대 고정)

```
id · name · prefecture · tags · lat · lng · imagePath · description · wikiTitle · lastRefreshedAt
```

---

## 3. `dtos/DestinationDtos`

record 컨테이너 하나. `private` 생성자로 인스턴스화를 막는다.

| record | 필드 | 용도 |
|---|---|---|
| `Destination` | `Long id, String name, String prefecture, List<String> tags, Double lat, Double lng, String imagePath, String description, String wikiTitle, String lastRefreshedAt` | 응답 |
| `Upsert` | `String name, String prefecture, List<String> tags, Double lat, Double lng, String imagePath, String description, String wikiTitle` | 요청 (POST · PUT) |

`lat` · `lng` 는 **`Double` (박싱)** 이어야 한다. `double` 이면 null 이 0.0 으로 바뀌어
지도에 아프리카 앞바다 핀이 꽂힌다.

### 프론트가 보내는 본문이 DTO 와 다르다 (기존 불일치 — 보존할 것)

`frontend/src/pages/admin/AdminContent.jsx:63-72` 가 보내는 것:

```js
{ name, prefecture, category, tags, summary, imageUrl, lat, lng }
```

`category` · `summary` · `imageUrl` 은 백엔드에 **없는 필드**이고,
`description` · `imagePath` · `wikiTitle` 은 **오지 않는다.**

즉 지금 관리자가 수동으로 추가한 관광지는 설명과 이미지가 null 로 저장되고,
수정하면 **`wiki_title` 이 NULL 로 덮어써진다.** 이건 기존 버그이지만
**동작 보존이 우선이므로 고치지 않는다.**

따라서:

- Jackson 의 `FAIL_ON_UNKNOWN_PROPERTIES` 를 **켜지 말 것.** (Spring Boot 기본값 off — 그대로 두면 된다)
- `Upsert` 에 오지 않는 필드는 null 로 들어오고, 그대로 저장한다. 방어 코드를 넣지 않는다.

---

## 4. `entity/Destination`

```
@Entity
@Table(name = "destinations")
```

`@Table` 은 **필수**다. 없으면 Hibernate 가 테이블명을 `Destination` 으로 찾는다.

| 컬럼 | 필드 | 매핑 |
|---|---|---|
| `id INTEGER PK AUTOINCREMENT` | `Long id` | `@Id` + `@GeneratedValue(strategy = IDENTITY)` |
| `name TEXT NOT NULL` | `String name` | 기본 |
| `prefecture TEXT NOT NULL` | `String prefecture` | 기본 |
| `tags TEXT NOT NULL DEFAULT '[]'` | `List<String> tags` | **`@Convert(converter = StringListConverter.class)`** |
| `lat REAL` | `Double lat` | 기본 |
| `lng REAL` | `Double lng` | 기본 |
| `image_path TEXT` | `String imagePath` | `@Column(name = "image_path")` |
| `description TEXT` | `String description` | 기본 |
| `wiki_title TEXT UNIQUE` | `String wikiTitle` | `@Column(name = "wiki_title")` |
| `last_refreshed_at TEXT` | `String lastRefreshedAt` | `@Column(name = "last_refreshed_at")` |

JPA 엔티티는 **기본 생성자(protected 이상)가 필요**하고 `final` 필드를 쓸 수 없다.
record 로 만들 수 없다.

### `common/jpa/StringListConverter`

`AttributeConverter<List<String>, String>` 구현. destination 과 festival 이 공유하므로
`_repo/common/jpa/` 에 둔다.

| 방향 | 동작 |
|---|---|
| `convertToDatabaseColumn` | `List<String>` → JSON 문자열. null 이면 `"[]"` |
| `convertToEntityAttribute` | JSON → `List<String>`. 파싱 실패하면 **`List.of()`** (옛 RowMapper 가 그렇게 한다) |

파싱 실패 시 예외를 던지면 안 된다. 깨진 행 하나가 목록 전체를 500 으로 만든다.

### `last_refreshed_at` — JPA 가 자동으로 못 하는 것

옛 SQL 은 INSERT · UPDATE 마다 `last_refreshed_at = datetime('now')` 를 박았다.
JPA 에는 그런 게 없으므로 **Service 가 직접 값을 만들어 넣어야 한다.**

SQLite `datetime('now')` 의 출력은 **UTC**, 포맷은 `yyyy-MM-dd HH:mm:ss` 다.

```
DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now(ZoneOffset.UTC))
```

`LocalDateTime.now()` (시스템 시간대) 를 쓰면 KST 라서 기존 118건보다 9시간 미래가 된다.
`Instant.toString()` 을 쓰면 `2026-09-16T04:30:00Z` 라 포맷이 달라진다.
둘 다 프론트의 "마지막 갱신" 표시와 수집 매트릭스를 어긋나게 한다.

`@PrePersist` / `@PreUpdate` 로 엔티티 안에서 처리해도 되지만, 그러면 수집기(Task 9)의
UPSERT 도 영향을 받는다. **Task 1 에서는 Service 에서 명시적으로 넣는 쪽을 권한다.**

---

## 5. `repository/DestinationRepository`

```java
public interface DestinationRepository extends JpaRepository<Destination, Long> { }
```

`JpaRepository` 가 `findById` · `save` · `deleteById` · `count` 를 공짜로 준다.
추가로 필요한 것은 **필터 조회 하나**다.

### 필터 조회의 문제

`prefecture` 와 `tag` 가 각각 있을 수도 없을 수도 있어 조합이 4가지다.
`tags` 는 JSON 문자열이라 `LIKE '%"온천"%'` 로 찾는다 (옛 코드와 동일한 방식이어야
결과가 같다).

파생 쿼리(`findByPrefecture...`)로는 optional 조합을 못 만든다. `@Query` 하나로 처리한다.

```java
@Query("""
    SELECT d FROM Destination d
     WHERE (:prefecture IS NULL OR d.prefecture = :prefecture)
       AND (:tagPattern IS NULL OR d.tags LIKE :tagPattern)
     ORDER BY d.id DESC
    """)
List<Destination> search(@Param("prefecture") String prefecture,
                         @Param("tagPattern") String tagPattern);
```

`tagPattern` 은 **Service 가 만들어서 넘긴다** — `tag == null ? null : "%\"" + tag + "\"%"`.
리포지토리가 `%` 와 따옴표를 조립하면 그건 규칙 판단이다.

> `d.tags` 는 컨버터가 붙은 필드지만 DB 에서는 TEXT 이므로 JPQL `LIKE` 가 동작한다.
> 만약 Hibernate 가 컨버터 때문에 타입 오류를 내면, **그 지점만** `nativeQuery = true` 로
> 우회한다 (설계 문서의 허용 사항).

빈 문자열(`?prefecture=`) 처리: 옛 코드는 `isBlank()` 도 무시했다. 이 판정은 **Service** 에서 한다.

---

## 6. `service/DestinationService`

`@Service`. 쓰기 메서드에 `@Transactional`.

| 메서드 | 시그니처 | 하는 일 |
|---|---|---|
| 목록 | `List<Dtos.Destination> list(String prefecture, String tag)` | blank → null 정규화, tagPattern 조립, 엔티티→DTO |
| 상세 | `Dtos.Destination get(long id)` | 없으면 **`NotFoundException`** |
| 생성 | `long create(Dtos.Upsert req)` | 엔티티 생성 + `lastRefreshedAt` 세팅 + save → **새 id 반환** |
| 수정 | `void update(long id, Dtos.Upsert req)` | 없으면 `NotFoundException`, 있으면 전 필드 덮어쓰기 + `lastRefreshedAt` 갱신 |
| 삭제 | `void delete(long id)` | 존재 확인 **없이** 삭제 (아래 참조) |

### 규칙

- **엔티티를 반환하지 않는다.** 변환은 Service 안에서 끝낸다. 엔티티가 컨트롤러로
  나가면 `open-in-view: false` 에서 직렬화 예외가 난다.
- **`ResponseEntity` 를 만지지 않는다.** Service 는 HTTP 를 모른다.
- 변환 메서드는 `private Dtos.Destination toDto(Destination e)` 하나로 모은다.

### 삭제의 연쇄 — 결정이 필요한 지점

스키마에 `ON DELETE CASCADE` 가 없어서 옛 코드는 손으로 지웠다:

```sql
DELETE FROM favorites WHERE target_type='destination' AND target_id=?
DELETE FROM reviews   WHERE target_type='destination' AND target_id=?
DELETE FROM history   WHERE target_type='destination' AND target_id=?
DELETE FROM destinations WHERE id=?
```

`favorite` · `review` · `history` 는 **Task 6 에 가서야 도메인이 생긴다.** 그때까지
destination 이 남의 테이블을 지워야 한다. 설계 문서의 의존 목록에 이 관계가 빠져 있다.

| 안 | 내용 | 평가 |
|---|---|---|
| **A (권장)** | Task 1 에서는 `DestinationRepository` 에 `@Modifying @Query(nativeQuery=true)` 3개를 두고, 주석으로 "Task 6 에서 각 도메인 Service 호출로 교체" 명시 | 지금 움직이고, 빚이 눈에 보인다 |
| B | Task 6 까지 Legacy 삭제 경로를 남겨둠 | Admin CRUD 가 두 군데로 갈라진다 |
| C | 지금 favorite/review/history 도 만든다 | Task 1 이 Task 6 을 삼킨다. 템플릿 역할이 흐려진다 |

**A 로 가되, `grep -rn "Task 6"` 로 찾을 수 있게 주석을 남긴다.**
Task 6 에서 `FavoriteService.deleteByTarget(String, long)` 형태로 걷어낸다.

---

## 7. 컨트롤러 2개

### `controller/DestinationController`

`@RestController` + `@RequestMapping("/api/destinations")`. 생성자 주입은 Service 하나뿐.

| 메서드 | 반환 타입 |
|---|---|
| `list` | `List<Dtos.Destination>` — `ResponseEntity` 로 감싸지 않는다 |
| `one` | `Dtos.Destination` — 404 는 Service 의 예외 + 핸들러가 만든다 |

**`if` 가 없어야 한다.** `Optional.map(...).orElse(notFound())` 도 조건 분기다. 쓰지 않는다.

### `controller/DestinationAdminController`

`@RestController` + `@RequestMapping("/api/admin/destinations")`. **같은 `DestinationService` 를 주입받는다.**

| 메서드 | 반환 |
|---|---|
| `create` | `ResponseEntity.status(201).body(Map.of("id", id))` |
| `update` | `ResponseEntity.noContent().build()` (204) |
| `delete` | `ResponseEntity.noContent().build()` (204) |

여기는 상태코드를 직접 만들어야 하므로 `ResponseEntity` 를 쓴다. 조건 분기는 여전히 없다.

> `Map.of("id", id)` 대신 `record IdResponse(long id)` 를 `Dtos` 에 두어도 좋다.
> JSON 은 `{"id":3}` 으로 동일하다. 취향이다.

### 감사 로그 (`auditService`)

옛 AdminController 는 CRUD 마다 로그를 남긴다. 계약의 일부는 아니지만 관리자 화면의
감사 로그 탭에 보인다. **동작 보존을 위해 유지한다.**

| 동작 | 호출 |
|---|---|
| create | `auditService.log(currentUser, "CONTENT_CREATE", "destination", id, "name=" + name)` |
| update | `auditService.log(currentUser, "CONTENT_UPDATE", "destination", id, "name=" + name)` |
| delete | `auditService.log(currentUser, "CONTENT_DELETE", "destination", id, null)` |

**Task 1 에서는 옛 위치에서 import 한다** — `com.japantravel.service.AuditService`,
`com.japantravel.security.CurrentUser`. Task 4 에서 새 위치로 바뀌며 import 2줄이 수정된다.
실수가 아니라 예정된 것이고 컴파일러가 잡아준다.

감사 로그 호출은 **Service 에 둔다.** 컨트롤러 2개(일반·관리자)가 갈라져도 로그가
한 군데에 남는다.

---

## 8. 옛 코드 정리 (같은 커밋에서)

빈 이름이 충돌하면 **앱이 아예 뜨지 않는다.** 새 클래스를 만들기만 하고 옛것을
그대로 두면 `ConflictingBeanDefinitionException` 이 난다.

| 대상 | 조치 |
|---|---|
| `controller/DestinationController` | **삭제** (새것이 대체) |
| `repository/DestinationRepository` | **`LegacyDestinationRepository` 로 이름변경** |
| ↳ 사용처 `SearchController` | 타입·필드명 수정 |
| ↳ 사용처 `WikipediaCollector` | 타입·필드명 수정 |
| ↳ 사용처 `StartupRunner` | 타입·필드명 수정 |
| ↳ 사용처 `AdminController` | 타입 수정 + **destination CRUD 메서드 3개 제거** (313~333행) |
| `dto/Dtos.Destination` | **남겨둔다.** Legacy 리포지토리가 아직 쓴다 |

새 클래스가 처음부터 최종 이름을 갖고, 옛 클래스 이름이 지저분해진다.
`grep -rl Legacy` 가 **"아직 안 옮긴 것" 목록** 역할을 한다.

---

## 9. 검증

```bash
# 0. 옛 프로세스가 8080 을 잡고 있으면 검증이 거짓말을 한다
netstat -ano | grep ":8080" | grep LISTEN

# 1. 컴파일
cd backend && ./gradlew compileJava

# 2. 기동 — 빈 이름 충돌 · 엔티티 매핑 오류가 여기서 드러난다
cd backend && ./gradlew bootRun

# 3. baseline 과 동일한가
diff <(python -m json.tool baseline/destinations.json) \
     <(curl -s localhost:8080/api/destinations | python -m json.tool) && echo "동일"

# 4. 필터 (한글은 반드시 퍼센트 인코딩 — Git Bash 가 깨뜨린다)
#    python -c "import urllib.parse;print(urllib.parse.quote('도쿄도'))"
curl -s "localhost:8080/api/destinations?prefecture=%EB%8F%84%EC%BF%84%EB%8F%84" \
  | python -c "import sys,json;print(len(json.load(sys.stdin)))"      # 22

# 5. 404
curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/api/destinations/99999   # 404

# 6. 계약 유지의 증거
git diff --stat frontend/                                             # 비어 있어야 함
```

관리자 CRUD 는 ADMIN 세션 쿠키가 필요하다 (`c.txt` 가 그 용도로 만들어진 파일).

```bash
curl -s -c c.txt -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' -d '{"username":"...","password":"..."}'
curl -s -b c.txt -X POST localhost:8080/api/admin/destinations \
  -H 'Content-Type: application/json' \
  -d '{"name":"테스트","prefecture":"도쿄도","tags":["온천"],"lat":null,"lng":null}' -w "\n%{http_code}\n"
# → 201 {"id":N} · 만든 뒤 DELETE 로 지워서 118건으로 되돌릴 것
```

**검증 후 테스트 행을 반드시 지운다.** 남기면 이후 Task 의 baseline diff 가 전부 깨진다.

---

## 10. 커밋

Task 당 1커밋. 문제가 나면 그 Task 만 되돌린다.

```
feat(destination): destination 도메인 신규 작성, 옛 리포지토리 Legacy 로 분리
```

---

## 시작 전 — baseline

아직 `baseline/` 이 없다. **코드를 쓰기 전에** 떠야 한다.

```bash
cd backend && ./gradlew bootRun    # 별도 터미널
mkdir -p baseline
curl -s localhost:8080/api/destinations > baseline/destinations.json
python -c "import json;print(len(json.load(open('baseline/destinations.json'))))"   # 118
```

`baseline/` · `c.txt` · `data/*.db-wal` · `data/*.db-shm` 은 `.gitignore` 에 넣는다.
