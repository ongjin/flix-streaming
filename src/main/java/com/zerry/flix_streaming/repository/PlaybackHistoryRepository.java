package com.zerry.flix_streaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zerry.flix_streaming.model.PlaybackHistory;

import java.util.List;

public interface PlaybackHistoryRepository extends JpaRepository<PlaybackHistory, Long> {
    List<PlaybackHistory> findByUserId(Long userId);
}
