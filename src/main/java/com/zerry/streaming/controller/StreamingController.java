package com.zerry.streaming.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zerry.streaming.dto.ContentDto;
import com.zerry.streaming.response.ApiResponse;
import com.zerry.streaming.service.ContentService;
import com.zerry.streaming.service.SessionService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class StreamingController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContentService contentService;

    @Autowired
    private SessionService sessionService;

    private final String videoDir = "flix-streaming/src/main/resources/videos/";

    /**
     * 헬스 체크 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Streaming Service is running."));
    }

    /**
     * 스트리밍 시작 API
     * 세션 생성, 로그 기록 추가 로직 구현 필요
     */
    @PostMapping("/stream/start")
    public ResponseEntity<ApiResponse<String>> startStreaming() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "Unknown";
        Long userId = (authentication != null) ? Long.parseLong(authentication.getCredentials().toString()) : 0L;
        boolean sessionCreated = sessionService.createSession(username, userId);
        if (sessionCreated) {
            // 필요 시 로그 기록 추가 (예: createLogMessage 메서드 활용)
            return ResponseEntity.ok(ApiResponse.success("Streaming started for user: " +
                    username));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("Failed to create session for user: " + username));
        }

    }

    @SuppressWarnings("unused")
    private String createLogMessage(String event, String message) {
        ObjectNode logJson = objectMapper.createObjectNode();
        logJson.put("timestamp", Instant.now().toString());
        logJson.put("service_name", "streaming");
        logJson.put("log_level", "INFO");
        logJson.put("event", event);
        logJson.put("message", message);
        logJson.put("request_id", UUID.randomUUID().toString());
        logJson.put("host", "streaming-server-01");
        logJson.put("environment", "production");
        try {
            return objectMapper.writeValueAsString(logJson);
        } catch (Exception e) {
            // 실제 환경에서는 적절한 예외 처리 로직 추가
            return "{}";
        }
    }

    @GetMapping("/stream")
    public ResponseEntity<ApiResponse<String>> streamContent() {
        // SecurityContextHolder에서 인증된 사용자 정보를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("authentication: {}", authentication);
        String username = authentication != null ? authentication.getName() : "Unknown";
        // 스트리밍 콘텐츠(예시)를 반환합니다.
        String content = "Streaming content for user: " + username;
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    /**
     * 스트리밍 콘텐츠 목록 조회 API
     * 인증된 사용자에게 콘텐츠 목록을 반환합니다.
     */
    @GetMapping("/stream/contents")
    public ResponseEntity<ApiResponse<List<ContentDto>>> getContents() {
        List<ContentDto> contents = contentService.getAllContents();
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 목록 조회 성공", contents));
    }

    /**
     * 특정 콘텐츠의 상세 정보 조회 API
     */
    @GetMapping("/stream/contents/{id}")
    public ResponseEntity<ApiResponse<ContentDto>> getContentById(@PathVariable Long id) {
        ContentDto content = contentService.getContentById(id);
        if (content == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("콘텐츠를 찾을 수 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/stream/continue")
    public ResponseEntity<ApiResponse<String>> continueWatching() {
        // 예: 세션 서버와 연동하여, 인증된 사용자에 대한 마지막 재생 위치 조회
        // (여기서는 간단히 예시 메시지로 처리)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "Unknown";
        String continueInfo = "User " + username + " last watched at 600 seconds.";
        return ResponseEntity.ok(ApiResponse.success(continueInfo));
    }

    /**
     * 정적 스트리밍 서비스: 파일을 그대로 Resource로 반환합니다.
     */
    @GetMapping(value = "/static-video/{filename}", produces = "video/mp4")
    public ResponseEntity<Resource> getStaticVideo(@PathVariable String filename) throws IOException {
        Path videoPath = Paths.get(videoDir, filename);
        Resource resource = new UrlResource(videoPath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(resource);
    }

    /**
     * 동적 스트리밍 서비스: 클라이언트의 Range 요청을 처리하여,
     * 요청한 범위의 바이트만 읽어 전송합니다.
     */
    @GetMapping(value = "/dynamic-video/{filename}", produces = "video/mp4")
    public ResponseEntity<StreamingResponseBody> getDynamicVideo(
            @PathVariable String filename,
            @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {

        Path videoPath = Paths.get(videoDir, filename);
        Resource resource = new UrlResource(videoPath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        long fileSize = resource.contentLength();
        AtomicLong rangeStart = new AtomicLong(1024);
        long rangeEnd = fileSize - 1;

        if (StringUtils.hasText(rangeHeader)) {
            String rangeValue = rangeHeader.replace("bytes=", "").trim();
            String[] ranges = rangeValue.split("-");
            try {
                if (ranges.length > 0) {
                    rangeStart.set(Long.parseLong(ranges[0]));
                    if (ranges.length > 1 && StringUtils.hasText(ranges[1])) {
                        rangeEnd = Long.parseLong(ranges[1]);
                    }
                }
            } catch (NumberFormatException e) {
                log.error("Invalid Range Header: {}", rangeHeader, e);
            }
            if (rangeEnd > fileSize - 1) {
                rangeEnd = fileSize - 1;
            }
            if (rangeStart.get() > rangeEnd) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
            }
        }

        long contentLength = rangeEnd - rangeStart.get() + 1;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "video/mp4");
        headers.add("Accept-Ranges", "bytes");
        if (StringUtils.hasText(rangeHeader)) {
            headers.add("Content-Range", "bytes " + rangeStart.get() + "-" + rangeEnd + "/" + fileSize);
            headers.add("Content-Length", String.valueOf(contentLength));
        } else {
            headers.add("Content-Length", String.valueOf(fileSize));
        }
        HttpStatus status = StringUtils.hasText(rangeHeader) ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = resource.getInputStream()) {
                inputStream.skip(rangeStart.get());
                byte[] buffer = new byte[8192];
                long bytesRemaining = contentLength;
                int read;
                while (bytesRemaining > 0 &&
                        (read = inputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                    outputStream.write(buffer, 0, read);
                    bytesRemaining -= read;
                }
            }
        };

        return ResponseEntity.status(status).headers(headers).body(responseBody);
    }
}
