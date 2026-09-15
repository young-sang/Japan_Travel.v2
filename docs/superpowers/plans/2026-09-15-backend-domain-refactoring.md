# 백엔드 도메인 리팩토링 실행 계획

> **작업 방식:** 코드는 **사용자가 직접 수정한다.** Claude는 각 Task마다
> ① 도메인 설명 → ② 작업 지시 → ③ (사용자 수정) → ④ 검증 → ⑤ 커밋 을 담당한다.
> 체크박스(`- [ ]`)로 진행 상황을 추적한다.

**설계 문서:** [2026-09-15-backend-domain-refactoring-design.md](../specs/2026-09-15-backend-domain-refactoring-design.md)

**목표:** 계층 기준 패키지를 도메인 기준으로 재편하면서 프로젝트 코드를 이해한다.

**아키텍처:** 도메인마다 `Controller` / `Service` / `Repository` / `Dtos` 4종을
같은 패키지에 둔다. 관리자 API는 별도 `admin/` 패키지 없이 각 도메인의
`<Domain>AdminController`로 흡수한다. API 계약(경로·메서드·JSON)은 불변이다.

**기술 스택:** Java 17 · Spring Boot 3.3 · JdbcTemplate · SQLite · Maven

---

## 핵심 불변식

이 리팩토링이 코드로 강제하려는 규칙은 하나다.

> **한 도메인의 Repository는 그 도메인의 Service만 호출한다.**

현재는 이게 깨져 있다. `StartupRunner`·`WikipediaCollector`·`SearchController`가
`DestinationRepository`를 직접 부른다. 리팩토링 후에는 전부 `DestinationService`를
거친다. 그 결과 **"누가 이 도메인을 쓰는가"를 Service의 public 메서드 목록만 보면
알 수 있게 된다.**

## 함정: curl + 한글 쿼리 파라미터 (Task 0에서 실제로 밟음)

Windows Git Bash에서 **curl 명령에 한글을 그대로 쓰면 서버에 온전히 도달하지 않는다.**

```bash
curl -s "localhost:8080/api/destinations?prefecture=도쿄도"
#   → HTTP 400 (Tomcat이 URL의 raw 비ASCII 바이트를 거부)

curl -s -G "localhost:8080/api/destinations" --data-urlencode "prefecture=도쿄도"
#   → 200 이지만 [] (이미 깨진 바이트를 퍼센트 인코딩해서 보냄)
```

**반드시 퍼센트 인코딩된 ASCII URL을 쓴다.**

```bash
curl -s "localhost:8080/api/destinations?prefecture=%EB%8F%84%EC%BF%84%EB%8F%84"
#   → 200, 22건 ✅
```

자주 쓰는 값:

| 값 | 퍼센트 인코딩 |
|---|---|
| 도쿄도 | `%EB%8F%84%EC%BF%84%EB%8F%84` |
| 교토부 | `%EA%B5%90%ED%86%A0%EB%B6%80` |
| 나라현 | `%EB%82%98%EB%9D%BC%ED%98%84` |
| 온천 | `%EC%98%A8%EC%B2%9C` |

필요하면 직접 만든다: `python -c "import urllib.parse;print(urllib.parse.quote('오사카부'))"`

**응답 확인도 마찬가지다.** 콘솔이 한글을 깨뜨려 보여주므로 `head -c`로 눈으로 읽지 말고
파이썬으로 판정한다.

```bash
python -c "
import json; d=json.load(open('out.json',encoding='utf-8'))
print(len(d), json.dumps(d[0],ensure_ascii=True)[:200])
"
```

## 기준선 (baseline)

Task 0 시점의 응답을 스크래치패드에 떠 두었다. 이후 매 Task에서 **눈으로 비교하지 말고
`diff`로 판정한다.**

```
<scratchpad>/baseline/
  destinations.json         88096 bytes · 118건
  destinations-tokyo.json   22건
  festivals.json            18275 bytes
  courses.json              847 bytes
  destination-1.json
  search-onsen.json         dest=2 fest=0 course=0
  weather-kyoto.json        (값은 실시간이라 구조만 비교)
```

검증 예:

```bash
curl -s localhost:8080/api/destinations > /tmp/now.json
diff <(python -m json.tool "$BL/destinations.json") <(python -m json.tool /tmp/now.json) \
  && echo "동일 ✅"
```

`weather`·`fx`는 외부 API 실시간 값이라 내용이 매번 다르다. **키 구조만** 비교한다.

## 검증 루프

테스트가 없다. 매 Task 끝에 이 3단계를 돌린다.

```bash
# 1) 컴파일
cd backend && mvn -q compile

# 2) 기동 (별도 터미널, 백그라운드)
cd backend && mvn spring-boot:run

# 3) 해당 도메인 엔드포인트 스모크 — Task마다 명시
```

