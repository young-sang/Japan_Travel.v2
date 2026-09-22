# destination 조회 API 설계

2026-09-22 · 확정 · greenfield 브랜치

`destination` 도메인에 service / controller / DTO 계층을 얹어 **조회 2개**를
완성한다. 엔티티와 레포지토리는 이미 있다.

---

## 1. 범위

| 넣은 것 | 뺀 것 |
|---|---|
| `GET /api/destinations` (현 필터) | 사용자 등록·수정 **제안** |
| `GET /api/destinations/{id}` | 관리자 **승인·반려** |
| | 관리자 CRUD, 이름 검색, 페이지네이션 |
| | 테스트 코드 |

**제안 → 승인을 뺀 이유.** 사용자가 폼으로 신규 등록·수정을 요청하면 관리자가
확인 후 반영하는 흐름을 원했으나, 이 기능은 *"누가 제안했는가"* 와 *"누가
관리자인가"* 를 알아야 성립한다. 둘 다 아직 없다 — 관리자 기능은
D-009 에서 1차 범위 밖으로, 인증·세션·권한은 D-019 에서 Task 2 로 미뤘다.
지금 만들면 제안자 컬럼을 담을 곳이 없어 스키마를 두 번 고치게 된다.
**인증(Task 2) 이후 별도 설계로 다룬다.**

**테스트를 뺀 이유.** 사용자 요청. 조회 2개뿐이라 `curl` 스모크로 확인한다.

---

## 2. 엔드포인트 계약

### GET /api/destinations

| 항목 | 값 |
|---|---|
| 쿼리 | `prefecture` (선택, **현 이름**) |
| 200 | `DestinationResponse[]` — `id DESC` 정렬 |
| 404 | `prefecture` 가 `prefectures` 에 없는 이름일 때 |

- `prefecture` 생략 시 전체 목록
- **현은 실재하는데 여행지가 0건이면 `200 []`.** 오타(`쿄토부`)와 빈
  결과(`교토부`, 아직 데이터 없음)를 구분한다. 데이터를 채우는 중에 멀쩡한
  현이 전부 404 로 보이는 것을 피하기 위해서다.

### GET /api/destinations/{id}

| 항목 | 값 |
|---|---|
| 200 | `DestinationResponse` |
| 404 | 해당 id 없음 |

### 에러 본문

기존 `ApiExceptionHandler` 형식을 그대로 쓴다. `NotFoundException` → `404`:

```json
{ "message": "여행지를 찾을 수 없습니다: 999" }
```

---

## 3. DTO

`destination/dto/DestinationResponse.java` — **record 하나로 목록·상세 공용.**

```java
public record DestinationResponse(
        Long id,
        String name,
        String prefecture,
        String description,
        Double lat,
        Double lng,
        String imagePath,
        LocalDateTime createdAt
) {
    public static DestinationResponse from(Destination d) { ... }
}
```

**목록/상세를 나누지 않은 이유.** 서비스에 지도가 있어 목록에도 `lat/lng` 가
필요하다. 좌표가 빠지지 않으면 목록과 상세의 차이는 `description` 하나뿐이라,
DTO 를 둘로 나눠도 얻는 게 거의 없고 매핑만 두 군데가 된다.

**`prefecture` 를 문자열로 평탄화한 이유.** 목록의 필터 입력과 응답 필드가 같은
값 공간이 되어, 상세 응답의 `prefecture` 를 그대로 목록 필터로 되먹일 수 있다.
중첩 객체(`{"id":26,"name":"교토부"}`)로 두면 소비자가 매번 꺼내 써야 한다.

**`createdAt`** 은 Jackson 기본 설정(Spring Boot 가 타임스탬프 직렬화를 끔)에서
ISO-8601 문자열로 나간다 — `"2026-09-22T14:03:11"`.

**변환은 DTO 안의 정적 팩토리** `from(Destination)` 에 둔다. 별도 Mapper 클래스를
만들지 않는다 — 지금은 필드 복사뿐이라 파일만 늘어난다.

---

## 4. 계층 구성

