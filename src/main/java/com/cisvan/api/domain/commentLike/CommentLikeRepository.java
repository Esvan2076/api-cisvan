package com.cisvan.api.domain.commentLike;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {

    boolean existsByIdCommentIdAndIdUserId(Long commentId, Long userId);

    void deleteByIdCommentIdAndIdUserId(Long commentId, Long userId);

    int countByIdCommentId(Long commentId);

    boolean existsById_UserIdAndId_CommentId(Long userId, Long commentId);

    @Query("""
        SELECT cl.createdAt
        FROM CommentLike cl
        JOIN Comment c ON cl.id.commentId = c.id
        WHERE c.userId = :userId
    """)
    List<LocalDateTime> findAllLikeDatesForUser(@Param("userId") Long userId);
}
