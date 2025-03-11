package com.zerry.streaming_service.repository;

import com.zerry.streaming_service.model.PlaybackHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlaybackHistoryRepository extends JpaRepository<PlaybackHistory, Long> {
    List<PlaybackHistory> findByUserId(Long userId);
}
