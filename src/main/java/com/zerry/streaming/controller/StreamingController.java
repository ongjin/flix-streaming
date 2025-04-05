package com.zerry.streaming.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.zerry.streaming.dto.SessionDataDto;
import com.zerry.streaming.response.ApiResponse;
import com.zerry.streaming.security.CustomUserDetails;
import com.zerry.streaming.service.SessionService;

import lombok.extern.slf4j.Slf4j;

/**
 * 비디오 스트리밍 API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/stream")
@Slf4j
public class StreamingController {

    @Autowired
    private SessionService sessionService;

    @Value("${app.video.dir:src/main/resources/videos/}")
    private String videoDir;

    /**
     * 헬스 체크 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Streaming Service is running."));
    }

    /**
     * 정적 스트리밍 서비스: 파일을 그대로 Resource로 반환합니다.
     */
    @GetMapping(value = "/static/{filename}", produces = "video/mp4")
    public ResponseEntity<Resource> getStaticVideo(@PathVariable String filename) throws IOException {
        log.info("정적 비디오 스트리밍 요청: {}", filename);
        log.info("비디오 디렉토리 경로: {}", videoDir);

        Path videoPath = Paths.get(videoDir, filename);
        log.info("비디오 파일 경로: {}", videoPath.toAbsolutePath());

        Resource resource = new UrlResource(videoPath.toUri());
        if (!resource.exists()) {
            log.warn("비디오 파일을 찾을 수 없음: {}", filename);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        log.info("비디오 파일을 찾았습니다: {}", filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(resource);
    }

    /**
     * 동적 스트리밍 서비스: 클라이언트의 Range 요청을 처리하여,
     * 요청한 범위의 바이트만 읽어 전송합니다.
     */
    @GetMapping(value = "/video/{videoId}", produces = "video/mp4")
    public ResponseEntity<StreamingResponseBody> streamDynamicVideo(
            @PathVariable String videoId,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        log.info("동적 비디오 스트리밍 요청: {}, 세션: {}", videoId, sessionId);

        // 사용자 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        String email = userDetails.getUsername();

        // 세션 ID가 없으면 새 세션 생성
        final String finalSessionId;
        if (sessionId == null || sessionId.isEmpty()) {
            finalSessionId = UUID.randomUUID().toString();
            sessionService.createSession(email, userId, videoId);
            log.info("새 세션 생성: {}", finalSessionId);
        } else {
            // 기존 세션 조회
            SessionDataDto sessionData = sessionService.getSession(sessionId);
            if (sessionData != null) {
                log.info("기존 세션 조회: {}, 마지막 위치: {}", sessionId, sessionData.getLastPosition());
                finalSessionId = sessionId;
            } else {
                log.warn("세션을 찾을 수 없음: {}", sessionId);
                finalSessionId = UUID.randomUUID().toString();
                sessionService.createSession(email, userId, videoId);
                log.info("새 세션 생성: {}", finalSessionId);
            }
        }

        // 비디오 스트리밍 로직
        Path videoPath = Paths.get(videoDir, videoId);
        Resource resource;
        try {
            resource = new UrlResource(videoPath.toUri());
        } catch (IOException e) {
            log.error("비디오 리소스를 찾을 수 없음: {}", videoId, e);
            return ResponseEntity.notFound().build();
        }

        if (!resource.exists()) {
            log.error("비디오 파일을 찾을 수 없음: {}", videoId);
            return ResponseEntity.notFound().build();
        }

        long fileSize;
        try {
            fileSize = resource.contentLength();
        } catch (IOException e) {
            log.error("비디오 파일 크기를 가져올 수 없음: {}", videoId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        AtomicLong rangeStart = new AtomicLong(0);
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
        headers.add("X-Session-Id", finalSessionId);
        if (StringUtils.hasText(rangeHeader)) {
            headers.add("Content-Range", "bytes " + rangeStart.get() + "-" + rangeEnd + "/" + fileSize);
            headers.add("Content-Length", String.valueOf(contentLength));
        } else {
            headers.add("Content-Length", String.valueOf(fileSize));
        }
        HttpStatus status = StringUtils.hasText(rangeHeader) ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;

        StreamingResponseBody responseBody = outputStream -> {
            try {
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

                    // 스트리밍 완료 후 세션 위치 업데이트
                    long finalPosition = rangeStart.get() + contentLength - bytesRemaining;
                    sessionService.updateSessionPosition(finalSessionId, (int) finalPosition);
                }
            } catch (IOException e) {
                log.error("비디오 스트리밍 중 오류 발생: {}", videoId, e);
                throw new RuntimeException("비디오 스트리밍 중 오류가 발생했습니다.", e);
            }
        };

        return ResponseEntity.status(status).headers(headers).body(responseBody);
    }
}