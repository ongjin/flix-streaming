package com.zerry.streaming.dto;

import lombok.Data;

@Data
public class SessionCreateRequest {
    private Long userId;
    private String username;
    private String timestamp;
}