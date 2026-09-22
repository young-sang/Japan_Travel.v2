---
name: spring-domain-skeleton
description: Japan Travel 백엔드에 도메인 단위 5계층 스켈레톤(controller/service/repository/entity/dto)을 생성한다. "destination 도메인 만들어줘", "festival 도메인 세팅해줘", "도메인 뼈대 만들어", "create post domain", "spring domain skeleton", "add domain package" 같은 요청에 사용할 것. 이 프로젝트에서 새 도메인 패키지를 추가하려는 맥락이면 항상 이 스킬을 쓴다.
---

# 도메인 스켈레톤 생성 (Japan Travel)

도메인 이름을 받아 `controller` · `service` · `repository` · `entity` · `dto`
다섯 계층의 뼈대를 만든다. **비즈니스 메서드와 엔티티 필드는 만들지 않는다.**
내부는 승인된 설계에 따라 채운다. 이 스킬은 뼈대만 놓는다.

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

## 절차

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

**현재 schema.sql 에 있는 테이블은 3개뿐이다** — `prefectures` · `destinations` ·
`festivals`. 나머지 도메인은 **테이블을 먼저 추가해야 한다.** 스키마는 도메인별로
늘려가는 것이 방침이다 (D-020).

옛 스키마 기준의 이름 규칙 (그린필드에서 재확인 필요):

| 도메인 | 테이블 | 비고 |
|---|---|---|
| `destination` | `destinations` | 있음 |
| `festival` | `festivals` | 있음 |
| `course` | `courses` | 미생성 |
| `post` | `posts` + `post_comments` | 테이블 2개 |
| `user` | `users` | 인증은 Task 2 (D-019) |
| `favorite` · `review` | 대상별로 분리 — `favorite_destinations` 등 | **D-022 의 대가** |
| `search` · `weather` · `exchange` | 없음 | **엔티티·리포지토리를 만들지 않는다** |

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
grep -rn "class <Domain>Controller\|class <Domain>Service\|interface <Domain>Repository" \
  backend/src/main/java --include=*.java
```

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

**DTO 는 record 하나당 파일 하나다.** 옛 `Dtos.java` 한 파일에 몰아넣는 방식은
쓰지 않는다. 요청 DTO 가 필요해지면 `<Domain>CreateRequest.java` 처럼 따로 만든다.

테스트 클래스는 만들지 않는다.

### 6. 결과 보고

생성된 파일을 트리로 출력하고, 한 줄로 알린다: 도메인 루트 · 매핑한 테이블 ·
엔티티 생성 여부.

엔티티를 만들었다면 **"필드는 id 만 있다. `schema.sql` 을 보고 채워야 한다"** 를
명시한다.

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

Service 를 호출하고 반환값을 그대로 돌려주는 얇은 계층이다. 엔티티를 보지 않는다.

---

## 에러 규칙 — 도메인마다 다르게 하지 않는다

`com.japantravel.common.web.ApiExceptionHandler` 가 전부 번역한다. 컨트롤러에서
`try/catch` 를 쓰지 않는다.

| 상황 | 상태 | 던지는 것 |
|---|---|---|
| id 로 찾았는데 없음 | 404 | `NotFoundException` |
| 참조 값이 존재하지 않음 (없는 현 이름 등) | 404 | `NotFoundException` |
| 필터 결과가 0건 (대상은 실재함) | 200 `[]` | 던지지 않는다 |
| 값이 허용 범위 밖 (`month=13`) | 400 | `IllegalArgumentException` |
| 타입이 맞지 않음 (`month=abc`) | 400 | Spring 이 던짐, 핸들러가 이미 처리 |
| 남의 것을 건드림 | 403 | `ForbiddenException` |
| 중복 | 409 | `ConflictException` |

본문은 `{"message": "..."}` 형식이다.

---

## 하지 않는 것

- 비즈니스 메서드 구현 — 뼈대만 만든다
- `id` 외의 엔티티 필드나 연관관계 추가
- DTO 필드 작성
- 테스트 클래스 생성
- `application.yml` 수정, 빌드 파일에 의존성 추가
- **기존 파일 수정** — 새 파일 생성만 한다. 빈 이름 충돌이 나도 옛 클래스를
  건드리지 않고 보고만 한다

**`schema.sql` 은 예외다.** 테이블이 없는 도메인이면 스키마 추가가 선행되어야
하는데, 이 스킬은 그것을 하지 않고 **사용자에게 알린다.**
