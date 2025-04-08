package com.zerry.streaming.dto;

import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalApiResponse<T> {
    private String status;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}