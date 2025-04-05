package com.zerry.streaming.dto;

import lombok.Data;

@Data
public class StreamingHistoryRequest {
    private String userId;
    private String deviceInfo;
    private String ipAddress;
}