---
name: spring-domain-skeleton
description: Japan Travel 백엔드에서 도메인 하나를 만드는 전체 흐름(설계 재정리 → 계약 결정 → 스펙 검수 → 스키마·스켈레톤 → 계층별 채우기 → 스모크)을 게이트마다 멈추며 진행하고, 그 안에서 5계층 스켈레톤(controller/service/repository/entity/dto)을 생성한다. "destination 도메인 만들어줘", "festival 도메인 세팅해줘", "도메인 뼈대 만들어", "Task N 시작하자", "course 시작", "create post domain", "spring domain skeleton", "add domain package", 이미 만든 도메인의 "사후 검수" 같은 요청에 사용할 것. 이 프로젝트에서 새 도메인 패키지를 추가하거나 도메인 구현을 시작·검수하려는 맥락이면 항상 이 스킬을 쓴다.
---

# 도메인 만들기 (Japan Travel)

도메인 하나를 **게이트가 있는 6단계**로 만든다. 스켈레톤 생성은 그중 4단계다.
각 단계 끝에서 **멈추고 사용자의 승인을 받은 뒤** 다음 단계로 간다.

이 흐름이 지키려는 것은 속도가 아니라 **이해**다 (D-006 · D-007 · D-028). 코드는 Claude 가
쓰지만, 발표에서 "왜 이렇게 했냐" 에 답할 사람은 사용자다. 대화 왕복이 늘어나는 것은
D-028 이 일부러 받아들인 대가다 — **줄이려고 하지 않는다.**

## 전체 흐름과 게이트

| 단계 | 하는 일 | 끝에서 멈추고 받을 것 |
|---|---|---|
| **0. 설계 재정리** | 아래 "0단계" 참조. 기존 설계 · 결정 · 옛 동작을 정리하고 **재검토 후보**를 제시 | 무엇을 유지하고 무엇을 다시 정할지 |
| **1. 계약 결정** | 정할 것마다 선택지 · 트레이드오프 · 추천을 제시 (`AskUserQuestion`) | 각 선택. 되돌리기 어려운 것은 그 자리에서 `DECISIONS.md` 에 기록 |
| **2. 스펙 작성** | `docs/superpowers/specs/<날짜>-<domain>-api-design.md` | **사용자 검수.** 승인 전에 스키마 · 코드를 쓰지 않는다 |
| **3. 스키마** | `schema.sql` 에 테이블 추가 (스펙대로) | 추가한 DDL 확인 |
| **4. 스켈레톤** | 아래 "스켈레톤 절차" 1 ~ 6 | 트리 보고 확인. **채우지 않은 채로 멈춘다** |
| **5. 계층별 채우기** | `entity → repository → service → dto → controller` 를 **한 계층씩** | 계층마다 승인 |
| **6. 스모크 · 문서** | 스펙의 검증 절차 실행, `API.md` · `SCHEMA.md` · `ERRORS.md` 반영 | 결과 확인, 커밋 여부 |

### 게이트 규칙

- **한 턴에 한 단계.** 단계를 끝내면 결과를 보여 주고 멈춘다. "속도를 위해 합친다" 는
  판단을 Claude 가 스스로 하지 않는다. 사용자가 **명시적으로** 합치라고 한 경우에만 합친다.
- **Claude 가 기본값으로 정한 것은 "제안" 이다.** "바꾸고 싶으면 말해 달라" 로 넘기고 진행하지
  않는다. 목록으로 보여 주고 승인을 받는다. 스펙에는 승인된 것만 "확정" 으로 적는다.
- **공용 파일 변경**(`ErrorCode` · `ApiResponse` · `SecurityConfig` 등)은 5단계의 해당 계층에서
  따로 짚는다. 새 에러 코드는 1단계에서 이미 계약으로 승인받았어야 한다.
- 사용자가 설계 이유를 되물었다면(예: "왜 2개로 나눴나") 0 ~ 1단계에서 설명이 빠졌다는 신호다.
  답한 뒤 같은 종류의 빈틈이 더 없는지 점검한다.

### 0단계 — 설계 재정리

설계가 이미 어느 정도 있어도 **다시 정리하는 과정을 건너뛰지 않는다.** 설계 문서는 쓰인 시점의
전제를 담고 있고, 그 전제는 이후 결정으로 바뀌었을 수 있다.

1. **관련 결정을 모은다** — `docs/DECISIONS.md` 에서 이 도메인 · 대상 테이블 · 공통 규칙에 걸린 항목.
   상태(`확정` · `보류` · `번복됨`)와 근거를 함께 본다.
