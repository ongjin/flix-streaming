package com.zerry.streaming_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * RedisConfig
 * Redis 연동을 위한 설정을 제공합니다.
 */
@Configuration
public class RedisConfig {

    /**
     * StringRedisTemplate을 빈으로 등록하여 Redis 연산에 사용합니다.
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return StringRedisTemplate 인스턴스
     */
    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
