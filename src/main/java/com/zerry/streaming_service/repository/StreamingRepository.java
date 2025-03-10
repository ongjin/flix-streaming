package com.zerry.streaming_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zerry.streaming_service.model.StreamingEntity;

/**
 * SampleRepository
 * SampleEntity에 대한 데이터 접근 계층을 담당합니다.
 */
public interface StreamingRepository extends JpaRepository<StreamingEntity, Long> {

}
