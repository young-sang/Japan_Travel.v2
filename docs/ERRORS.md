# 에러 코드

백엔드가 돌려주는 모든 에러의 목록과, 각각이 **어디서 · 무엇 때문에** 나는지.
코드의 정의는 [`common/error/ErrorCode.java`](../backend/src/main/java/com/japantravel/common/error/ErrorCode.java),
응답으로 바꾸는 곳은 [`common/web/ApiExceptionHandler.java`](../backend/src/main/java/com/japantravel/common/web/ApiExceptionHandler.java)
와 [`common/security/SecurityConfig.java`](../backend/src/main/java/com/japantravel/common/security/SecurityConfig.java)
의 `AuthenticationEntryPoint` 이다. 결정 배경은 [D-034](DECISIONS.md) (에러 체계) ·
[D-035](DECISIONS.md) (응답 봉투).

## 응답 모양

성공이든 에러든 모든 응답은 같은 봉투(`ApiResponse`)에 담긴다. 키는 항상 `success` · `data` ·
`error` 세 개이고, 빈 쪽은 `null` 이다. 상태 코드는 본문에 싣지 않고 HTTP 상태 줄에만 있다.

```json
// 에러 — HTTP 404
{ "success": false,
  "data": null,
  "error": { "code": "DESTINATION_NOT_FOUND", "message": "여행지를 찾을 수 없습니다" } }

// 성공 — HTTP 200 (참고)
{ "success": true, "data": { "id": 1, ... }, "error": null }
```

`error` 의 값은 `ApiError(code, message)` 이다.

- `code` — `ErrorCode` enum 이름. **프론트는 이 값으로 분기한다.**
- `message` — enum 에 적힌 고정 문구. 사람이 읽는 용도이고, 던지는 쪽에서 덮어쓰지 않는다.
  그래서 문제가 된 값(`id`, `prefecture`, `month`)은 응답에 실리지 않는다. 클라이언트는
  자기가 보낸 요청을 알고 있다.
- 에러가 여러 개인 경우(`@Valid` 도입 후 필드 검증)는 `error` 를 배열로 만들지 않고
  `error.fields` 목록을 추가해 확장한다 (D-035). 지금은 없다.

## 코드 추가 규칙

그 에러를 **던지는 코드를 만들 때** enum 과 이 문서에 함께 추가한다. 쓰는 곳 없는 코드를
미리 넣지 않는다. 예) `FORBIDDEN` 은 course 의 소유권 판정을 만들 때, `MISSING_PARAMETER` 는
필수 `@RequestParam` 을 처음 쓸 때(search 의 `q` 등) 추가한다.

## 목록

| code | 상태 | 출처 | 한 줄 요약 |
|---|---|---|---|
| `PREFECTURE_NOT_FOUND` | 404 | 서비스 | 없는 현 이름으로 필터 |
| `DESTINATION_NOT_FOUND` | 404 | 서비스 | 없는 여행지 id |
| `FESTIVAL_NOT_FOUND` | 404 | 서비스 | 없는 축제 id |
| `INVALID_MONTH` | 400 | 서비스 | month 가 1~12 밖 |
| `USERNAME_TAKEN` | 409 | 서비스 | 가입 시 아이디 중복 |
| `LOGIN_FAILED` | 401 | 서비스 | 아이디 없음 또는 비밀번호 틀림 |
| `UNAUTHORIZED` | 401 | 필터 · 서비스 | 로그인이 필요한데 유효한 토큰이 없음 |
| `TYPE_MISMATCH` | 400 | Spring | 경로·쿼리 값의 타입 변환 실패 |
| `MALFORMED_REQUEST` | 400 | Spring | 요청 본문 JSON 을 읽지 못함 |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | Spring | 본문의 Content-Type 이 JSON 이 아님 |
| `API_NOT_FOUND` | 404 | Spring | 매핑된 컨트롤러가 없는 경로 |
| `METHOD_NOT_ALLOWED` | 405 | Spring | 경로는 있는데 그 HTTP 메서드가 없음 |
| `INTERNAL_ERROR` | 500 | catch-all | 위 어디에도 해당하지 않는 예외 |

---

## 서비스가 던지는 에러

서비스가 `throw new ApiException(ErrorCode.XXX)` 로 던지고 `ApiExceptionHandler.handleApi` 가 받는다.

### `PREFECTURE_NOT_FOUND` — 404

- **어디서** `DestinationService.findAll`, `FestivalService.findAll`
- **무엇 때문에** `?prefecture=` 에 `prefectures` 테이블에 없는 이름이 왔다.
- **아닌 경우** 실재하는 현인데 해당 여행지·축제가 0건이면 에러가 아니라 빈 목록 `[]` 이다.
- **예** `GET /api/destinations?prefecture=없는현`

