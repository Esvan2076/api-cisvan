package com.cisvan.api.domain.defaultImage;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultImageService {
    
    private final DefaultImageRepository defaultImageRepository;

    public List<DefaultImage> getDefaultImages() {
        return defaultImageRepository.findAll();
    }
}
