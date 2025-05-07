package com.cisvan.api.domain.commentLike;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @PostMapping("/{commentId}/like")
    public ResponseEntity<?> likeComment(
        @PathVariable Long commentId,
        HttpServletRequest request
    ) {
        commentLikeOrchestrator.likeComment(commentId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<?> unlikeComment(
        @PathVariable Long commentId,
        HttpServletRequest request
    ) {
        commentLikeOrchestrator.unlikeComment(commentId, request);
        return ResponseEntity.ok().build();
    }
}