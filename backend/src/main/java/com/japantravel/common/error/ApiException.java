package com.japantravel.common.error;

import lombok.Getter;

//  서비스가 의도해서 던지는 유일한 예외. 상태와 메시지는 ErrorCode 가 정한다.
//  예) throw new ApiException(ErrorCode.DESTINATION_NOT_FOUND);
@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
