# festival 조회 API 설계

2026-09-22 · 확정 · greenfield 브랜치 · 진행 순서 2단계

`festival` 도메인을 5계층으로 새로 만든다. destination 과 같은 패턴을 반복하되,
날짜(`month` · `date_text`)와 필터 2개가 다르다.

관련 결정: [D-022](../../DECISIONS.md#d-022--places-를-destinations-와-festivals-로-다시-나눈다)
(festivals 를 별도 테이블로) · 설계 원본: destination 스펙
[2026-09-22-destination-query-api-design.md](2026-09-22-destination-query-api-design.md)

---

## 1. 범위

`GET /api/festivals` · `GET /api/festivals/{id}` 두 개. destination 과 같이
쓰기·관리자·테스트는 넣지 않는다.

---

## 2. 스키마 — `festivals` 추가

```sql
CREATE TABLE IF NOT EXISTS festivals (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(200) NOT NULL,
  prefecture_id BIGINT NOT NULL,
  month         INT NOT NULL,
  date_text     VARCHAR(100),
  description   TEXT,
  lat           DOUBLE,
  lng           DOUBLE,
  image_path    VARCHAR(500),
  created_at    DATETIME NOT NULL,
  UNIQUE (name, prefecture_id),
  FOREIGN KEY (prefecture_id) REFERENCES prefectures(id),
  CHECK (month BETWEEN 1 AND 12)
);
```

**destination 과의 차이**
- `month INT NOT NULL` — D-022 가 테이블 분리의 이득으로 예고한 제약
- `date_text VARCHAR(100)` — "7월 중순" 같은 자유 텍스트
- **`tags` 없음** — D-022 에 따라 태그는 destination 에만

**뺀 옛 컬럼** — `wiki_title` · `last_refreshed_at`. 둘 다 Wikipedia 수집기 전용이고,
수집기는 D-009 에서 범위 밖이다.

**`CHECK` 는 MySQL 8.0.16+ 에서 실제로 강제된다.** 그 이전 버전은 파싱만 하고 무시한다.
설치본이 8.0 이므로 동작한다.

**날짜를 실제 DATE 로 두지 않은 이유** — 축제는 "매년 7월 중순" 같은 반복 행사라
특정 연도의 날짜를 박으면 이듬해에 틀린 데이터가 된다. D-021 에 따라 데이터를 AI 로
생성하므로 생성된 구체 날짜의 신뢰도도 낮다. 월 + 자유 텍스트면 월별 필터가 되면서
낡지 않는다.

---

## 3. 엔드포인트 계약

### GET /api/festivals

| 항목 | 값 |
|---|---|
| 쿼리 | `prefecture` (선택, 현 이름) · `month` (선택, 1~12) |
| 정렬 | `month ASC, id DESC` |
| 200 | `FestivalResponse[]` |
| 404 | `prefecture` 가 `prefectures` 에 없는 이름 |
| 400 | `month` 가 1~12 밖이거나 정수가 아님 |

- 두 필터는 독립이며 조합 가능 (교토부 + 7월)
- 실재하는 현·달인데 0건이면 `200 []`
- **`month` 를 400 으로, 없는 현을 404 로 나눈 이유** — 13월은 애초에 존재할 수 없는
  잘못된 요청이고, 없는 현 이름은 "그런 리소스가 없다" 에 가깝다.

### GET /api/festivals/{id}

`200 FestivalResponse` / `404`.

### 에러 본문

`NotFoundException` → `404 {"message": "..."}` (기존 `ApiExceptionHandler`).
`400` 은 Spring 이 타입 변환 실패로 내는 기본 응답과, 범위 위반 시 던지는
`IllegalArgumentException` 을 핸들러에 추가해 맞춘다.

---

## 4. DTO

`festival/dto/FestivalResponse.java` — 목록·상세 공용 record.

```java
public record FestivalResponse(
        Long id, String name, String prefecture,
        Integer month, String dateText,
        String description, Double lat, Double lng,
        String imagePath, LocalDateTime createdAt
) {
    public static FestivalResponse from(Festival f) { ... }
}
```

destination 과 같은 규칙 — `prefecture` 는 이름 문자열로 평탄화, 변환은 DTO 안
정적 팩토리, 목록/상세를 나누지 않는다.

---

## 5. 계층 구성

```
com/japantravel/festival/
├── entity/Festival.java
├── repository/FestivalRepository.java
├── dto/FestivalResponse.java
├── service/FestivalService.java
└── controller/FestivalController.java
```

### 필터 조합은 쿼리 메서드 4개로 푼다

필터가 2개라 조합이 4가지다. **쓰지 않는 조건에 `null` 을 넘기는 방식은 쓸 수 없다** —
`WHERE month = NULL` 은 SQL 에서 unknown 이라 항상 0건이 된다. 그래서 조합마다
메서드를 둔다.

```java
List<Festival> findAllByOrderByMonthAscIdDesc();
List<Festival> findByPrefectureNameOrderByMonthAscIdDesc(String prefectureName);
List<Festival> findByMonthOrderByMonthAscIdDesc(Integer month);
List<Festival> findByPrefectureNameAndMonthOrderByMonthAscIdDesc(String p, Integer m);
```

전부 `@EntityGraph(attributePaths = "prefecture")` 를 단다. 분기는 Service 한 곳에만 둔다.

**기각한 대안**

| 대안 | 기각 이유 |
|---|---|
| `@Query` 하나로 `:p IS NULL OR ...` | 메서드와 분기가 하나로 줄지만 JPQL 문자열을 직접 쓰게 된다. destination 과 방식이 갈린다 |
| `Specification` (동적 쿼리) | 필터가 계속 늘면 깔끔하나 Criteria API 를 새로 배워야 하고 조회 2개에는 과하다 |

**나중에 필터가 3개가 되면** 조합이 8가지가 되어 이 방식이 무너진다. 그때
`@Query` 로 바꾼다. 고칠 곳은 레포지토리와 Service 메서드 하나뿐이다.

### Service

`@Service` · `@Transactional(readOnly = true)`. destination 과 같이 **DTO 를 반환한다**
(`open-in-view: false` 이므로 컨트롤러에서 LAZY `prefecture` 를 건드리면 터진다).

`findAll(String prefecture, Integer month)` 의 순서:
1. `month` 범위 검증 → 위반 시 400
2. `prefecture` 가 주어졌으면 `existsByName` 검증 → 없으면 404
3. 조합에 맞는 레포지토리 메서드 호출 → DTO 변환

---

## 6. 확인 방법

| 요청 | 기대 |
|---|---|
| `GET /api/festivals` | `200`, 전체 (month 오름차순) |
| `?prefecture=교토부` | `200` |
| `?month=7` | `200` |
| `?prefecture=교토부&month=7` | `200` |
| `?prefecture=쿄토부` | `404` |
| `?month=13` | `400` |
| `?month=abc` | `400` |
| `/{있는 id}` | `200` |
| `/999999` | `404` |
