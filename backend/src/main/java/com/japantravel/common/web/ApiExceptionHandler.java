package com.japantravel.common.web;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.common.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

//  컨트롤러 단계의 모든 예외를 ErrorResponse 로 바꾼다. 필터 단계의 401 은 여기까지 오지 않고
//  SecurityConfig 의 AuthenticationEntryPoint 가 같은 모양으로 쓴다. 코드별 설명은 docs/ERRORS.md.
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

//  서비스가 throw new ApiException(ErrorCode.XXX) 로 던진 것. 우리가 의도한 에러는 전부 여기로 온다.
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
        return toResponse(ex.getErrorCode());
    }

//  ── 아래 다섯은 컨트롤러 메서드가 실행되기 "전에" Spring MVC 가 던진다 ──
//  요청 → ① 핸들러 매핑(URL·메서드로 컨트롤러 메서드 고르기) → ② 인자 바인딩(파라미터 채우기)
//       → ③ 컨트롤러 실행. 이 다섯은 ① 이나 ② 에서 나므로 우리 코드에는 throw 가 없다.
//  받지 않으면 Spring 기본 본문 {timestamp, status, error, path} 가 나가 모양이 달라진다.

//  ② 인자 바인딩. @PathVariable · @RequestParam 의 문자열을 선언된 타입(Long, Integer)으로
//  바꾸다 실패했다. 예) /api/destinations/abc ("abc" → Long), /api/festivals?month=abc
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return toResponse(ErrorCode.TYPE_MISMATCH);
    }

//  ② 인자 바인딩. @RequestBody 를 Jackson 이 JSON → record 로 읽다 실패했다.
//  예) 본문이 비었음, {broken 같은 JSON 문법 오류. 필드가 빠지거나 null 인 것은 읽기는 성공한다.
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMalformed(HttpMessageNotReadableException ex) {
        return toResponse(ErrorCode.MALFORMED_REQUEST);
    }

//  ② 인자 바인딩. @RequestBody 를 읽을 변환기를 Content-Type 으로 고르는데 맞는 게 없다.
//  예) fetch 로 JSON.stringify 본문을 보내며 헤더를 빠뜨리면 text/plain 이 되어 여기 걸린다.
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return toResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

//  ① 핸들러 매핑. 어떤 @RequestMapping 에도 안 맞으면 Spring 은 마지막으로 정적 파일 핸들러
//  (ResourceHttpRequestHandler) 에 넘기고, 파일도 없으면 이것을 던진다. 이름에 Resource 가 붙은 이유.
//  예) /api/destinations/1/nope. 비공개 경로는 로그인 전이면 Security 가 먼저 401 을 낸다.
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return toResponse(ErrorCode.API_NOT_FOUND);
    }

//  ① 핸들러 매핑. URL 에 맞는 컨트롤러는 있는데 그 HTTP 메서드용 메서드가 없다.
//  예) DELETE /api/destinations/1 (GET 만 있음)
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMethod(HttpRequestMethodNotSupportedException ex) {
        return toResponse(ErrorCode.METHOD_NOT_ALLOWED);
    }

//  위 어디에도 걸리지 않은 모든 예외. 원인은 응답에 싣지 않고 서버 콘솔에만 남긴다.
//  로그를 남기는 곳은 여기 하나뿐이다 (D-034). 안 남기면 500 의 원인을 찾을 방법이 없다.
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("처리되지 않은 예외", ex);
        return toResponse(ErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode code) {
        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
    }
}
