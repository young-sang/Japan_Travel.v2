---
name: spring-domain-skeleton
description: Japan Travel 백엔드에 도메인 단위 5계층 스켈레톤(controller/service/repository/entity/dtos)을 생성한다. "destination 도메인 만들어줘", "festival 도메인 세팅해줘", "도메인 뼈대 만들어", "create post domain", "spring domain skeleton", "add domain package" 같은 요청에 사용할 것. 이 프로젝트에서 새 도메인 패키지를 추가하려는 맥락이면 항상 이 스킬을 쓴다.
---

# 도메인 스켈레톤 생성 (Japan Travel)

도메인 이름을 받아 `controller` · `service` · `repository` · `entity` · `dtos`
다섯 계층의 빈 뼈대를 만든다. **메서드와 필드는 만들지 않는다.** 내부 구현은 사용자가
직접 설계한다. 이 스킬은 뼈대만 놓는다.

## 이 프로젝트의 전제

| 항목 | 값 |
|---|---|
| 빌드 | Gradle (Groovy DSL) · Spring Boot 3.3.4 · Java 17 |
| 영속성 | Spring Data JPA + Hibernate 6.5 · SQLite (community dialect) |
| Lombok | **없다.** 생성자와 getter 를 직접 쓴다 |
| 스키마의 주인 | `backend/src/main/resources/schema.sql` — `ddl-auto: none` |
| 계층 | 5개. `dtos` 는 선택이 아니라 필수다 |

**스키마가 이미 존재하고 데이터가 들어 있다.** 따라서 엔티티가 테이블에 맞추는 것이지
테이블이 엔티티에 맞추지 않는다. 테이블·컬럼 이름을 추측하지 말고 `schema.sql` 을 읽는다.

## 절차

아래 순서를 그대로 따른다. 순서를 건너뛰지 않는다.

### 1. 기본 패키지와 도메인 루트 탐지

```bash
grep -rl "@SpringBootApplication" --include=*.java backend/src/main/java
```

찾은 파일의 `package` 선언이 기본 패키지다. 예: `com.japantravel`.

그다음 **도메인 루트**를 정한다.

```bash
ls -d backend/src/main/java/com/japantravel/_repo 2>/dev/null
```

- `_repo` 가 **있으면** 도메인 루트는 `<기본패키지>._repo` 다. (전환 작업 중)
- `_repo` 가 **없으면** 도메인 루트는 `<기본패키지>` 다. (전환 완료 후)

추측하지 않는다. 기본 패키지를 못 찾으면 사용자에게 묻는다.

### 2. 도메인 이름과 테이블 확정

도메인 이름은 소문자 단수 영문이다. 클래스명은 PascalCase.

| 용도 | 형태 | 예 |
|---|---|---|
| 패키지명 | 소문자 | `destination`, `post` |
| 클래스명 | PascalCase | `Destination`, `Post` |
| URL 경로 | 복수형 | `/api/destinations` |

**테이블 이름은 복수형 규칙으로 만들지 말고 `schema.sql` 에서 확인한다.**

```bash
grep -n "CREATE TABLE" backend/src/main/resources/schema.sql
```

이 프로젝트의 테이블은 규칙이 일정하지 않다.

| 도메인 | 테이블 | 비고 |
|---|---|---|
| `destination` | `destinations` | |
| `festival` | `festivals` | |
| `course` | `courses` | |
| `post` | `posts` + `post_comments` | 테이블 2개 |
| `user` | `users` | |
| `favorite` | `favorites` | |
| `review` | `reviews` | |
| `history` | `history` | **단수** |
| `audit` | `audit_log` | **이름이 다름** |
| `collector` | `collector_runs` + `bulk_runs` | 테이블 2개 |
| `search` · `weather` · `exchange` · `system` | 없음 | **엔티티·리포지토리를 만들지 않는다** |

**대응하는 테이블이 없으면** `entity` 와 `repository` 를 만들지 않고
`controller` · `service` · `dtos` 셋만 만든다. 생성 전에 사용자에게 알린다.

테이블을 못 찾으면 **만들지 말고 사용자에게 묻는다.** 임의로 정하지 않는다.

### 3. 관리자 컨트롤러 여부 확인

`/api/admin/<domains>` 엔드포인트가 있는 도메인은 `<Domain>AdminController` 도 만든다.

```bash
grep -rn "api/admin/<domains>" frontend/src
```

프론트가 호출하지 않으면 만들지 않는다. 애매하면 사용자에게 묻는다.

### 4. 충돌 검사 — 생성 전에 반드시 수행

**(a) 디렉터리 충돌**

```bash
ls -d <sourceRoot>/<도메인루트경로>/<domain> 2>/dev/null
```

**(b) 빈 이름 충돌 — 이 프로젝트에서 실제로 발생하는 문제다**

Spring 은 빈 이름을 **클래스 단순명**에서 만든다. 패키지가 달라도 같은 단순명이면
`ConflictingBeanDefinitionException` 으로 **앱이 아예 뜨지 않는다.**

```bash
grep -rn "class <Domain>Controller\|class <Domain>Repository\|interface <Domain>Repository" \
  backend/src/main/java --include=*.java
```

