package com.zerry.flix_streaming.dto;

import lombok.Data;

@Data
public class SessionCreateResponse {
    private String sessionId;
    private String status;
}