그리고 **`git diff --stat frontend/`가 비어 있어야 한다.** 프론트가 바뀌었다면
API 계약을 깬 것이다.

## 예상되는 임시 import

Task 1~2(destination·festival)는 `CurrentUser`(account)와 `AuditService`(audit)를
**옛 위치에서 import**한다. 이 두 도메인은 Task 3~4에서 옮겨지므로, 그때
`import com.japantravel.security.CurrentUser` → `com.japantravel.audit.AuditService` 식으로
**총 4줄이 바뀐다.** 이건 실수가 아니라 예정된 것이다. `mvn compile`이 잡아준다.

destination을 먼저 하는 이유는 가장 단순해서 패턴을 세우기 좋기 때문이다.
account를 먼저 하면 임시 import는 없어지지만, 패턴도 없는 상태에서 인증이라는
가장 엉킨 도메인부터 손대게 된다.

---

## 목표 구조

```
com.japantravel/
  JapanTravelApplication.java
  common/config/   AsyncConfig · CacheConfig · SecurityConfig · WebConfig · StartupRunner
  destination/     Controller · AdminController · Service · Repository · Dtos
  festival/        Controller · AdminController · Service · Repository · Dtos
  account/         AuthController · AdminController · Service · UserRepository · Dtos
                   AppUserPrincipal · CurrentUser · UserDetailsServiceImpl
  audit/           AdminController · Service · Repository
  course/          Controller · Service · Repository · Dtos
  favorite/        Controller · Service · Repository · Dtos
  review/          Controller · Service · Repository · Dtos
  history/         Controller · Service · Repository · Dtos
  post/            Controller · AdminController · Service · Repository · Dtos
  search/          Controller · Service
  weather/         Controller · Service · OpenMeteoClient
  exchange/        Controller · Service · FrankfurterClient
  collector/       AdminController · Service · CollectorRunRepository · BulkRunRepository · Dtos
                   WikipediaCollector · WikipediaClient · NominatimClient · FetchResult
                   PrefectureCatalog · TagInferrer · ZombieWatcher · BackfillCoordsRunner
  system/          AdminController · Service
```

**설계 문서에서 수정된 점:** `FetchResult`를 `common/`이 아니라 `collector/`에 둔다.
사용처가 `NominatimClient`·`WikipediaClient`·`WikipediaCollector`·`BackfillCoordsRunner`
넷뿐이고 전부 collector 소속이다. `OpenMeteoClient`·`FrankfurterClient`는 쓰지 않는다.

**DTO 컨테이너 규칙:** 도메인마다 `<Domain>Dtos.java` 한 파일에 record를 모은다.
record가 하나뿐인 도메인(destination·festival·history)에는 과한 포장이지만,
"어느 도메인이든 같은 자리에 같은 것이 있다"는 예측 가능성을 택했다.
Task 1에서 실제로 만들어 보고 마음에 안 들면 top-level record로 바꿔도 된다 —
**단, 바꾼다면 Task 1에서 바꿔야 한다.** 이후 9개 Task가 이 모양을 따른다.

---

## Task 0: `common/` — 설정 클래스 이동

로직이 없는 순수 이동이다. 몸풀기 겸 `git mv` + package 선언 수정 흐름을 익힌다.

**중요:** `com.japantravel.config`를 **import하는 파일이 하나도 없다.** 5개 클래스는
전부 컴포넌트 스캔으로만 발견된다. 따라서 import 수정이 0건이다.

**Files:**
- Move: `backend/src/main/java/com/japantravel/config/{AsyncConfig,CacheConfig,SecurityConfig,WebConfig,StartupRunner}.java`
  → `backend/src/main/java/com/japantravel/common/config/`
- Delete: `backend/src/main/java/com/japantravel/controller/AdminController.java.tmp.7656.53fcf8a4b0e9`

- [x] **Step 1: 잔재 파일 삭제**

`AdminController.java`와 바이트 단위로 동일한 복사본이고, `.gitignore`의 `*.tmp.*`에
걸려 추적되지 않는다. 지워도 잃는 것이 없다.

```bash
rm "backend/src/main/java/com/japantravel/controller/AdminController.java.tmp.7656.53fcf8a4b0e9"
```

- [x] **Step 2: 디렉터리 생성 후 5개 파일 이동**

```bash
mkdir -p backend/src/main/java/com/japantravel/common/config
cd backend/src/main/java/com/japantravel
for f in AsyncConfig CacheConfig SecurityConfig WebConfig StartupRunner; do
  git mv "config/$f.java" "common/config/$f.java"
done
rmdir config
```

`git mv`를 쓰는 이유: `git log --follow`로 파일 히스토리가 이어진다.
`mv` + `git add`로도 결과는 같지만 rename 감지가 안 될 수 있다.

- [x] **Step 3: 5개 파일의 package 선언 수정**

