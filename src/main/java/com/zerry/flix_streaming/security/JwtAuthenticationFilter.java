package com.zerry.flix_streaming.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.zerry.flix_streaming.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
                String userId = claims.getSubject();
                String username = claims.get("username", String.class);

                if (username == null || username.isEmpty()) {
                    throw new JwtException("JWT token does not contain a valid username");
                }

                // 권한 정보 추출
                List<String> roles = claims.get("roles", List.class);
                Collection<GrantedAuthority> authorities = new ArrayList<>();
                if (roles != null) {
                    roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
                }

                // Principal 정보 구성
                Map<String, Object> principal = new HashMap<>();
                principal.put("userId", userId);
                principal.put("username", username);
                principal.put("roles", roles);

                // AbstractAuthenticationToken 사용
                AbstractAuthenticationToken auth = new AbstractAuthenticationToken(authorities) {
                    @Override
                    public Object getCredentials() {
                        return null;
                    }

                    @Override
                    public Object getPrincipal() {
                        return principal;
                    }
                };
                auth.setAuthenticated(true);

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token");
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Authentication failed");
            }
        }
        filterChain.doFilter(request, response);
    }
}