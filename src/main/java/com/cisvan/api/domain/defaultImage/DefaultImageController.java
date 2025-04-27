package com.cisvan.api.domain.defaultImage;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/default-image")
@RequiredArgsConstructor
public class DefaultImageController {

    private final DefaultImageService defaultImageService;
    
    @GetMapping
    public ResponseEntity<List<DefaultImage>> fetchDefaultImages() {
        return ResponseEntity.ok(defaultImageService.getDefaultImages());
    }
}