각 파일 **1행**을 바꾼다.

```java
// 변경 전
package com.japantravel.config;

// 변경 후
package com.japantravel.common.config;
```

한 번에 하려면:

```bash
cd backend/src/main/java/com/japantravel/common/config
sed -i 's/^package com\.japantravel\.config;$/package com.japantravel.common.config;/' *.java
grep -n "^package" *.java   # 5줄 전부 common.config 인지 확인
```

- [x] **Step 4: 컴파일**

```bash
cd backend && mvn -q compile
```

기대: 출력 없이 종료(성공). 에러가 나면 package 선언 오타다.

- [x] **Step 5: 기동 + 스모크**

```bash
cd backend && mvn spring-boot:run
# 다른 터미널에서
curl -s localhost:8080/api/destinations | head -c 200
curl -s localhost:8080/api/fx
```

기대: 기동 로그에 에러 없음. 두 엔드포인트가 Task 0 이전과 동일하게 응답.
`SecurityConfig`가 스캔되지 않았다면 인증이 통째로 풀리므로, **인증 필요한
엔드포인트도 확인한다:**

```bash
curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/api/favorites
```

기대: `401` 또는 `403` (비로그인이므로 거부). `200`이 나오면 SecurityConfig가
안 잡힌 것이다.

- [x] **Step 6: 커밋**

```bash
git add -A backend/src/main/java/com/japantravel
git commit -m "refactor(common): config 5종을 common/config 패키지로 이동"
```

---

## Task 1: `destination/` — 템플릿 단계

**여기서 정한 모양이 나머지 8개 Task의 틀이 된다.** 서두르지 않는다.

이 Task는 세 가지를 동시에 확립한다:
1. `Dtos.java`에서 도메인 DTO를 떼어내는 방법
2. Service를 신설하고 Repository 직접 호출을 전부 걷어내는 방법
3. `AdminController`에서 도메인 관리 API를 흡수하는 방법

**Files:**
- Create: `backend/src/main/java/com/japantravel/destination/DestinationDtos.java`
- Create: `backend/src/main/java/com/japantravel/destination/DestinationService.java`
- Create: `backend/src/main/java/com/japantravel/destination/DestinationAdminController.java`
- Move: `controller/DestinationController.java` → `destination/DestinationController.java`
- Move: `repository/DestinationRepository.java` → `destination/DestinationRepository.java`
- Modify: `dto/Dtos.java` (Destination record 제거)
- Modify: `controller/AdminController.java` (destination 관련 8곳 제거/위임)
- Modify: `controller/SearchController.java` (Repository → Service)
- Modify: `common/config/StartupRunner.java` (Repository → Service)
- Modify: `collector/WikipediaCollector.java` (Repository → Service)

### 현재 호출자 전수 조사 결과

`DestinationRepository`를 부르는 곳은 정확히 여기뿐이다. Service는 이 목록을
빠짐없이 덮어야 한다.

| 호출처 | 호출 메서드 |
|---|---|
| `DestinationController` | `findAll(pref,tag)` · `findById(id)` |
| `SearchController:27` | `findAll(null,null)` |
| `StartupRunner:59` | `count()` |
| `WikipediaCollector:261` | `upsertByWiki(...)` |
| `AdminController:95,100` | `count()` · `distinctPrefectures()` |
| `AdminController:107,109` | `countsByPrefecture()` · `maxRefreshedByPrefecture()` |
| `AdminController:316,323,324,331` | `insert(d)` · `findById(id)` · `update(id,d)` · `deleteById(id)` |

- [ ] **Step 1: `DestinationDtos.java` 생성**

`Dtos.java`의 `Destination` record를 **필드명·순서·타입 그대로** 옮긴다.
한 글자라도 바꾸면 JSON이 바뀌고 프론트가 깨진다.

```java
package com.japantravel.destination;

import java.util.List;

public final class DestinationDtos {
    private DestinationDtos() {}

    public record Destination(
            Long id, String name, String prefecture, List<String> tags,
            Double lat, Double lng, String imagePath, String description,
            String wikiTitle, String lastRefreshedAt
    ) {}
}
```

- [ ] **Step 2: `Dtos.java`에서 `Destination` record 삭제**

`backend/src/main/java/com/japantravel/dto/Dtos.java`의 8~12행을 지운다.

```java
    public record Destination(
            Long id, String name, String prefecture, List<String> tags,
            Double lat, Double lng, String imagePath, String description,
            String wikiTitle, String lastRefreshedAt
    ) {}
```

이 시점부터 컴파일이 깨진다. 정상이다. Step 7까지 계속 깨져 있다.

- [ ] **Step 3: Repository 이동 + package/import 수정**

```bash
mkdir -p backend/src/main/java/com/japantravel/destination
cd backend/src/main/java/com/japantravel
git mv repository/DestinationRepository.java destination/DestinationRepository.java
```

