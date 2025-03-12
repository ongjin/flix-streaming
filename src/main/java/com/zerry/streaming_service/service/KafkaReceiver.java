package com.zerry.streaming_service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * KafkaReceiver
 * 
 * @KafkaListener를 사용하여 "test-topic"의 메시지를 수신합니다.
 */
@Service
@Slf4j
public class KafkaReceiver {
    /**
     * "test-topic" 토픽의 메시지를 수신합니다.
     *
     * @param message 수신된 메시지
     */
    @KafkaListener(topics = "test-topic", groupId = "streaming-service")
    public void listen(String message) {
        log.info("Test message: {}", message);
    }
}
