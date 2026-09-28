package com.japantravel.common.error;

//  모든 에러 응답의 본문. 필터 단계(SecurityConfig)와 컨트롤러 단계(ApiExceptionHandler)가 같이 쓴다.
//  Spring 의 org.springframework.web.ErrorResponse 와는 무관하다 (우리 코드에서 import 하지 않는다).
public record ErrorResponse(String code, String message) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage());
    }
}
