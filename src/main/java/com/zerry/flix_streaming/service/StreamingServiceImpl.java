package com.zerry.flix_streaming.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * SampleServiceImpl
 * SampleService 인터페이스의 기본 구현체입니다.
 */
@Service
@RequiredArgsConstructor
public class StreamingServiceImpl implements StreamingService {

    @Override
    public String getGreeting(String name) {
        // 이름을 포함한 인사말 메시지를 반환합니다.
        return "Hello, " + name + "! Welcome to Streaming Service.";
    }

}
