package com.cisvan.api.domain.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTconst(String tconst);

    List<Comment> findByParentCommentId(Long parentCommentId);

    List<Comment> findByTconstAndIsReviewFalse(String tconst); // Solo comentarios

    List<Comment> findByTconstAndIsReviewTrue(String tconst); // Solo reseñas
}