package com.zerry.streaming.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.zerry.streaming.dto.SessionCreateResponse;
import com.zerry.streaming.dto.SessionDataDto;

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
    public String createSession(String username, Long userId, String videoId) {
        String url = sessionServiceUrl + "/api/v1/sessions";

        // 세션 데이터 생성
        SessionDataDto sessionData = new SessionDataDto();
        sessionData.setUserId(userId);
        String sessionId = UUID.randomUUID().toString();
        sessionData.setSessionId(sessionId);
        sessionData.setDeviceInfo("WEB");
        sessionData.setStatus("ACTIVE");
        sessionData.setLastPosition(0);
        sessionData.setCreatedAt(LocalDateTime.now());
        sessionData.setUpdatedAt(LocalDateTime.now());
        sessionData.setVideoId(videoId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<SessionDataDto> request = new HttpEntity<>(sessionData, headers);
            ResponseEntity<SessionCreateResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    SessionCreateResponse.class);

            log.info("세션 생성 성공: {}", response.getBody());
            return sessionId;
        } catch (Exception e) {
            log.error("세션 생성 요청 실패: {}", e.getMessage());
            return null;
        }
    }

    public SessionDataDto getSession(String sessionId) {
        String url = sessionServiceUrl + "/api/v1/sessions/" + sessionId;

        try {
            ResponseEntity<SessionDataDto> response = restTemplate.getForEntity(
                    url,
                    SessionDataDto.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("세션 조회 성공: {}", sessionId);
                return response.getBody();
            } else {
                log.warn("세션 조회 실패: {}", sessionId);
                return null;
            }
        } catch (Exception e) {
            log.error("세션 조회 요청 실패: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updateSessionPosition(String sessionId, int position) {
        String url = sessionServiceUrl + "/api/v1/sessions/" + sessionId;

        try {
            // PATCH 요청을 위한 데이터 준비
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // PATCH 요청 본문 생성
            String patchBody = "{\"lastPosition\": " + position + "}";
            HttpEntity<String> request = new HttpEntity<>(patchBody, headers);

            // PATCH 요청 보내기
            ResponseEntity<SessionDataDto> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.PATCH,
                    request,
                    SessionDataDto.class);

            log.info("세션 위치 업데이트 성공: {}", sessionId);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("세션 위치 업데이트 요청 실패: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String findExistingSession(String username, Long userId, String videoId) {
        log.info("기존 세션 검색: 사용자 {}, 비디오 {}", username, videoId);

        // 세션 서비스에 요청하여 기존 세션 검색
        String url = sessionServiceUrl + "/api/v1/sessions/search";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "username", username,
                "userId", userId,
                "videoId", videoId);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                if ("SUCCESS".equals(responseBody.get("status"))) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    if (data != null) {
                        String sessionId = (String) data.get("sessionId");
                        log.info("기존 세션 찾음: {}", sessionId);
                        return sessionId;
                    }
                }
            }
            log.info("기존 세션을 찾을 수 없음");
            return null;
        } catch (Exception e) {
            log.error("세션 검색 중 오류 발생: {}", e.getMessage());
            // 오류 발생 시 null 반환하여 새 세션 생성 진행
            return null;
        }
    }
}
