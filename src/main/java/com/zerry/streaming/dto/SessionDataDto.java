package com.zerry.streaming.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SessionDataDto {
    private String sessionId; // userId + "-" + deviceId
    private Long userId; // 사용자 ID
    private String deviceInfo; // 디바이스 정보 (예: Web, iOS 등)
    private String ipAddress;
    private String status; // 재생 상태 (예: playing, paused 등)
    private Long contentId; // 재생 중인 콘텐츠 ID
    private String deviceId; // 디바이스 식별자 (예: "WEB", "MOBILE_12")
    private int lastPosition; // 마지막 재생 위치 (초 단위)
    private LocalDateTime lastAccessTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String videoId; // 비디오 ID
}
