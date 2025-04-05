package com.zerry.streaming.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.zerry.streaming.security.JwtAuthenticationFilter;
import com.zerry.streaming.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

/**
 * SecurityConfig
 * 특정 엔드포인트(예: /health)는 인증 없이 접근할 수 있도록 설정합니다.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 정적 스트리밍: /api/v1/stream/static/** 는 인증 없이 접근 허용
                        .requestMatchers("/api/v1/stream/static/**").permitAll()
                        .requestMatchers("/stream/**").authenticated() // /stream 요청은 인증 필요
                        .anyRequest().authenticated() // 그 외 요청은 인증 필요
                )
                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider),
                        UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults()); // 기본 HTTP Basic 인증 사용 (테스트용)
        return http.build();
    }
}