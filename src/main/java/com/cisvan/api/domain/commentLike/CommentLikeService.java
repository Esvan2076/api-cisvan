package com.cisvan.api.domain.commentLike;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.comment.CommentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public boolean toggleLike(Long commentId, Long userId) {
        CommentLikeId id = new CommentLikeId(userId, commentId);

        // Verificar si el comentario existe
        if (!commentRepository.existsById(commentId)) {
            return false;
        }

        // Verificar si el usuario ya dio like
        if (commentLikeRepository.existsById(id)) {
            // Si existe el like, lo eliminamos (unlike)
            commentLikeRepository.deleteById(id);
            decrementLikeCount(commentId);
            return false;
        } else {
            // Si no existe el like, lo añadimos (like)
            CommentLike like = CommentLike.builder().id(id).build();
            commentLikeRepository.save(like);
            incrementLikeCount(commentId);
            return true;
        }
    }

    @Transactional
    public void incrementLikeCount(Long commentId) {
        commentRepository.incrementLikeCount(commentId);
    }

    @Transactional
    public void decrementLikeCount(Long commentId) {
        commentRepository.decrementLikeCount(commentId);
    }

    public boolean isLikedByUser(Long commentId, Long userId) {
        return commentLikeRepository.existsByIdCommentIdAndIdUserId(commentId, userId);
    }
}