### `DESTINATION_NOT_FOUND` — 404

- **어디서** `DestinationService.findById` (`GET /api/destinations/{id}`)
- **무엇 때문에** 그 id 의 여행지가 없다.
- **예** `GET /api/destinations/999999`

### `FESTIVAL_NOT_FOUND` — 404

- **어디서** `FestivalService.findById` (`GET /api/festivals/{id}`)
- **무엇 때문에** 그 id 의 축제가 없다.
- **예** `GET /api/festivals/999999`

### `INVALID_MONTH` — 400

- **어디서** `FestivalService.validateMonth` (`GET /api/festivals?month=`)
- **무엇 때문에** month 가 1~12 밖이다. 13월은 존재할 수 없는 값이라 "리소스 없음(404)" 이
  아니라 "요청이 잘못됨(400)" 이다.
- **아닌 경우** 숫자가 아닌 값(`month=abc`)은 서비스까지 오지 못하고 `TYPE_MISMATCH` 가 된다.
- **예** `GET /api/festivals?month=13`, `?month=0`

### `USERNAME_TAKEN` — 409

- **어디서** `AuthService.signup` (`POST /api/auth/signup`)
- **무엇 때문에** 같은 username 이 이미 있다. `existsByUsername` 으로 먼저 확인한다.
- **아닌 경우** 두 요청이 동시에 같은 이름으로 가입하면 둘 다 확인을 통과하고 DB UNIQUE 에
  걸린다. 이건 `INTERNAL_ERROR` 로 나간다 (auth 설계에서 따로 처리하지 않기로 함).

### `LOGIN_FAILED` — 401

- **어디서** `AuthService.login` (`POST /api/auth/login`)
- **무엇 때문에** 아이디가 없거나 비밀번호가 틀렸다.
- **두 경우를 하나로 묶는 이유** 나누면 "이 아이디는 가입돼 있다" 를 알려주게 된다. 응답 본문이
  완전히 같아야 한다.

### `UNAUTHORIZED` — 401

두 종류의 자리에서 난다.

1. **`SecurityConfig` 의 `AuthenticationEntryPoint`** — 로그인이 필요한 경로에 유효한 토큰이 없다.
   토큰이 없음 · 위조 · 만료 · `Bearer ` 접두사 누락 모두 여기다. `JwtAuthenticationFilter` 는
   잘못된 토큰을 거절하지 않고 익명으로 통과시키고, 막을지는 `SecurityConfig` 의 URL 규칙이 정한다.
   이 단계는 `@RestControllerAdvice` 보다 앞이라 핸들러를 거치지 않고 EntryPoint 가 직접 쓴다.
2. **`AuthService.me` · `FavoriteService.findUser`** (즐겨찾기 추가) — 토큰은 유효한데 그 사용자가 삭제됐다.
   프론트에게는 "로그인이 풀린 것" 이므로 404 가 아니라 401 이다. 즐겨찾기 추가에서 이 확인을 빼면
   FK 위반으로 `INTERNAL_ERROR`(500) 가 된다.

> **Security 가 경로 판정보다 먼저다.** 로그인 전에는 없는 경로(`GET /api/nope`)도,
> 틀린 메서드(`DELETE /api/destinations/1`)도 `API_NOT_FOUND` · `METHOD_NOT_ALLOWED` 가 아니라
> `UNAUTHORIZED` 가 나간다. 공개 경로(`GET /api/destinations/**`, `GET /api/festivals/**`,
> `POST /api/auth/signup`, `POST /api/auth/login`) 에서만 로그인 없이 404 · 405 를 볼 수 있다.

---

## Spring 이 던지는 에러

컨트롤러 메서드가 **실행되기 전에** Spring MVC(`DispatcherServlet`) 가 요청을 처리하다가 던지는
예외들이다. 우리 코드에는 `throw` 가 없고, `ApiExceptionHandler` 가 받아서 우리 모양으로 바꾼다.
받지 않으면 Spring 기본 본문(`{timestamp, status, error, path}`) 이 나가서, 같은 400 인데
본문 모양이 달라진다.

요청이 컨트롤러에 닿기까지의 순서와, 각 예외가 나는 지점:

```
요청
 └ ① 핸들러 매핑 — URL · HTTP 메서드로 컨트롤러 메서드를 고른다
 │     경로는 맞는데 메서드가 없음 ─▶ HttpRequestMethodNotSupportedException   (405)
 │     어떤 컨트롤러에도 안 맞음  ─▶ 정적 리소스 핸들러로 넘어감 ─▶ 파일도 없음
 │                                  ─▶ NoResourceFoundException             (404)
 └ ② 인자 바인딩 — 메서드 파라미터를 채운다
 │     @PathVariable · @RequestParam 문자열 → Long · Integer 변환 실패
 │                                  ─▶ MethodArgumentTypeMismatchException   (400)
 │     @RequestBody: Content-Type 에 맞는 변환기를 고른다
 │       JSON 이 아님               ─▶ HttpMediaTypeNotSupportedException    (415)
 │       JSON 을 record 로 못 읽음  ─▶ HttpMessageNotReadableException       (400)
 └ ③ 컨트롤러 메서드 실행 → 서비스 (여기서부터 ApiException)
```