옛 패키지(`com.japantravel.controller` · `com.japantravel.repository`)에 같은 이름이
있으면 **생성하지 말고 보고한다.** 해결책은 옛 클래스를 `Legacy<Name>` 으로 이름을
바꾸는 것이며, **그 작업은 사용자가 한다.** 이 스킬은 기존 파일을 건드리지 않는다.

**하나라도 충돌하면 아무 파일도 만들지 않고 중단한다.** 부분 생성은 하지 않는다.
여러 도메인을 한 번에 요청받았을 때도 하나라도 충돌하면 전체를 중단한다.

### 5. 파일 생성

```
<도메인루트>/<domain>/
├── controller/<Domain>Controller.java
├── controller/<Domain>AdminController.java   (3단계에서 필요하다고 판단한 경우만)
├── service/<Domain>Service.java
├── repository/<Domain>Repository.java        (테이블이 있는 경우만)
├── entity/<Domain>.java                      (테이블이 있는 경우만)
└── dtos/<Domain>Dtos.java
```

테스트 클래스는 만들지 않는다.

### 6. 결과 보고

생성된 파일을 위 트리 형태로 출력한다. 함께 한 줄로 알린다:
기본 패키지 · 도메인 루트 · 매핑한 테이블 · 관리자 컨트롤러 생성 여부.

엔티티를 만들었다면 **"필드는 비어 있다. `schema.sql` 을 보고 직접 채워야 한다"** 를
명시한다.

---

## 템플릿

`{{basePackage}}`(도메인 루트), `{{domain}}`, `{{Domain}}`, `{{domains}}`(URL 복수형),
`{{table}}`(schema.sql 에서 확인한 실제 테이블명)을 치환해서 쓴다.

Lombok 을 쓰지 않는다. 생성자 주입을 직접 쓴다.

### entity/{{Domain}}.java

```java
package {{basePackage}}.{{domain}}.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 스키마의 주인은 schema.sql 이다. 필드는 schema.sql 의 컬럼을 보고 직접 채운다.
// 컬럼명이 필드명과 다르면 @Column(name = "...") 을 붙인다. (예: image_path)
@Entity
@Table(name = "{{table}}")
public class {{Domain}} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    protected {{Domain}}() {
    }

    public Long getId() {
        return id;
    }
}
```

`@Table(name = ...)` 을 **반드시 붙인다.** 없으면 Hibernate 가 클래스명을 그대로
테이블명으로 쓰는데, 이 프로젝트의 테이블은 대부분 복수형이라 맞지 않는다.

### repository/{{Domain}}Repository.java

```java
package {{basePackage}}.{{domain}}.repository;

import {{basePackage}}.{{domain}}.entity.{{Domain}};
import org.springframework.data.jpa.repository.JpaRepository;

public interface {{Domain}}Repository extends JpaRepository<{{Domain}}, Long> {
}
```

### service/{{Domain}}Service.java

```java
package {{basePackage}}.{{domain}}.service;

import {{basePackage}}.{{domain}}.repository.{{Domain}}Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class {{Domain}}Service {

    private final {{Domain}}Repository {{domain}}Repository;

    public {{Domain}}Service({{Domain}}Repository {{domain}}Repository) {
        this.{{domain}}Repository = {{domain}}Repository;
    }
}
```

쓰기 메서드에는 개별적으로 `@Transactional` 을 붙인다. 클래스 기본값은 읽기 전용이다.

### controller/{{Domain}}Controller.java

```java
package {{basePackage}}.{{domain}}.controller;

import {{basePackage}}.{{domain}}.service.{{Domain}}Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/{{domains}}")
public class {{Domain}}Controller {

    private final {{Domain}}Service {{domain}}Service;

    public {{Domain}}Controller({{Domain}}Service {{domain}}Service) {
        this.{{domain}}Service = {{domain}}Service;
    }
}
```

### controller/{{Domain}}AdminController.java

```java
package {{basePackage}}.{{domain}}.controller;

import {{basePackage}}.{{domain}}.service.{{Domain}}Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 일반 API 와 같은 Service 를 공유한다. 규칙이 두 군데로 갈라지지 않게 한다.
@RestController
@RequestMapping("/api/admin/{{domains}}")
public class {{Domain}}AdminController {

    private final {{Domain}}Service {{domain}}Service;

    public {{Domain}}AdminController({{Domain}}Service {{domain}}Service) {
        this.{{domain}}Service = {{domain}}Service;
    }
}
```

### dtos/{{Domain}}Dtos.java

```java
package {{basePackage}}.{{domain}}.dtos;

// API 계약이다. 엔티티를 컨트롤러 밖으로 내보내지 않기 위해 존재한다.
// 필드명은 프론트가 읽는 JSON 키와 정확히 일치해야 한다.
public final class {{Domain}}Dtos {

    private {{Domain}}Dtos() {
    }
}
```

`record` 는 사용자가 이 클래스 안에 직접 추가한다.

---

## 하지 않는 것

- CRUD 메서드 구현 — 뼈대만 만든다
- `id` 외의 엔티티 필드나 연관관계 추가
- DTO record 작성
- 테스트 클래스 생성
- `application.yml` · `schema.sql` 등 설정·스키마 파일 수정
- 빌드 파일에 의존성 추가
- **기존 파일 수정** — 새 파일 생성만 한다. 빈 이름 충돌이 나도 옛 클래스를
  건드리지 않고 보고만 한다
