package com.zerry.streaming.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zerry.streaming.dto.ContentDto;
import com.zerry.streaming.response.ApiResponse;
import com.zerry.streaming.service.ContentService;

import lombok.extern.slf4j.Slf4j;

/**
 * 콘텐츠 관련 API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/content")
@Slf4j
public class ContentController {

    @Autowired
    private ContentService contentService;

    /**
     * 스트리밍 콘텐츠 목록 조회 API
     * 인증된 사용자에게 콘텐츠 목록을 반환합니다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentDto>>> getContents() {
        log.info("콘텐츠 목록 조회 요청");
        List<ContentDto> contents = contentService.getAllContents();
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 목록 조회 성공", contents));
    }

    /**
     * 특정 콘텐츠의 상세 정보 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentDto>> getContentById(@PathVariable Long id) {
        log.info("콘텐츠 상세 정보 조회 요청: {}", id);
        ContentDto content = contentService.getContentById(id);
        if (content == null) {
            log.warn("콘텐츠를 찾을 수 없음: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("콘텐츠를 찾을 수 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 상세 정보 조회 성공", content));
    }
}