### `TYPE_MISMATCH` — 400 · `MethodArgumentTypeMismatchException`

- **어디서** ② 인자 바인딩. `@PathVariable Long id`, `@RequestParam Integer month` 처럼 선언된
  타입으로 문자열을 바꾸다 실패했다.
- **예** `GET /api/destinations/abc` (`abc` → `Long` 실패), `GET /api/festivals?month=abc`

### `MALFORMED_REQUEST` — 400 · `HttpMessageNotReadableException`

- **어디서** ② 인자 바인딩. `@RequestBody` 를 Jackson 이 JSON → record 로 읽다 실패했다.
- **예** 본문이 비었음, `{broken` 같은 문법 오류, 객체 자리에 배열(`[]`) 이 옴
- **아닌 경우** 필드가 빠지거나 `null` 인 것은 읽기 자체는 성공한다(해당 필드가 `null`).
  입력 검증(`@Valid`) 이 없으므로 그 `null` 은 서비스까지 가서 `INTERNAL_ERROR` 가 될 수 있다.

### `UNSUPPORTED_MEDIA_TYPE` — 415 · `HttpMediaTypeNotSupportedException`

- **어디서** ② 인자 바인딩. `@RequestBody` 를 읽을 변환기를 요청의 `Content-Type` 으로 고르는데,
  JSON 이 아니라 맞는 게 없다.
- **흔한 원인** 프론트에서 `fetch(url, { method: 'POST', body: JSON.stringify(x) })` 처럼 헤더를
  빠뜨리면 `Content-Type: text/plain` 이 된다. `headers: { 'Content-Type': 'application/json' }` 필요.

### `API_NOT_FOUND` — 404 · `NoResourceFoundException`

- **어디서** ① 핸들러 매핑. 어떤 `@RequestMapping` 에도 맞지 않으면 Spring 은 요청을 마지막으로
  정적 리소스 핸들러(`ResourceHttpRequestHandler`, `static/` 파일을 내주는 것) 에 넘긴다.
  거기서도 파일을 못 찾으면 이 예외를 던진다. 이름에 "Resource" 가 들어간 이유다 (Spring 6.1+).
- **예** `GET /api/destinations/1/nope`, (로그인 후) `GET /api/nope`

### `METHOD_NOT_ALLOWED` — 405 · `HttpRequestMethodNotSupportedException`

- **어디서** ① 핸들러 매핑. URL 에 맞는 컨트롤러는 있는데 그 HTTP 메서드용 메서드가 없다.
- **예** (로그인 후) `DELETE /api/destinations/1`

---

## 그 밖의 모든 예외

### `INTERNAL_ERROR` — 500

- **어디서** `ApiExceptionHandler.handleUnexpected` — `@ExceptionHandler(Exception.class)`.
  위 핸들러 어디에도 걸리지 않은 예외가 모두 여기로 온다.
- **응답** 원인은 싣지 않고 고정 문구만.
- **로그** 이 핸들러에서만 `log.error` 로 스택트레이스를 서버 콘솔에 남긴다. 안 남기면 500 의
  원인을 찾을 방법이 없다 (D-034 — 프로젝트의 "로깅 넣지 않음" 방침에 대한 명시적 예외).
- **예**
  - 동시 가입의 DB UNIQUE 위반
  - 검증하지 않은 `null` 입력: `POST /api/auth/signup` 에 `"password": null` → BCrypt 가
    `IllegalArgumentException`. 예전에는 모든 `IllegalArgumentException` 을 400 으로 바꿨지만,
    그러면 서버 버그도 "클라이언트 잘못" 으로 가려지므로 없앴다. 의도한 400 은 전부 `ErrorCode` 로 던진다.
  - 위 목록에 없는 Spring 예외 (406 등). 현재 코드에는 발생 경로가 없다.

## 이 체계 밖에 있는 것

- **필터 단계의 인증 외 예외** — 컨트롤러에 닿기 전 필터에서 난 예외는 `@RestControllerAdvice` 가
  받지 못하고 `/error` 로 넘어가 Spring 기본 본문으로 나간다. 지금은 이런 예외를 던지는 필터가
  없어서 처리하지 않는다 (`JwtAuthenticationFilter` 는 자기 예외를 삼킨다).
- **403** — 1차에는 권한 분기가 없어 발생하지 않는다. `AccessDeniedHandler` 도 두지 않는다.