파일 안에서 2줄을 바꾼다.

```java
// 1행
package com.japantravel.repository;          // 변경 전
package com.japantravel.destination;         // 변경 후

// import 줄
import com.japantravel.dto.Dtos.Destination;              // 변경 전
import com.japantravel.destination.DestinationDtos.Destination;  // 변경 후
```

**같은 패키지이므로 import를 아예 지우고 `DestinationDtos.Destination`으로 쓰거나,
`import static`이 아닌 중첩 클래스 import를 그대로 남겨도 된다.** 후자가 기존
코드 변경이 적으므로 권장한다.

Repository 내부 코드는 **한 줄도 바꾸지 않는다.** SQL·메서드 시그니처 전부 그대로다.

- [ ] **Step 4: `DestinationService.java` 생성**

Step 1의 호출자 표를 그대로 덮는다. 지금은 대부분 Repository로 위임만 한다.
**관리자 쓰기 3종(create/update/delete)에는 감사 로그가 함께 들어간다** —
`AdminController`에 흩어져 있던 것을 여기로 모은다. 이게 "규칙이 Service로
올라간다"의 실제 모습이다.

```java
package com.japantravel.destination;

import com.japantravel.destination.DestinationDtos.Destination;
import com.japantravel.security.CurrentUser;
import com.japantravel.service.AuditService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DestinationService {

    private final DestinationRepository repo;
    private final AuditService auditService;
    private final CurrentUser currentUser;

    public DestinationService(DestinationRepository repo,
                              AuditService auditService,
                              CurrentUser currentUser) {
        this.repo = repo;
        this.auditService = auditService;
        this.currentUser = currentUser;
    }

    // ── 조회 ───────────────────────────────────────────────────────────
    public List<Destination> list(String prefecture, String tag) {
        return repo.findAll(prefecture, tag);
    }

    public Optional<Destination> findById(long id) {
        return repo.findById(id);
    }

    // ── 통계 (관리자 대시보드·수집 현황) ────────────────────────────────
    public int count() {
        return repo.count();
    }

    public List<String> distinctPrefectures() {
        return repo.distinctPrefectures();
    }

    public Map<String, Integer> countsByPrefecture() {
        return repo.countsByPrefecture();
    }

    public Map<String, String> maxRefreshedByPrefecture() {
        return repo.maxRefreshedByPrefecture();
    }

    // ── 수집기 진입점 ──────────────────────────────────────────────────
    /** Wikipedia title 기준 UPSERT. 새로 추가되면 true. */
    public boolean upsertByWiki(String name, String prefecture, List<String> tags,
                                Double lat, Double lng, String imagePath,
                                String description, String wikiTitle) {
        return repo.upsertByWiki(name, prefecture, tags, lat, lng,
                imagePath, description, wikiTitle);
    }

    // ── 관리자 쓰기 (감사 로그 포함) ────────────────────────────────────
    public long create(Destination d) {
        long id = repo.insert(d);
        auditService.log(currentUser, "CONTENT_CREATE", "destination", id, "name=" + d.name());
        return id;
    }

    /** 대상이 없으면 false. */
    public boolean update(long id, Destination d) {
        if (repo.findById(id).isEmpty()) return false;
        repo.update(id, d);
        auditService.log(currentUser, "CONTENT_UPDATE", "destination", id, "name=" + d.name());
        return true;
    }

    public void delete(long id) {
        repo.deleteById(id);
        auditService.log(currentUser, "CONTENT_DELETE", "destination", id, null);
    }
}
```

`CurrentUser`와 `AuditService`는 아직 옛 패키지에 있다. Task 3~4에서 이 2줄이
바뀐다 — 예정된 일이다.

- [ ] **Step 5: `DestinationController` 이동 + Service 사용**

```bash
cd backend/src/main/java/com/japantravel
git mv controller/DestinationController.java destination/DestinationController.java
```

파일 전체를 이렇게 바꾼다. **`@RequestMapping("/api/destinations")`과 메서드
시그니처는 그대로다** — URL과 쿼리 파라미터가 계약이다.

```java
package com.japantravel.destination;

import com.japantravel.destination.DestinationDtos.Destination;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationService service;
    public DestinationController(DestinationService service) { this.service = service; }

    @GetMapping
    public List<Destination> list(@RequestParam(required = false) String prefecture,
                                  @RequestParam(required = false) String tag) {
        return service.list(prefecture, tag);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Destination> one(@PathVariable long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
```

- [ ] **Step 6: `DestinationAdminController.java` 생성**

`AdminController`의 314~334행(`/destinations` 3개 엔드포인트)을 여기로 옮긴다.
**경로는 `/api/admin/destinations` 그대로 유지한다.** 감사 로그는 Service로
올라갔으므로 여기엔 없다.

