package com.cisvan.api.domain.profileImage;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
public class ProfileImageController {

    private final ProfileImageService userProfileImageService;
    private final ProfileImageOrchestrator profileImageOrchestrator;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProfileImage>> fetchProfileImagesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileImageService.getProfileImagesByUserId(userId));
    }

    @GetMapping("/user")
    public ResponseEntity<List<ProfileImage>> fetchProfileImagesByUser(HttpServletRequest request) {
        return ResponseEntity.ok(profileImageOrchestrator.getProfileImagesByUser(request));
    }
}
