package com.zerry.streaming.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * PlaybackHistory 엔티티
 * 사용자가 재생한 동영상 이력을 기록합니다.
 */
@Entity
@Table(name = "playback_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaybackHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 인증 서비스에서 전달받은 사용자 식별자 (스트리밍 서비스에서는 userId만 저장)
    @Column(nullable = false)
    private Long userId;

    // 재생한 콘텐츠 (Content와 다대일 관계)
    @ManyToOne
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    // 재생 시작 시각
    @CreationTimestamp
    private LocalDateTime watchedAt;

    // 재생한 시간 (초 단위)
    private int durationWatched;

    // 사용자가 부여한 평점 (선택, 1~5 등)
    private Integer rating;
}
