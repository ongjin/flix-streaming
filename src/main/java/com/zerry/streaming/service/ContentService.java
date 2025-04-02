package com.zerry.streaming.service;

import java.util.List;

import com.zerry.streaming.dto.ContentDto;

public interface ContentService {
    List<ContentDto> getAllContents();

    ContentDto getContentById(Long id);
}
