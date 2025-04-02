package com.zerry.streaming.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.zerry.streaming.dto.SessionCreateRequest;
import com.zerry.streaming.dto.SessionCreateResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final RestTemplate restTemplate;

    // 세션 서버는 도커 네트워크상에서 session-service:8052로 접근
    @Value("${session.service.url:http://localhost:8052}")
    private String sessionServiceUrl;

    public SessionServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public boolean createSession(String username, Long userId) {
        String url = sessionServiceUrl + "/session";
        SessionCreateRequest request = new SessionCreateRequest();
        request.setUsername(username);
        request.setUserId(userId);
        request.setTimestamp(Instant.now().toString());
        try {
            ResponseEntity<SessionCreateResponse> response = restTemplate.postForEntity(url, request,
                    SessionCreateResponse.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("세션 생성 요청 실패: {}", e.getMessage());
            return false;
        }
    }
}
