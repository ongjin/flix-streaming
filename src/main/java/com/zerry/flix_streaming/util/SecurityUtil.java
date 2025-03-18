package com.zerry.flix_streaming.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SecurityUtil {

    // 현재 로그인한 사용자 정보 가져오기
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    // 현재 로그인한 사용자 이름(username) 가져오기
    public String getUsername() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return authentication != null ? authentication.getPrincipal().toString() : null;
    }

    // 현재 로그인한 사용자 ID(userId) 가져오기
    public Long getUserId() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Map) {
            return (Long) ((Map<?, ?>) authentication.getDetails()).get("userId");
        }
        return null;
    }
}
