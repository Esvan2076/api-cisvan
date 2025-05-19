package com.cisvan.api.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTconst(String tconst);

    List<Comment> findByParentCommentId(Long parentCommentId);

    List<Comment> findByTconstAndIsReviewFalse(String tconst); // Solo comentarios

    List<Comment> findByTconstAndIsReviewTrue(String tconst); // Solo reseñas

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void incrementLikeCount(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId AND c.likeCount > 0")
    void decrementLikeCount(@Param("commentId") Long commentId);

    // Método para encontrar el tconst raíz usando una consulta nativa con CTE recursivo
    @Query(value = """
        WITH RECURSIVE comment_hierarchy AS (
            SELECT id, tconst, parent_comment_id
            FROM comment
            WHERE id = :commentId
            UNION
            SELECT c.id, c.tconst, c.parent_comment_id
            FROM comment c
            JOIN comment_hierarchy ch ON c.id = ch.parent_comment_id
        )
        SELECT tconst
        FROM comment_hierarchy
        WHERE tconst IS NOT NULL
        LIMIT 1;
    """, nativeQuery = true)
    Optional<String> findRootTconst(@Param("commentId") Long commentId);

    Optional<Comment> findByUserIdAndTconstAndIsReviewTrue(Long userId, String tconst);

    // Método para obtener los comentarios de un contenido específico ordenados por likeCount
    Page<Comment> findByTconstAndIsReviewTrueOrderByLikeCountDesc(String tconst, Pageable pageable);

    @Query("SELECT c.tconst FROM Comment c WHERE c.userId = :userId AND c.tconst IS NOT NULL")
    List<String> findTconstsByUserId(@Param("userId") Long userId);
}