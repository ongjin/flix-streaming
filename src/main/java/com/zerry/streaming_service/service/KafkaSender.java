package com.zerry.streaming_service.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * KafkaSender
 * KafkaTemplate을 사용하여 메시지를 전송하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class KafkaSender {
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 주어진 토픽에 메시지를 전송합니다.
     *
     * @param topic   전송할 토픽 이름
     * @param message 전송할 메시지
     */
    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
