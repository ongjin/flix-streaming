package com.zerry.streaming.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.zerry.streaming.dto.ContentDto;
import com.zerry.streaming.model.Content;
import com.zerry.streaming.repository.ContentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;

    @Override
    public List<ContentDto> getAllContents() {
        List<Content> contents = contentRepository.findAll();
        return contents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContentDto getContentById(Long id) {
        return contentRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    private ContentDto convertToDto(Content content) {
        ContentDto dto = new ContentDto();
        dto.setId(content.getId());
        dto.setTitle(content.getTitle());
        dto.setDescription(content.getDescription());
        dto.setGenre(content.getGenre());
        dto.setReleaseDate(content.getReleaseDate());
        dto.setDuration(content.getDuration());
        dto.setContentUrl(content.getContentUrl());
        return dto;
    }
}
