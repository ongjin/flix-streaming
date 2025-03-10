package com.zerry.streaming_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * SampleEntity
 * 데이터베이스 테이블과 매핑되는 JPA 엔티티입니다.
 */

@Entity
@Data
public class StreamingEntity {
    private String name;
    private String message;
}
