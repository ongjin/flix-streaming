package com.zerry.streaming.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ApiResponse
 * 모든 서버(마이크로서비스)가 공통으로 사용하는 응답 포맷
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String status; // SUCCESS, FAIL, ERROR 등
    private String message; // 처리 결과 메시지
    private T data; // 실제 응답 데이터
    private LocalDateTime timestamp; // 응답 시각

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .message("정상 처리되었습니다.")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> fail(String message) {
        return ApiResponse.<T>builder()
                .status("FAIL")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status("ERROR")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}