2. **설계 지도를 본다** — `docs/superpowers/specs/2026-09-17-greenfield-rebuild-design.md` 의 스키마 절과
   Task 표. 그 뒤의 결정(예: D-022 의 테이블 분리)으로 달라진 부분을 표시한다.
3. **옛 동작을 확인한다** — `git show pre-greenfield:<경로>` 로 옛 컨트롤러 · 리포지토리 · SQL 을,
   `frontend/src/` 에서 그 API 를 부르는 곳을 본다. **이름이 아니라 실제 동작**을 적는다
   (예: "방문기록" 이 실제로는 상세 페이지 열람 시 자동 기록이었다).
4. **재검토 후보를 뽑는다** — 근거가 **"옛 동작 유지"** 인 결정은 D-008 로 전제가 풀렸으므로 후보다.
   이후 결정과 부딪히는 설계 지도의 항목도 후보다.
5. **사용자에게 보여 준다** — 짧은 요약(무엇을 하는 도메인인가 · 옛 동작 · 걸린 결정 · 달라진 점)과
   재검토 후보 목록. 각 후보에 "유지 / 다시 정함" 을 받는다. **여기서 멈춘다.**

### 5단계 — 계층별 채우기

계층마다 다음을 한 묶음으로 내놓고 멈춘다 (D-007).

1. **가이드** — 이 계층이 무슨 일을 하는가 · 왜 이렇게 하는가 · 무엇을 쓸 것인가 · 놓치기 쉬운 것
2. **작성** — 그 계층의 파일만 채운다
3. **확인** — 컴파일 (`./gradlew compileJava -q`)
4. **설명** — 쓴 코드의 선택 하나하나에 이유. 스펙에 없던 판단이 생겼으면 **제안으로 표시**하고 승인받는다

### 사후 검수 모드 — 이미 만든 도메인

규칙을 건너뛰고 만든 도메인을 다시 볼 때. 코드를 새로 만들지 않고 **같은 게이트로 되짚는다.**

- **0단계** 그대로 수행.
- **1 · 2단계** — 스펙의 각 항목을 "사용자가 정함 / Claude 가 정함(미승인)" 으로 나눠 보여 주고,
  미승인 항목에 승인 또는 변경을 받는다. 스펙을 갱신한다.
- **3 · 4단계** — 생략 (이미 있음).
- **5단계** — 계층마다 기존 코드를 **가이드 + 설명** 형식으로 보여 주고 멈춘다. 바꿀 것이 나오면
  그 계층에서 고친다.
- **6단계** — 바뀐 것이 있으면 스모크를 다시 돌린다.

## 이 프로젝트의 전제

| 항목 | 값 | 근거 |
|---|---|---|
| 빌드 | Gradle (Groovy DSL) · Spring Boot 3.3.4 · Java 17 | D-002 · D-026 |
| DB | **MySQL 8** (공식 dialect, `database-platform` 을 적지 않는다) | D-026 |
| 영속성 | Spring Data JPA + Hibernate 6.5 · `open-in-view: false` | D-002 |
| Lombok | **쓴다.** 엔티티는 `@Getter` + `@NoArgsConstructor(PROTECTED)` 까지만 | D-027 |
| 패키지 | `com.japantravel.<domain>.<layer>` — **`_repo` 접두사는 없다** | D-029 |
| 스키마의 주인 | `backend/src/main/resources/schema.sql` — `ddl-auto: none` | D-020 |
| 계층 | 5개. `dto` 는 선택이 아니라 필수다 | |

**엔티티에 `@Data` · `@ToString` · `@EqualsAndHashCode` 를 붙이지 않는다.** JPA
엔티티에서 사고를 낸다 — `toString()` 이 LAZY 연관을 건드려 세션 밖에서 예외를
내거나 양방향 연관에서 무한 재귀에 빠지고, `hashCode` 는 id 가 null 인 저장 전
시점에 동작이 이상해진다. (D-027)

**스키마가 엔티티에 맞추지 않는다.** 테이블·컬럼 이름을 추측하지 말고 `schema.sql`
을 읽는다.

## 먼저 볼 것 — 이미 완성된 두 도메인

`destination` 과 `festival` 이 이 프로젝트의 기준 형태다. 새 도메인을 만들기 전에
**둘 중 하나를 열어 실제 코드를 확인한다.** 이 스킬의 템플릿보다 실제 코드가 우선이다.