```java
package com.japantravel.destination;

import com.japantravel.destination.DestinationDtos.Destination;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/destinations")
public class DestinationAdminController {

    private final DestinationService service;
    public DestinationAdminController(DestinationService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Destination d) {
        long id = service.create(d);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable long id, @RequestBody Destination d) {
        if (!service.update(id, d)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

> **권한은 자동으로 따라온다 (확인됨):** `SecurityConfig`는 **경로 기반**이다 —
> `.requestMatchers("/api/admin/**").hasRole("ADMIN")`. 클래스에 `@PreAuthorize`가
> 붙어 있지 않으므로, 새 컨트롤러를 `/api/admin/destinations`에 두기만 하면
> 같은 규칙이 적용된다. 옮길 애너테이션은 없다.
>
> 이것이 **관리자 API를 도메인으로 흩어도 안전한 이유**다. 경로 규약(`/api/admin/**`)만
> 지키면 컨트롤러가 어느 패키지에 있든 권한은 동일하다. Step 9에서 실제로 확인한다.

- [ ] **Step 7: 나머지 4개 호출처를 Service로 전환**

`AdminController.java`:
- destination CRUD 3개 메서드(314~334행)와 `destRepo` 필드·생성자 인자를 제거
- 남은 통계 호출 4곳을 `destService`로 교체: 95·100·107·109행
- `import com.japantravel.repository.*` 는 그대로 두되
  `import com.japantravel.destination.DestinationService;` 추가,
  `import com.japantravel.dto.Dtos.Destination;` 제거

`SearchController.java:27`:
```java
// 변경 전
private final DestinationRepository destRepo;
var dests = destRepo.findAll(null, null).stream()

// 변경 후
private final DestinationService destService;
var dests = destService.list(null, null).stream()
```

`common/config/StartupRunner.java:59`:
```java
int existing = destService.count();   // destRepo.count() 에서 변경
```

`collector/WikipediaCollector.java:261`:
```java
isNew = destService.upsertByWiki(     // destRepo.upsertByWiki(...) 에서 변경
```

네 파일 모두 생성자 주입 타입을 `DestinationRepository` → `DestinationService`로
바꾸고 import를 정리한다.

- [ ] **Step 8: 컴파일 + 검증**

```bash
cd backend && mvn -q compile
```

기대: 성공. 실패하면 남은 `DestinationRepository` 직접 참조가 있다는 뜻이다.

**불변식 확인 — 이게 이번 Task의 진짜 산출물이다:**

```bash
cd "d:/01_Program/00_Project/~2026.06/05_Capstone/Japan_Travel.v2"
grep -rn "DestinationRepository" backend/src/main/java --include=*.java
```

기대: `destination/DestinationRepository.java`(자기 자신)와
`destination/DestinationService.java` **두 곳에서만** 나와야 한다.
다른 곳에서 나오면 전환이 덜 된 것이다.

- [ ] **Step 9: 기동 + 스모크**

```bash
cd backend && mvn spring-boot:run
```

```bash
# 조회 (비로그인 허용)
curl -s "localhost:8080/api/destinations?prefecture=%EB%8F%84%EC%BF%84%EB%8F%84" > /tmp/now.json
python -c "import json;d=json.load(open('/tmp/now.json',encoding='utf-8'));print(len(d),'건 (기준선: 22건)')"
curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/api/destinations/1
curl -s "localhost:8080/api/search?q=%EC%98%A8%EC%B2%9C"   # 기준선: dest=2 fest=0 course=0

# 관리자 경로가 여전히 보호되는지 (비로그인)
curl -s -o /dev/null -w "%{http_code}\n" -X POST localhost:8080/api/admin/destinations \
  -H "Content-Type: application/json" --data-binary '{"name":"x"}'
```

기대:
- 조회 3건이 Task 1 이전과 **동일한 JSON**
- 마지막 POST는 `401` 또는 `403`. **`201`이 나오면 관리자 API가 무방비로 열린 것이다 —
  즉시 중단하고 SecurityConfig를 확인한다.**

브라우저에서 `localhost:5173`을 열어 여행지 목록·상세·검색이 뜨는지도 본다.

- [ ] **Step 10: 프론트 무변경 확인 + 커밋**

```bash
git diff --stat frontend/          # 출력이 비어 있어야 한다
git add -A backend/src/main/java
git commit -m "refactor(destination): destination 도메인 패키지로 재편

- DestinationDtos 분리, DestinationService 신설
- AdminController의 /api/admin/destinations 를 DestinationAdminController 로 이관
- 감사 로그를 Service 로 이동
- StartupRunner/WikipediaCollector/SearchController 가 Repository 대신 Service 사용"
```

---

## Task 2 이후 — 공통 절차

Task 2부터는 Task 1에서 확립한 패턴을 적용한다. 계획서에 전체 코드를 싣지 않는
이유는, 패턴을 스스로 적용해 보는 것이 이 작업의 목적이기 때문이다.

**각 Task 시작 시 Claude가 먼저 제공하는 것:**

1. **도메인 설명** — 무슨 일을 하는가 / 어떤 비즈니스 규칙이 있는가 / 누가 의존하는가
2. **호출자 전수 조사표** — Task 1의 표와 같은 형식. Service가 덮어야 할 메서드 목록
3. **파일 이동/생성 목록** — 정확한 경로
4. **주의점** — 이 도메인 고유의 함정

**사용자가 수행하는 것:** Task 1의 Step 1~10과 같은 흐름.

**매 Task 공통 검증:**

```bash
cd backend && mvn -q compile
grep -rn "<Domain>Repository" backend/src/main/java --include=*.java
#   → 그 도메인 패키지 안 2개 파일에서만 나와야 한다
git diff --stat frontend/          # 비어 있어야 한다
```

---

## Task 2: `festival/`

destination의 거울이다. 구조가 거의 동일하므로 Task 1 코드를 그대로 대응시킨다.

**Files:**
- Create: `festival/FestivalDtos.java` · `FestivalService.java` · `FestivalAdminController.java`
- Move: `controller/FestivalController.java` · `repository/FestivalRepository.java` → `festival/`
- Modify: `dto/Dtos.java` (Festival record 제거) · `controller/AdminController.java`
  (`/festivals` 3개 엔드포인트 + 통계 호출 제거) · `controller/SearchController.java`
  · `collector/WikipediaCollector.java`

**주의점:** `Festival` record는 필드가 11개로 `Destination`(10개)과 다르다.
`month`·`dateText`가 추가되고 순서도 다르다. 복사 후 필드를 그대로 두었는지 확인한다.

**스모크:**
```bash
curl -s "localhost:8080/api/festivals?prefecture=%EA%B5%90%ED%86%A0%EB%B6%80"
curl -s -o /dev/null -w "%{http_code}\n" -X POST localhost:8080/api/admin/festivals \
  -H "Content-Type: application/json" --data-binary '{"name":"x"}'   # 401/403 기대
```

---

## Task 3: `account/`

가장 엉킨 도메인이다. 인증이 걸려 있으므로 스모크를 가장 꼼꼼히 한다.

**Files:**
- Create: `account/AccountService.java` · `account/AccountAdminController.java` · `account/AccountDtos.java`
  (`User`·`SignupRequest`·`LoginRequest` record)
- Move: `controller/AuthController.java` · `repository/UserRepository.java`
  · `security/{AppUserPrincipal,CurrentUser,UserDetailsServiceImpl}.java` → `account/`
- Modify: `dto/Dtos.java` · `controller/AdminController.java` (`/users` 3개 엔드포인트)
  · **`destination/DestinationService.java`·`festival/FestivalService.java`의
    `import com.japantravel.security.CurrentUser` → `com.japantravel.account.CurrentUser`**
  · `common/config/SecurityConfig.java` (`UserDetailsServiceImpl` import)
  · `CurrentUser`를 쓰는 나머지 전부

**주의점:**
- `SecurityConfig`가 `UserDetailsServiceImpl`을 참조한다. import를 놓치면 기동 자체가 실패한다.
- `CurrentUser`는 거의 모든 컨트롤러가 쓴다. 이동 전에
  `grep -rn "security.CurrentUser" backend/src/main/java`로 대상 목록을 먼저 뽑는다.
- `AuthController`의 세션 쿠키 처리는 건드리지 않는다.

**스모크 (로그인 왕복 전체):**
```bash
curl -s -c /tmp/c.txt -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" --data-binary '{"username":"admin","password":"admin1234"}'
curl -s -b /tmp/c.txt localhost:8080/api/auth/me
curl -s -b /tmp/c.txt localhost:8080/api/admin/users | head -c 200
curl -s -b /tmp/c.txt -X POST localhost:8080/api/auth/logout
curl -s -o /dev/null -w "%{http_code}\n" -b /tmp/c.txt localhost:8080/api/auth/me   # 401 기대
```

---

## Task 4: `audit/`

작다. `AuditService`·`AuditLogRepository`·`AdminController`의 `/audit` 엔드포인트.

**Files:**
- Create: `audit/AuditAdminController.java`
- Move: `service/AuditService.java` · `repository/AuditLogRepository.java` → `audit/`
- Modify: `controller/AdminController.java` (413행 `/audit` 제거)
  · **`AuditService`를 import하는 모든 파일** — Task 1~3에서 만든 Service들 포함

**주의점:** Task 1~2의 임시 import 2줄이 여기서 최종 위치로 바뀐다.
`grep -rn "service.AuditService" backend/src/main/java`로 대상을 먼저 뽑는다.
이 Task가 끝나면 `com.japantravel.service` 패키지가 비어서 삭제된다.

**스모크:** 로그인 후 `curl -s -b /tmp/c.txt localhost:8080/api/admin/audit | head -c 300`
그리고 여행지를 하나 수정한 뒤 감사 로그에 `CONTENT_UPDATE`가 남는지 확인한다.

---

## Task 5: `course/`

비즈니스 규칙이 처음으로 제대로 등장한다. `CourseController.isOwnerOrAdmin`이
`CourseService`로 올라간다.

**Files:**
- Create: `course/CourseService.java` · `course/CourseDtos.java` (`Course`·`CourseStop`)
- Move: `controller/CourseController.java` · `repository/CourseRepository.java` → `course/`
- Modify: `dto/Dtos.java` · `controller/AdminController.java` · `controller/SearchController.java`

**주의점:**
- `isOwnerOrAdmin`은 "소유자이거나 ADMIN이면 허용"이다. Service로 옮길 때
  403 응답 결정은 컨트롤러에 남기고 **판정만** Service로 올린다
  (`boolean canModify(Course c)` 또는 예외 방식 — Task 시작 시 상의).
- `create`의 기본값 처리(`status == null ? "published"`, `tags == null ? List.of()`)도
  Service로 올린다.
- `mine=true`일 때 비로그인이면 빈 목록을 반환하는 동작을 유지한다.

**스모크:** 로그인 후 코스 생성 → 수정 → 다른 계정으로 수정 시도(403 기대) → 삭제.

---

## Task 6: `favorite/` `review/` `history/`

`UserDataRepository` 145줄을 세 Repository로 가른다. SQL은 테이블별로 이미
깔끔하게 갈려 있어 기계적이다.

**Files:**
- Create: `favorite/` `review/` `history/` 각각 Controller · Service · Repository · Dtos
- Delete: `controller/UserDataController.java` · `repository/UserDataRepository.java`
  (내용은 셋으로 분배)
- Modify: `dto/Dtos.java` · `controller/AdminController.java`
  · `destination/DestinationRepository.deleteById`가 favorites/reviews/history를
    직접 지우는 부분 — **아래 주의점 참조**

**주의점 — 이 Task의 핵심 함정:**
`DestinationRepository.deleteById`가 `favorites`·`reviews`·`history` 테이블을
직접 DELETE한다 (스키마에 `ON DELETE CASCADE`가 없어서). 세 도메인으로 가르고
나면 **destination이 남의 테이블을 직접 만지는 꼴**이 되어 불변식을 깬다.
festival·course의 `deleteById`도 마찬가지일 가능성이 높다.

선택지는 세 가지고, Task 시작 때 상의해서 정한다:
1. 그대로 둔다 (불변식에 예외를 인정하고 주석으로 명시)
2. `FavoriteService.removeAllFor(type, id)` 등을 만들어 destination이 호출
3. `schema.sql`에 `ON DELETE CASCADE` 추가 — **DB 재생성이 필요하므로 범위 밖**

**스모크:** 로그인 후 즐겨찾기 추가/조회/삭제, 리뷰 작성/수정/삭제, 히스토리
기록/조회/비우기. 그리고 **여행지를 삭제했을 때 그에 달린 즐겨찾기·리뷰가
같이 지워지는지** 확인한다.

---

## Task 7: `post/`

게시글·댓글. DTO가 5개로 가장 많다.

**Files:**
- Create: `post/PostService.java` · `post/PostAdminController.java` · `post/PostDtos.java`
  (`Post`·`PostCreate`·`PostComment`·`CommentCreate`·`PostPage`)
- Move: `controller/PostController.java` · `repository/PostRepository.java` → `post/`
- Modify: `dto/Dtos.java` · `controller/AdminController.java` (68·77·84행)

**주의점:**
- `PostController`의 `@RequestMapping("/api")`가 `/posts`와 `/comments` 두 갈래를
  담당한다. 경로를 `/api/posts`로 옮기면 `DELETE /api/comments/{id}`가 깨진다.
  **`@RequestMapping("/api")`를 유지하거나 댓글용 컨트롤러를 따로 둔다.**
- `AdminController`의 `DELETE /api/admin/comments/{id}`도 같은 문제를 갖는다.
- 작성자 본인 또는 ADMIN만 수정·삭제 가능한 규칙이 Service로 올라간다.

**스모크:** 글 작성 → 댓글 작성 → 목록 페이징(`?page=0&size=20`) → 관리자 삭제.

---

## Task 8: `search/` `weather/` `exchange/`

작은 것 셋을 한 Task에서 처리한다.

**Files:**
- Create: `search/SearchService.java` · `weather/WeatherController.java` ·
  `weather/WeatherService.java` · `exchange/ExchangeController.java` ·
  `exchange/ExchangeService.java`
- Move: `controller/SearchController.java` → `search/` ·
  `client/OpenMeteoClient.java` → `weather/` · `client/FrankfurterClient.java` → `exchange/`
- Delete: `controller/ProxyController.java` (weather/exchange로 분해)

**주의점:**
- `ProxyController`의 `PREF_CENTER` 상수(도도부현 8개 중심 좌표)는 `WeatherService`로
  간다. `/api/weather`·`/api/fx` **경로는 그대로다.**
- `SearchService`는 destination·festival·course **Service** 셋에 의존한다.
  전체 조회 후 메모리 필터링하는 현재 동작은 **그대로 둔다** (비효율이지만 동작 보존 우선).

**스모크:**
```bash
curl -s "localhost:8080/api/search?q=%EC%98%A8%EC%B2%9C"   # 기준선: dest=2 fest=0 course=0
curl -s "localhost:8080/api/weather?prefecture=%EA%B5%90%ED%86%A0%EB%B6%80"
curl -s localhost:8080/api/fx
```

---

## Task 9: `collector/` `system/`

가장 크다. `AdminController`에 남은 전부를 처리하고 그 파일을 삭제한다.

**Files:**
- Create: `collector/CollectorAdminController.java` · `collector/CollectorService.java` ·
  `collector/CollectorDtos.java` (record 7개: `CollectorRunRequest`·`CollectorRunStatus`·
  `BulkRun`·`BulkRunSummary`·`FailureEntry`·`CollectorRunDetail`·`BulkRunDetail`)
- Create: `system/SystemAdminController.java` · `system/SystemService.java`
- Move: `collector/` 기존 4종은 그대로 · `client/{WikipediaClient,NominatimClient,FetchResult}.java`
  → `collector/` · `repository/{CollectorRunRepository,BulkRunRepository}.java` → `collector/` ·
  `tools/BackfillCoordsRunner.java` → `collector/`
- Delete: `controller/AdminController.java` · `dto/Dtos.java` · `client/` · `tools/` ·
  `repository/` · `controller/` (모두 비게 된다)

**주의점:**
- `AdminController`의 `parseFailures`·`parseAbortReason` private 메서드는
  `CollectorService`로 간다. `ObjectMapper` 의존도 함께 옮긴다.
- 벌크 실행/재시도 로직(149·199·217행)이 이 도메인의 진짜 비즈니스 규칙이다.
  컨트롤러에 남기지 말고 Service로 올린다.
- `system/`은 stats(92행)·collection-matrix(105행)·cache(429·448행)를 담당한다.
  stats는 destination·festival·course·post·user Service를 모두 참조하는
  **교차 도메인 읽기**다. 이건 불가피하다 — 관리자 대시보드의 성격상 그렇다.
- 이 Task가 끝나면 `controller/`·`repository/`·`dto/`·`client/`·`tools/`·`service/`
  패키지가 전부 사라진다. 빈 디렉터리를 지운다.

**스모크:** 로그인 후 `/admin` 화면 전체를 브라우저에서 돌아본다 — 대시보드,
수집 현황, 벌크 진행, 콘텐츠 관리, 사용자, 감사 로그, 시스템. 그리고 실제로
수집을 한 번 돌린다:

```bash
curl -s -b /tmp/c.txt -X POST localhost:8080/api/admin/collector/run \
  -H "Content-Type: application/json; charset=utf-8" \
  --data-binary '{"type":"destination","prefecture":"나라현"}'   # JSON 본문은 charset=utf-8 헤더가 있어 한글 리터럴이 통한다 (URL 파라미터와 달리)
```

---

## Task 10: 마무리

- [ ] **Step 1: 최종 구조 확인**

```bash
find backend/src/main/java -name "*.java" | sed 's|backend/src/main/java/com/japantravel/||' | sort
```

기대: 목표 구조와 일치. `controller/`·`repository/`·`dto/`·`service/`·`client/`·
`tools/`·`security/`·`config/` 경로가 하나도 남아 있지 않다.

- [ ] **Step 2: 불변식 전수 검사**

```bash
for d in destination festival course post account favorite review history; do
  echo "--- $d"
  grep -rln "${d^}Repository" backend/src/main/java --include=*.java
done
```

기대: 각 도메인마다 자기 패키지 안 파일 2개(Repository 자신 + Service)만 나온다.

- [ ] **Step 3: 프론트 무변경 최종 확인**

```bash
git diff --stat main -- frontend/
```

기대: 출력 없음.

- [ ] **Step 4: 아키텍처 문서 갱신**

`docs/` 아래에 도메인 구조와 의존 방향을 정리한다. 발표 자료의 근거가 된다.
설계 문서에 기록해 둔 **미사용 테이블 3개**(`achievements`·`collections`·
`collection_items`)도 여기에 옮겨 적는다.

- [ ] **Step 5: 최종 커밋**
