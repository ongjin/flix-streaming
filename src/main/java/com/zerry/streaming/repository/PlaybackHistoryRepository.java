package com.zerry.streaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zerry.streaming.model.PlaybackHistory;

import java.util.List;

@Repository
public interface PlaybackHistoryRepository extends JpaRepository<PlaybackHistory, Long> {
    List<PlaybackHistory> findByUserId(Long userId);
}
