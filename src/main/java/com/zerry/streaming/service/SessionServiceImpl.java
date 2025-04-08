package com.zerry.streaming.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.zerry.streaming.dto.ExternalApiResponse;
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
            ResponseEntity<ExternalApiResponse<SessionCreateResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<ExternalApiResponse<SessionCreateResponse>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ExternalApiResponse<SessionCreateResponse> apiResponse = response.getBody();
                if (apiResponse != null && "SUCCESS".equals(apiResponse.getStatus())) {
                    log.info("세션 생성 성공: {}", sessionId);
                    return sessionId;
                }
            }
            log.warn("세션 생성 실패: {}", sessionId);
            return null;
        } catch (Exception e) {
            log.error("세션 생성 요청 실패: {}, 오류: {}", sessionId, e.getMessage());
            return null;
        }
    }

    @Override
    public SessionDataDto getSession(String sessionId) {
        String url = sessionServiceUrl + "/api/v1/sessions/" + sessionId;

        try {
            ResponseEntity<ExternalApiResponse<SessionDataDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ExternalApiResponse<SessionDataDto>>() {
                    });
            log.info("response: {}", response);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ExternalApiResponse<SessionDataDto> apiResponse = response.getBody();
                if (apiResponse != null && "SUCCESS".equals(apiResponse.getStatus())) {
                    SessionDataDto sessionData = apiResponse.getData();
                    log.info("세션 조회 성공: {}, 위치: {}", sessionId, sessionData.getLastPosition());
                    return sessionData;
                }
            }
            log.warn("세션 조회 실패: {}", sessionId);
            return null;
        } catch (Exception e) {
            log.error("세션 조회 요청 실패: {}, 오류: {}", sessionId, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updateSessionPosition(String sessionId, int position) {
        String url = sessionServiceUrl + "/api/v1/sessions/" + sessionId;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String patchBody = "{\"lastPosition\": " + position + ", \"status\": \"ACTIVE\"}";
            HttpEntity<String> request = new HttpEntity<>(patchBody, headers);

            ResponseEntity<ExternalApiResponse<SessionDataDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    request,
                    new ParameterizedTypeReference<ExternalApiResponse<SessionDataDto>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ExternalApiResponse<SessionDataDto> apiResponse = response.getBody();
                if (apiResponse != null && "SUCCESS".equals(apiResponse.getStatus())) {
                    SessionDataDto sessionData = apiResponse.getData();
                    log.info("세션 위치 업데이트 성공: {}, 위치: {}", sessionId, sessionData.getLastPosition());
                    return true;
                }
            }
            log.warn("세션 위치 업데이트 실패: {}", sessionId);
            return false;
        } catch (Exception e) {
            log.error("세션 위치 업데이트 요청 실패: {}, 오류: {}", sessionId, e.getMessage());
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
            ResponseEntity<ExternalApiResponse<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<ExternalApiResponse<Map<String, Object>>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ExternalApiResponse<Map<String, Object>> apiResponse = response.getBody();
                if (apiResponse != null && "SUCCESS".equals(apiResponse.getStatus())) {
                    Map<String, Object> data = apiResponse.getData();
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
