package com.japantravel.common.error;

//  봉투(ApiResponse)의 error 필드 값. 응답 전체가 아니다 (D-035).
//  Spring 의 org.springframework.web.ErrorResponse 와는 무관하다.
//  @Valid 를 넣으면 여기에 fields 목록을 추가한다 — error 를 배열로 만들지 않는다 (D-035).
public record ApiError(String code, String message) {

    public static ApiError of(ErrorCode errorCode) {
        return new ApiError(errorCode.name(), errorCode.getMessage());
    }
}
