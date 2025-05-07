package com.cisvan.api.domain.commentLike;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;

    public void like(Long commentId, Long userId) {
        CommentLikeId id = new CommentLikeId(userId, commentId);
        if (!commentLikeRepository.existsById(id)) {
            CommentLike like = CommentLike.builder().id(id).build();
            commentLikeRepository.save(like);
        }
    }

    public void unlike(Long commentId, Long userId) {
        CommentLikeId id = new CommentLikeId(userId, commentId);
        commentLikeRepository.deleteById(id);
    }

    public boolean isLikedByUser(Long commentId, Long userId) {
        return commentLikeRepository.existsByIdCommentIdAndIdUserId(commentId, userId);
    }
}
