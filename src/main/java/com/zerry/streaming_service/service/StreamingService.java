package com.zerry.streaming_service.service;

/**
 * SampleService Interface
 * 비즈니스 로직에 대한 메서드 시그니처를 정의합니다.
 */
public interface StreamingService {
    /**
     * 주어진 이름으로 인사말 메시지를 생성합니다.
     *
     * @param name 사용자 이름
     * @return 인사말 메시지
     */
    String getGreeting(String name);

}
