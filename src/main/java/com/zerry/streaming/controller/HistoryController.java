package com.zerry.streaming.controller;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zerry.streaming.dto.SessionDataDto;
import com.zerry.streaming.response.ApiResponse;
import com.zerry.streaming.security.CustomUserDetails;
import com.zerry.streaming.service.SessionService;
import com.zerry.streaming.dto.StreamingResponse;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 재생 기록 및 세션 관리 API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/history")
@Slf4j
@RequiredArgsConstructor
public class HistoryController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionService sessionService;

    /**
     * 스트리밍 시작 API
     * 세션 생성 및 로그 기록
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<StreamingResponse>> startStreaming(
            @RequestParam String videoId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            Long userId = userDetails.getId();

            log.info("스트리밍 시작 요청: 사용자 {}, 비디오 {}", email, videoId);

            // 기존 세션 검색
            String existingSessionId = sessionService.findExistingSession(email, userId, videoId);
            if (existingSessionId != null) {
                log.info("기존 세션 사용: {}", existingSessionId);
                return ResponseEntity.ok(ApiResponse.success("기존 세션을 사용합니다.",
                        new StreamingResponse(existingSessionId, videoId)));
            }

            // 새 세션 생성
            String sessionId = sessionService.createSession(email, userId, videoId);
            if (sessionId == null) {
                log.error("세션 생성 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.fail("세션 생성에 실패했습니다."));
            }

            log.info("새 세션 생성: {}", sessionId);
            return ResponseEntity.ok(ApiResponse.success("새 세션이 생성되었습니다.",
                    new StreamingResponse(sessionId, videoId)));
        } catch (Exception e) {
            log.error("스트리밍 시작 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("스트리밍 시작 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 이어보기 API
     * 사용자의 마지막 재생 위치를 조회합니다.
     */
    @GetMapping("/resume")
    public ResponseEntity<ApiResponse<SessionDataDto>> resumeStreaming(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        Long userId = userDetails.getId();

        // 세션 ID가 없으면 세션이 없다고 응답
        if (sessionId == null || sessionId.isEmpty()) {
            log.info("세션 ID가 없음: 사용자 {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("세션이 없습니다. 먼저 시청을 시작해주세요."));
        }

        // 세션 데이터 조회
        SessionDataDto sessionData = sessionService.getSession(sessionId);
        log.info("sessionData: {}", sessionData);
        if (sessionData == null) {
            log.info("세션 데이터가 없음: 세션 {}", sessionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("세션이 만료되었거나 존재하지 않습니다."));
        }

        int lastPosition = sessionData.getLastPosition();

        return ResponseEntity.ok(ApiResponse.success("재생 위치 조회 완료", sessionData));
    }

    @PostMapping("/position")
    public ResponseEntity<ApiResponse<ObjectNode>> updatePlaybackPosition(
            @RequestParam int position,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        Long userId = userDetails.getId();

        log.info("재생 위치 업데이트 요청: 비디오 {}, 위치 {}, 세션 {}", position, sessionId);

        // 세션 ID가 없으면 세션이 없다고 응답
        if (sessionId == null || sessionId.isEmpty()) {
            log.info("세션 ID가 없음: 사용자 {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("세션이 없습니다. 먼저 시청을 시작해주세요."));
        }

        // 세션 데이터 조회
        SessionDataDto sessionData = sessionService.getSession(sessionId);
        if (sessionData == null) {
            log.info("세션 데이터가 없음: 세션 {}", sessionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("세션이 만료되었거나 존재하지 않습니다."));
        }

        // 세션 위치 업데이트
        boolean success = sessionService.updateSessionPosition(sessionId, position);
        if (!success) {
            log.error("세션 위치 업데이트 실패: 세션 {}, 위치 {}", sessionId, position);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("재생 위치 업데이트에 실패했습니다."));
        }

        log.info("세션 위치 업데이트 성공: 세션 {}, 위치 {}", sessionId, position);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("sessionId", sessionId);
        response.put("position", position);
        response.put("success", success);

        return ResponseEntity.ok(ApiResponse.success("재생 위치 업데이트 완료", response));
    }

    /**
     * 로그 메시지 생성 헬퍼 메서드
     */
    private String createLogMessage(String event, String message) {
        return String.format("[%s] %s - %s", Instant.now(), event, message);
    }
}