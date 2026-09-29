package com.japantravel.common.web;

import com.japantravel.common.error.ApiError;
import com.japantravel.common.error.ErrorCode;

//  모든 응답의 봉투. 성공이면 data, 실패면 error 가 채워지고 나머지는 null 이다 (D-035).
//  상태 코드는 본문에 싣지 않는다 — HTTP 상태 줄에만 있다.
//  컨트롤러는 ApiResponse.ok(...) 를 직접 반환한다. 서비스는 봉투를 모른다.
public record ApiResponse<T>(boolean success, T data, ApiError error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

//  돌려줄 데이터가 없는 성공(DELETE 등). 204 가 아니라 200 + data: null (D-035).
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(ErrorCode code) {
        return new ApiResponse<>(false, null, ApiError.of(code));
    }
}
