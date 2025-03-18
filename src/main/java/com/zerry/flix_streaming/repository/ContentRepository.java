package com.zerry.flix_streaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zerry.flix_streaming.model.Content;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    // 제목이나 장르 등으로 검색하는 메서드 추가 가능
}
