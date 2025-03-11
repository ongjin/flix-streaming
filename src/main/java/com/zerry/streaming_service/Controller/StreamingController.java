package com.zerry.streaming_service.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.zerry.streaming_service.dto.ContentDto;
import com.zerry.streaming_service.response.ApiResponse;
import com.zerry.streaming_service.service.ContentService;
import com.zerry.streaming_service.service.KafkaSender;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class StreamingController {

    @Autowired
    private KafkaSender kafkaSender;

    @Autowired
    private ContentService contentService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Streaming Service is running."));
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
    // @KafkaListener(topics = "test-topic")
    // public void listen(String message) {
    // log.debug("Received message: {}", message);
    // }

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
}
