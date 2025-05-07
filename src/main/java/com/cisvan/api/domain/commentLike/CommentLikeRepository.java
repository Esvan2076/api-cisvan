package com.cisvan.api.domain.commentLike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {

    boolean existsByIdCommentIdAndIdUserId(Long commentId, Long userId);

    void deleteByIdCommentIdAndIdUserId(Long commentId, Long userId);

    int countByIdCommentId(Long commentId);
}
