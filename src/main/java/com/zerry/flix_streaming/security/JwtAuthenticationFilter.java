package com.zerry.flix_streaming.security;

import io.jsonwebtoken.Claims;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.zerry.flix_streaming.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtAuthenticationFilter
 * 클라이언트 요청에서 JWT 토큰을 추출하여 검증한 후, 인증 정보를 SecurityContext에 저장합니다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = JwtUtil.validateToken(token);
                // subject 에서 userId 추출
                String userId = claims.getSubject();
                // claims 에서 username 추출 (예시로 "username" 키를 사용)
                String username = claims.get("username", String.class);
                if (username == null || username.isEmpty()) {
                    throw new RuntimeException("JWT token does not contain a valid username");
                }
                Map<String, Object> principal = new HashMap<>();
                principal.put("userId", userId);
                principal.put("username", username);

                // 인증 객체 생성 (권한은 필요한 경우 설정)
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, userId, Collections.emptyList());
                // 필요에 따라 auth 객체에 username 등의 정보를 추가할 수 있음.
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (RuntimeException e) {
                // JWT 검증 실패 시 컨텍스트 클리어 또는 에러 응답 처리 가능
                SecurityContextHolder.clearContext();
                // 응답에 에러 메시지를 담거나, 필터 체인 종료 로직 추가 가능
            }
        }
        filterChain.doFilter(request, response);
    }
}