package com.zerry.flix_streaming.service;

import java.util.List;

import com.zerry.flix_streaming.dto.ContentDto;

public interface ContentService {
    List<ContentDto> getAllContents();

    ContentDto getContentById(Long id);
}
