package com.japantravel.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

//  이 API 가 낼 수 있는 모든 에러. 각 코드가 어디서 · 무엇 때문에 나는지는 docs/ERRORS.md.
//  추가 규칙: 그 에러를 던지는 코드를 만들 때 함께 추가한다. 쓰는 곳 없는 코드는 미리 넣지 않는다.
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

//  공통
    PREFECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 현입니다"),

//  destination
    DESTINATION_NOT_FOUND(HttpStatus.NOT_FOUND, "여행지를 찾을 수 없습니다"),

//  festival
    FESTIVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "축제를 찾을 수 없습니다"),
    INVALID_MONTH(HttpStatus.BAD_REQUEST, "month 는 1~12 여야 합니다"),

//  user
    USERNAME_TAKEN(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다"),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다"),
//  남의 것을 건드림. 도메인 이름을 붙이지 않는다 — review · course · post 가 같이 쓴다
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),

//  review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다"),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "별점은 1~5 여야 합니다"),

//  Spring 이 던지는 요청 오류 (ApiExceptionHandler 가 변환)
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 값의 형식이 올바르지 않습니다"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type 입니다"),
    API_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 API 입니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않는 HTTP 메서드입니다"),

//  그 밖의 모든 예외
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다");

    private final HttpStatus status;
    private final String message;
}
