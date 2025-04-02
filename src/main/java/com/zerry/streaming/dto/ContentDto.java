package com.zerry.streaming.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContentDto {
    private Long id;
    private String title;
    private String description;
    private String fileUrl;
    private String genre;
    private String thumbnailUrl;
    private Integer duration;
    private String status;
    private String contentUrl;
    private LocalDateTime releaseDate;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
}
