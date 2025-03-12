package com.zerry.flix_streaming.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Content 엔티티
 * 스트리밍할 동영상의 메타데이터를 관리합니다.
 */
@Entity
@Table(name = "contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 동영상 제목
    @Column(nullable = false, length = 255)
    private String title;

    // 동영상 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 장르 (예: 액션, 코미디 등)
    private String genre;

    // 출시일
    private LocalDate releaseDate;

    // 동영상 재생 시간 (초 단위)
    private int duration;

    // 실제 동영상 파일의 URL (CDN 주소 등)
    @Column(length = 500)
    private String contentUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
