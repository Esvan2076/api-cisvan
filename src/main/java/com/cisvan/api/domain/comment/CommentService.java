package com.cisvan.api.domain.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
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

    public List<Comment> getRepliesRecursively(Long parentCommentId) {
        List<Comment> allReplies = new ArrayList<>();
        // Obtener respuestas directas del comentario
        List<Comment> directReplies = commentRepository.findByParentCommentId(parentCommentId);

        for (Comment reply : directReplies) {
            // Añadir la respuesta actual
            allReplies.add(reply);
            // Obtener respuestas de forma recursiva
            List<Comment> subReplies = getRepliesRecursively(reply.getId());
            allReplies.addAll(subReplies);
        }

        // Ordenar los resultados por fecha (de más antiguo a más nuevo)
        allReplies.sort(Comparator.comparing(Comment::getCreatedAt));

        return allReplies;
    }

    public int countRepliesRecursively(Long parentCommentId) {
        int totalCount = 0;
        List<Comment> directReplies = commentRepository.findByParentCommentId(parentCommentId);
        
        for (Comment reply : directReplies) {
            // Cuenta la respuesta actual
            totalCount++;
            // Cuenta las respuestas recursivamente
            totalCount += countRepliesRecursively(reply.getId());
        }
        
        return totalCount;
    }

    public Optional<String> findRootTconst(Long commentId) {
        return commentRepository.findRootTconst(commentId);
    }

    public void unreportComment(Long commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isEmpty()) return;

        Comment comment = optionalComment.get();
        comment.setIsReported(false);
        commentRepository.save(comment);
    }

    public List<Comment> getReportedComments() {
        return commentRepository.findByIsReportedTrueOrderByCreatedAtDesc();
    }
}