```
com/japantravel/
├── common/                         package 선언에서 _repo 제거 (아래 6절)
├── prefecture/
│   ├── entity/Prefecture.java              있음
│   └── repository/PrefectureRepository.java 신규 — existsByName(String)
└── destination/
    ├── entity/Destination.java             있음
    ├── repository/DestinationRepository.java 있음 (커밋만 남음)
    ├── dto/DestinationResponse.java        신규
    ├── service/DestinationService.java     신규
    └── controller/DestinationController.java 신규
```

### DestinationService

`@Service` · `@Transactional(readOnly = true)` · 생성자 주입(Lombok
`@RequiredArgsConstructor`).

| 메서드 | 동작 |
|---|---|
| `findAll(String prefecture)` | 이름이 비면 `findAllByOrderByIdDesc()`. 아니면 `PrefectureRepository.existsByName` 으로 검증 후 `findByPrefectureNameOrderByIdDesc()`. 없는 이름이면 `NotFoundException` |
| `findById(Long id)` | `findById` → 없으면 `NotFoundException` |

**Service 가 엔티티가 아니라 DTO 를 반환한다.** `open-in-view: false` 이므로
Controller 에서 LAZY 인 `prefecture` 를 건드리면 `LazyInitializationException` 이
난다. 변환을 트랜잭션 안에서 끝내면 이 문제가 생기지 않는다. 레포지토리의
`@EntityGraph(attributePaths = "prefecture")` 가 N+1 도 함께 막는다.

### DestinationController

`@RestController` · `@RequestMapping("/api/destinations")`. Service 를 호출하고
반환값을 그대로 돌려주는 얇은 계층. 엔티티를 보지 않는다.

---

## 5. 기각한 대안

| 대안 | 기각 이유 |
|---|---|
| 현 필터를 `?prefectureId=26` 으로 | D-025 의 "키는 id" 와는 맞지만, 소비자가 현 목록 API 를 먼저 받아 id 를 찾아야 한다. 현 47개는 이름이 바뀌지 않아 자연키로 안전하고, 이미 작성한 `findByPrefectureNameOrderByIdDesc` 와도 맞는다 |
| `prefecture` 와 `prefectureId` 둘 다 허용 | 분기가 늘고 "둘 다 준 경우" 규칙을 정해야 한다 |
| 목록 결과가 비면 무조건 404 | 데이터를 채우는 중에 멀쩡한 현이 전부 404 로 보인다 |
| `DestinationSummary` / `DestinationDetail` 분리 | 지도 때문에 목록에도 좌표가 필요해, 실익이 `description` 하나뿐 |
| 별도 `DestinationMapper` | 필드 복사뿐이라 파일만 늘어남 |
| 제안·승인을 인증 없이 먼저 구현 | 제안자를 폼 입력 문자열로 받게 되어, 인증 붙일 때 스키마와 API 를 다시 손봐야 함 |

---

## 6. 같이 정리하는 것

`common/` 4개 파일이 **디렉터리에는 `_repo` 가 없는데 `package` 선언에만
`_repo` 가 있다.** javac 는 출력 경로를 package 기준으로 잡으므로 컴파일과
컴포넌트 스캔은 통과하지만, IDE 는 이를 오류로 표시하고 탐색·리팩토링이 깨진다.

`_repo` 는 옛 계층 패키지와 신규 코드를 구분하려던 임시 구획이었다. 옛 백엔드는
이미 `3ee8788` 에서 통째로 지워져 구분할 대상이 없다. **`_repo` 를 폐기한다.**

- `common/error/{ConflictException,ForbiddenException,NotFoundException}.java`
- `common/web/ApiExceptionHandler.java`
- `CLAUDE.md` 의 경로 규칙

---

## 7. 확인 방법

MySQL 연결(`DB_PASSWORD`) 후 `bootRun` 으로 기동하고 `curl` 로 확인한다.

| 요청 | 기대 |
|---|---|
| `GET /api/destinations` | `200`, 전체 목록 |
| `GET /api/destinations?prefecture=교토부` | `200`, 교토부만 (없으면 `[]`) |
| `GET /api/destinations?prefecture=쿄토부` | `404` |
| `GET /api/destinations/{있는 id}` | `200` |
| `GET /api/destinations/999999` | `404` |
