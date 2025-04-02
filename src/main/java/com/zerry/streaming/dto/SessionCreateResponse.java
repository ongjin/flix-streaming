package com.zerry.streaming.dto;

import lombok.Data;

@Data
public class SessionCreateResponse {
    private String sessionId;
    private String status;
}