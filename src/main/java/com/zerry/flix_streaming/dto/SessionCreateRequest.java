package com.zerry.flix_streaming.dto;

import lombok.Data;

@Data
public class SessionCreateRequest {
    private Long userId;
    private String username;
    private String timestamp;
}