```
backend/src/main/java/com/japantravel/destination/   조회 2개, 필터 1개
backend/src/main/java/com/japantravel/festival/      조회 2개, 필터 2개
```

설계 문서는 `docs/superpowers/specs/2026-09-22-destination-query-api-design.md` 와
`docs/superpowers/specs/2026-09-22-festival-query-api-design.md`.

## 스켈레톤 절차 (4단계)

2단계 스펙이 승인되고 3단계 스키마가 들어간 뒤에 수행한다.

### 1. 기본 패키지 확인

```bash
grep -rl "@SpringBootApplication" --include=*.java backend/src/main/java
```

찾은 파일의 `package` 선언이 기본 패키지이자 도메인 루트다 (`com.japantravel`).
**`_repo` 를 찾지 않는다.** D-029 로 폐기됐다.

### 2. 도메인 이름과 테이블 확정

도메인 이름은 소문자 단수 영문, 클래스명은 PascalCase, URL 은 복수형.

| 용도 | 형태 | 예 |
|---|---|---|
| 패키지명 | 소문자 단수 | `destination`, `post` |
| 클래스명 | PascalCase | `Destination`, `Post` |
| URL 경로 | 복수형 | `/api/destinations` |

**테이블 이름은 복수형 규칙으로 만들지 말고 `schema.sql` 에서 확인한다.**

```bash
grep -n "CREATE TABLE" backend/src/main/resources/schema.sql
```

**어떤 테이블이 있는지는 매번 위 grep 으로 확인한다.** 이 문서에 현재 목록을 적지
않는다 — 도메인마다 늘어나므로 적는 순간 낡는다. 테이블이 아직 없는 도메인은
**테이블을 먼저 추가해야 한다.** 스키마는 도메인별로 늘려가는 것이 방침이다 (D-020).

도메인별 테이블 이름 규칙:

| 도메인 | 테이블 | 비고 |
|---|---|---|
| `destination` · `festival` · `user` | `destinations` · `festivals` · `users` | 도메인 하나에 테이블 하나 |
| `favorite` · `review` · `history` | `<도메인>_destinations` · `<도메인>_festivals` | **대상별로 2개** (D-022 · D-036). 5단계의 변형 규칙을 따른다 |
| `course` | `courses` + `course_stops` | 테이블 2개. `course_tags` 는 없다 (D-024) |
| `post` | `posts` + `post_comments` | 테이블 2개 |
| `search` | 없음 | **엔티티·리포지토리를 만들지 않는다** |

**대응하는 테이블이 없으면** `entity` 와 `repository` 를 만들지 않고
`controller` · `service` · `dto` 셋만 만든다. 생성 전에 사용자에게 알린다.

테이블을 못 찾으면 **만들지 말고 사용자에게 묻는다.** 임의로 정하지 않는다.

### 3. 관리자 컨트롤러 — 기본적으로 만들지 않는다

**관리자 기능은 D-009 로 1차 범위 밖이다.** 진행 순서상 9단계이며, 그 전에는
`<Domain>AdminController` 를 만들지 않는다. 사용자가 명시적으로 요청한 경우에만 만든다.

### 4. 충돌 검사 — 생성 전에 반드시 수행

**(a) 디렉터리 충돌**

```bash
ls -d backend/src/main/java/com/japantravel/<domain> 2>/dev/null
```

**(b) 빈 이름 충돌**

Spring 은 빈 이름을 **클래스 단순명**에서 만든다. 패키지가 달라도 같은 단순명이면
`ConflictingBeanDefinitionException` 으로 **앱이 아예 뜨지 않는다.**

```bash
grep -rnE "class <Domain>(Controller|Service)\b|interface <Domain>\w*Repository\b" \
  backend/src/main/java --include=*.java
```

`<Domain>\w*Repository` 는 대상별 리포지토리(`<Domain>DestinationRepository` 등)까지 잡는다.

**하나라도 충돌하면 아무 파일도 만들지 않고 중단한다.** 부분 생성은 하지 않는다.
여러 도메인을 한 번에 요청받았을 때도 하나라도 충돌하면 전체를 중단한다.

### 5. 파일 생성

```
com/japantravel/<domain>/
├── controller/<Domain>Controller.java
├── service/<Domain>Service.java
├── repository/<Domain>Repository.java   (테이블이 있는 경우만)
├── entity/<Domain>.java                 (테이블이 있는 경우만)
└── dto/<Domain>Response.java
```

