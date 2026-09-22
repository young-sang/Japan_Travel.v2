package com.japantravel.common.web;

import com.japantravel.common.error.ConflictException;
import com.japantravel.common.error.ForbiddenException;
import com.japantravel.common.error.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

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
}
