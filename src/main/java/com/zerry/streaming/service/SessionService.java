package com.zerry.streaming.service;

import com.zerry.streaming.dto.SessionDataDto;

public interface SessionService {
    String createSession(String username, Long userId, String videoId);

    SessionDataDto getSession(String sessionId);

    boolean updateSessionPosition(String sessionId, int position);

    String findExistingSession(String username, Long userId, String videoId);
}
