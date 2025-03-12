package com.zerry.flix_streaming.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ContentDto {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private LocalDate releaseDate;
    private int duration;
    private String contentUrl;
}
