package com.zerry.streaming_service.Service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * SampleServiceImpl
 * SampleService 인터페이스의 기본 구현체입니다.
 */
@Service
@RequiredArgsConstructor
public class StreamingServiceImpl implements StreamingService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public String getGreeting(String name) {
        // 이름을 포함한 인사말 메시지를 반환합니다.
        return "Hello, " + name + "! Welcome to Streaming Service.";
    }

    /**
     * 주어진 토픽에 메시지를 전송합니다.
     * 
     * @param topic   전송할 토픽 이름
     * @param message 전송할 메시지
     */
    @Override
    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
