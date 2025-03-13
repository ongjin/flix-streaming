package com.zerry.flix_streaming.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zerry.flix_streaming.dto.ContentDto;
import com.zerry.flix_streaming.response.ApiResponse;
import com.zerry.flix_streaming.service.ContentService;
import com.zerry.flix_streaming.service.KafkaSender;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import org.springframework.http.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

@RestController
@Slf4j
public class StreamingController {

    @Autowired
    private KafkaSender kafkaSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContentService contentService;

    private final String videoDir = "flix-streaming/src/main/resources/videos/";

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Streaming Service is running."));
    }

    @PostMapping("/stream/start")
    public ResponseEntity<ApiResponse<String>> startStreaming() {
        // 스트리밍 시작 로직을 수행하고...
        // 핵심 이벤트 발생 시 Kafka 메시지를 전송합니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "Unknown";
        String logMessage = createLogMessage("STREAMING_START",
                "Streaming session started for user: " + username);
        kafkaSender.send("streaming", logMessage);
        return ResponseEntity.ok(ApiResponse.success("Streaming started for user: " + username));
    }

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

    @GetMapping("/")
    public ResponseEntity<ApiResponse<String>> index() {
        log.trace("TRACE!!");
        log.debug("DEBUG!!");
        log.info("INFO!!");
        log.warn("WARN!!");
        log.error("ERROR!!");
        return ResponseEntity.ok(ApiResponse.success("index"));
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
     * "test-topic" 토픽의 메시지를 수신합니다.
     * 
     * @param message 수신된 메시지
     */
    @KafkaListener(topics = "test-topic", groupId = "flix-streaming")
    public void listen(String message) {
        log.debug("Received message: {}", message);
    }

    @PostMapping("/kafka-send")
    public ResponseEntity<ApiResponse<String>> kafkaSend(@RequestBody Map<String, Object> map) {
        kafkaSender.send(map.get("topic").toString(), map.get("message").toString());
        return ResponseEntity.ok(ApiResponse.success("메시지 전송 완료", null));
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
     * 특정 콘텐츠의 상세 정보 조회 API
     */
    @GetMapping("/stream/contents/{id}")
    public ResponseEntity<ApiResponse<ContentDto>> getContentById(@PathVariable Long id) {
        ContentDto content = contentService.getContentById(id);
        if (content == null) {
            return ResponseEntity.status(404).body(ApiResponse.fail("콘텐츠를 찾을 수 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    /**
     * 정적 스트리밍 서비스: 파일을 그대로 Resource로 반환합니다.
     */
    @GetMapping(value = "/static-video/{filename}", produces = "video/mp4")
    public ResponseEntity<Resource> getStaticVideo(@PathVariable String filename) throws IOException {
        Path videoPath = Paths.get(videoDir + filename);
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

        Path videoPath = Paths.get(videoDir + filename);
        Resource resource = new UrlResource(videoPath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        long fileSize = resource.contentLength();
        AtomicLong rangeStart = new AtomicLong(1024);
        long rangeEnd = fileSize - 1; // 기본은 파일 전체

        // Range 헤더가 있다면 파싱
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

        // 응답 헤더 설정 (Partial Content인 경우)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "video/mp4");
        headers.add("Accept-Ranges", "bytes");
        if (StringUtils.hasText(rangeHeader)) {
            headers.add("Content-Range", "bytes " + rangeStart.get() + "-" + rangeEnd + "/" + fileSize);
            headers.add("Content-Length", String.valueOf(contentLength));
        } else {
            headers.add("Content-Length", String.valueOf(fileSize));
        }

        HttpStatus status = (StringUtils.hasText(rangeHeader)) ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = resource.getInputStream()) {
                // Range 시작 바이트까지 건너뛰기
                inputStream.skip(rangeStart.get());
                byte[] buffer = new byte[8192];
                long bytesRemaining = contentLength;
                int read;
                while (bytesRemaining > 0
                        && (read = inputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                    outputStream.write(buffer, 0, read);
                    bytesRemaining -= read;
                }
            }
        };

        return ResponseEntity.status(status).headers(headers).body(responseBody);
    }
}