**변형 — 활동 도메인(`favorite` · `review` · `history`)** 은 대상별로 테이블이 2개이므로
`entity` 와 `repository` 를 **대상마다 하나씩** 만든다 (D-036). 컨트롤러·서비스는 도메인당
하나다. 기준 형태는 `favorite` 패키지다.

```
com/japantravel/<domain>/
├── controller/<Domain>Controller.java
├── service/<Domain>Service.java
├── repository/<Domain>DestinationRepository.java · <Domain>FestivalRepository.java
├── entity/<Domain>Destination.java · <Domain>Festival.java     @Table(name = "<domain>_destinations") 등
└── dto/<Domain>…Response.java                                  모양은 설계 스펙이 정한다
```

**DTO 는 record 하나당 파일 하나다.** 옛 `Dtos.java` 한 파일에 몰아넣는 방식은
쓰지 않는다. 요청 DTO 가 필요해지면 `<Domain>CreateRequest.java` 처럼 따로 만든다.

테스트 클래스는 만들지 않는다.

### 6. 결과 보고

생성된 파일을 트리로 출력하고, 한 줄로 알린다: 도메인 루트 · 매핑한 테이블 ·
엔티티 생성 여부.

엔티티를 만들었다면 **"필드는 id 만 있다. `schema.sql` 을 보고 채워야 한다"** 를
명시한다.

컴파일을 확인하고 **여기서 멈춘다.** 같은 턴에 채우기(5단계)로 넘어가지 않는다.

---

## 템플릿

`{{domain}}`, `{{Domain}}`, `{{domains}}`(URL 복수형), `{{table}}`(schema.sql 에서
확인한 실제 테이블명)을 치환해서 쓴다.

### entity

```java
package com.japantravel.{{domain}}.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 스키마의 주인은 schema.sql 이다. 필드는 schema.sql 의 컬럼을 보고 채운다.
// 컬럼명이 필드명과 다르면 @Column(name = "...") 을 붙인다 (예: image_path).
// 다른 테이블을 참조하면 @ManyToOne(fetch = FetchType.LAZY) + @JoinColumn 을 쓴다.
@Entity
@Table(name = "{{table}}")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class {{Domain}} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

`@Table(name = ...)` 을 **반드시 붙인다.** 없으면 Hibernate 가 클래스명을 그대로
테이블명으로 쓰는데, 이 프로젝트의 테이블은 대부분 복수형이라 맞지 않는다.

### repository

```java
package com.japantravel.{{domain}}.repository;

import com.japantravel.{{domain}}.entity.{{Domain}};
import org.springframework.data.jpa.repository.JpaRepository;

public interface {{Domain}}Repository extends JpaRepository<{{Domain}}, Long> {
}
```

**연관 엔티티를 응답에 쓴다면 조회 메서드마다 `@EntityGraph` 를 단다.** 없으면
N+1 이 나고, `open-in-view: false` 라 컨트롤러까지 가면 터진다.

```java
@EntityGraph(attributePaths = "prefecture")
Optional<{{Domain}}> findById(Long id);
```

**필터 조합마다 메서드를 따로 둔다.** 쓰지 않는 조건에 `null` 을 넘기는 방식은
쓸 수 없다 — `WHERE month = NULL` 은 SQL 에서 unknown 이라 항상 0건이 된다.
필터가 3개를 넘어 조합이 8가지가 되면 그때 `@Query` 로 바꾼다.

### dto

```java
package com.japantravel.{{domain}}.dto;

import com.japantravel.{{domain}}.entity.{{Domain}};

// API 계약이다. 엔티티를 컨트롤러 밖으로 내보내지 않기 위해 존재한다.
// 목록과 상세가 같이 쓴다 — 나눠서 얻는 것이 분명할 때만 나눈다.
public record {{Domain}}Response(
        Long id
        // schema.sql 과 승인된 설계를 보고 채운다
) {
    public static {{Domain}}Response from({{Domain}} entity) {
        return new {{Domain}}Response(entity.getId());
    }
}
```

**참조 테이블은 이름 문자열로 평탄화한다.** `prefecture` 를 중첩 객체가 아니라
`String prefecture` 로 내보낸다. 목록 필터의 입력과 응답 필드가 같은 값 공간이 되어
상세 응답을 그대로 목록 필터로 되먹일 수 있다.

**변환은 DTO 안의 정적 팩토리 `from()` 에 둔다.** 별도 Mapper 클래스를 만들지 않는다.

### service

```java
package com.japantravel.{{domain}}.service;

