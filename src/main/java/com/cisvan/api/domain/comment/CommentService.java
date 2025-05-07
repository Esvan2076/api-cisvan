package com.cisvan.api.domain.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsForTitle(String tconst) {
        return commentRepository.findByTconstAndIsReviewFalse(tconst);
    }

    public List<Comment> getReviewsForTitle(String tconst) {
        return commentRepository.findByTconstAndIsReviewTrue(tconst);
    }

    public List<Comment> getReplies(Long parentCommentId) {
        return commentRepository.findByParentCommentId(parentCommentId);
    }

    public Optional<Comment> getById(Long id) {
        return commentRepository.findById(id);
    }

    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) return;

        Comment comment = commentOpt.get();

        // Solo puede borrar el autor del comentario
        if (!comment.getUserId().equals(userId)) return;

        commentRepository.delete(comment);
    }
}
