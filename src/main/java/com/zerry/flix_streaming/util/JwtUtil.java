package com.zerry.flix_streaming.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtException;
import java.util.Date;

/**
 * JwtUtil
 * JWT 토큰 생성 및 검증 기능을 제공합니다.
 */
public class JwtUtil {
    private static final String SECRET_KEY = "abc123qweDG";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1시간 유효

    /**
     * 주어진 사용자명을 기반으로 JWT 토큰을 생성합니다.
     *
     * @param username 사용자명
     * @return JWT 토큰 문자열
     */
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY.getBytes())
                .compact();
    }

    /**
     * 토큰을 검증하고 Claims를 반환합니다.
     *
     * @param token JWT 토큰
     * @return Claims (토큰에 포함된 정보)
     * @throws JwtException 검증 실패 시 발생
     */
    public static Claims validateToken(String token) throws JwtException {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }
}
