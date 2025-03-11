package com.zerry.streaming_service.repository;

import com.zerry.streaming_service.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {
    // 제목이나 장르 등으로 검색하는 메서드 추가 가능
}
