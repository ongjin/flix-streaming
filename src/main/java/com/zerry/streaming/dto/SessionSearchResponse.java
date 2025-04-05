package com.zerry.streaming.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionSearchResponse {
    private String sessionId;
    private String username;
    private Long userId;
    private String videoId;
    private Integer lastPosition;
}