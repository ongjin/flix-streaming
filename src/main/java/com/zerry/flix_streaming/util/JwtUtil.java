package com.zerry.flix_streaming.util;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.JwtException;

/**
 * JwtUtil
 * JWT 토큰 생성 및 검증 기능을 제공합니다.
 */
@Component
public class JwtUtil {
    private static final String SECRET_KEY = "abc123qweDG";

    /**
     * 토큰을 검증하고 Claims를 반환합니다.
     *
     * @param token JWT 토큰
     * @return Claims (토큰에 포함된 정보)
     * @throws JwtException 검증 실패 시 발생
     */
    public static Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token", e);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Expired JWT token", e);
        }
    }
}
