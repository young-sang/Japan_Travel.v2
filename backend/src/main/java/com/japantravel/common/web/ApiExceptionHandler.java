package com.japantravel.common.web;

import com.japantravel.common.error.ConflictException;
import com.japantravel.common.error.ForbiddenException;
import com.japantravel.common.error.NotFoundException;
import com.japantravel.common.error.UnauthorizedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

//  컨트롤러 · 서비스에서 난 401 (로그인 실패 등). 필터 단계의 401 은 여기까지 오지 않고
//  SecurityConfig 의 AuthenticationEntryPoint 가 같은 모양으로 쓴다.
    @ExceptionHandler
    public ResponseEntity<?> unauthorized(UnauthorizedException ex){
        return ResponseEntity.status(401).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<?> notFound(NotFoundException ex){
        return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<?> forbidden(ForbiddenException ex){
        return ResponseEntity.status(403).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<?> conflict(ConflictException ex){
        return ResponseEntity.status(409).body(ex.body());
    }

//  범위를 벗어난 값 (예: month=13). 리소스가 없는 것이 아니라 요청이 잘못된 것이다.
    @ExceptionHandler
    public ResponseEntity<?> badRequest(IllegalArgumentException ex){
        return ResponseEntity.status(400).body(Map.of("message", ex.getMessage()));
    }

//  타입이 맞지 않는 값 (예: month=abc). 이것을 잡지 않으면 Spring 기본 HTML 400 이
//  나가서, 같은 400 인데 본문 형식만 달라진다.
    @ExceptionHandler
    public ResponseEntity<?> typeMismatch(MethodArgumentTypeMismatchException ex){
        return ResponseEntity.status(400)
                .body(Map.of("message", ex.getName() + " 값이 올바르지 않습니다: " + ex.getValue()));
    }
}
