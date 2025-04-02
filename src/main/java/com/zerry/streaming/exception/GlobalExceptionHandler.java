package com.zerry.streaming.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zerry.streaming.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception ex, HttpServletRequest request) {
        // 에러 응답의 Content-Type을 강제로 application/json으로 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<?> errorResponse = ApiResponse.error("서버 에러가 발생했습니다: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