import com.japantravel.{{domain}}.repository.{{Domain}}Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class {{Domain}}Service {

    private final {{Domain}}Repository {{domain}}Repository;
}
```

**Service 는 엔티티가 아니라 DTO 를 반환한다.** `open-in-view: false` 이므로
컨트롤러에서 LAZY 연관을 건드리면 `LazyInitializationException` 이 난다. 변환을
트랜잭션 안에서 끝내면 이 문제가 생기지 않는다.

쓰기 메서드에는 개별적으로 `@Transactional` 을 붙인다. 클래스 기본값은 읽기 전용이다.

### controller

```java
package com.japantravel.{{domain}}.controller;

import com.japantravel.{{domain}}.service.{{Domain}}Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/{{domains}}")
@RequiredArgsConstructor
public class {{Domain}}Controller {

    private final {{Domain}}Service {{domain}}Service;
}
```

Service 를 호출하고 반환값을 **`ApiResponse.ok(...)` 로 감싸** 돌려주는 얇은 계층이다
([D-035](../../../docs/DECISIONS.md)). 엔티티를 보지 않는다. 메서드를 추가할 때의 모양:

```java
@GetMapping("/{id}")
public ApiResponse<{{Domain}}Response> detail(@PathVariable Long id) {
    return ApiResponse.ok({{domain}}Service.findById(id));
}
```

- 반환 타입은 `ApiResponse<DTO>` 또는 `ApiResponse<List<DTO>>`. `ResponseEntity` 는 쓰지 않는다.
- 201 은 `@ResponseStatus(HttpStatus.CREATED)` 로 준다.
- 돌려줄 데이터가 없으면(`DELETE` 등) 200 + `data: null`. 204 를 쓰지 않는다. 인자 없는
  `ApiResponse.ok()` 가 아직 없으면 그때 `common/web/ApiResponse` 에 추가한다.

---

## 에러 규칙 — 도메인마다 다르게 하지 않는다

`com.japantravel.common.web.ApiExceptionHandler` 가 전부 번역한다. 컨트롤러에서
`try/catch` 를 쓰지 않는다. 서비스는 `throw new ApiException(ErrorCode.XXX)` 하나로 던진다
([D-034](../../../docs/DECISIONS.md)). 상태별 예외 클래스는 없다.

| 상황 | 상태 | 던지는 것 |
|---|---|---|
| id 로 찾았는데 없음 | 404 | `ApiException(ErrorCode.<DOMAIN>_NOT_FOUND)` |
| 참조 값이 존재하지 않음 (없는 현 이름 등) | 404 | `ApiException(ErrorCode.PREFECTURE_NOT_FOUND)` 등 |
| 필터 결과가 0건 (대상은 실재함) | 200 `data: []` | 던지지 않는다 |
| 값이 허용 범위 밖 (`month=13`) | 400 | `ApiException(ErrorCode.INVALID_…)` — `IllegalArgumentException` 은 500 이 된다 |
| 타입이 맞지 않음 (`month=abc`) | 400 | Spring 이 던짐, 핸들러가 이미 처리 |
| 남의 것을 건드림 | 403 | `ApiException(ErrorCode.FORBIDDEN)` — 처음 쓸 때 enum 에 추가 |
| 중복 | 409 | `ApiException(ErrorCode.<무엇>_TAKEN)` 등 |

**새 에러가 필요하면** `common/error/ErrorCode` 에 `(HttpStatus, 고정 문구)` 로 추가하고
`docs/ERRORS.md` 에 "어디서 · 무엇 때문에" 를 적는다. 쓰는 곳 없는 코드는 미리 넣지 않는다.
메시지는 고정 문구만 쓴다 — 던질 때 덮어쓰지 않는다.

본문은 봉투 `{"success":false,"data":null,"error":{"code","message"}}` 형식이다.

---

## 4단계(스켈레톤)에서 하지 않는 것

아래는 5단계에서 계층별로, 승인을 받으며 한다.

- 비즈니스 메서드 구현 — 뼈대만 만든다
- `id` 외의 엔티티 필드나 연관관계 추가
- DTO 필드 작성
- 테스트 클래스 생성
- `application.yml` 수정, 빌드 파일에 의존성 추가
- **기존 파일 수정** — 새 파일 생성만 한다. 빈 이름 충돌이 나도 옛 클래스를
  건드리지 않고 보고만 한다

**`schema.sql` 은 4단계에서 건드리지 않는다.** 테이블은 3단계에서 스펙대로 이미 들어가
있어야 한다. 없으면 3단계로 돌아가고, 스펙에 없는 테이블이면 **만들지 말고 사용자에게 묻는다.**
