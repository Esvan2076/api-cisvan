package com.cisvan.api.domain.commentLike;

import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comments-like")
@RequiredArgsConstructor
public class CommentLikeController {

    private final CommentLikeOrchestrator commentLikeOrchestrator;

    @PostMapping("/{commentId}/like-toggle")
    public ResponseEntity<?> toggleLikeComment(
        @PathVariable Long commentId,
        HttpServletRequest request
    ) {
        boolean isLiked = commentLikeOrchestrator.toggleLikeComment(commentId, request);
        return ResponseEntity.ok(Collections.singletonMap("liked", isLiked));
    }    
}