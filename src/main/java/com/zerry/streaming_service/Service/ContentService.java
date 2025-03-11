package com.zerry.streaming_service.service;

import com.zerry.streaming_service.dto.ContentDto;
import java.util.List;

public interface ContentService {
    List<ContentDto> getAllContents();

    ContentDto getContentById(Long id);
}
