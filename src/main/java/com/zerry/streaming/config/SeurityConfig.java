package com.zerry.streaming.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.zerry.streaming.security.JwtAuthenticationFilter;

/**
 * SecurityConfig
 * 특정 엔드포인트(예: /health)는 인증 없이 접근할 수 있도록 설정합니다.
 */
@Configuration
public class SeurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                // 정적 스트리밍: /static-video/** 는 인증 없이 접근 허용
                                                .requestMatchers("/static-video/**").permitAll()
                                                // 동적 스트리밍: /dynamic-video/** 는 JWT 인증 필요
                                                .requestMatchers("/dynamic-video/**").authenticated()
                                                .requestMatchers("/stream/**").authenticated() // /stream 요청은 인증 필요
                                                .anyRequest().authenticated() // 그 외 요청은 인증 필요
                                )
                                .addFilterBefore(new JwtAuthenticationFilter(),
                                                UsernamePasswordAuthenticationFilter.class)

                                .httpBasic(Customizer.withDefaults()); // 기본 HTTP Basic 인증 사용 (테스트용)
                return http.build();
        }
}
