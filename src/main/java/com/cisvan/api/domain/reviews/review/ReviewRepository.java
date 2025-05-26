package com.cisvan.api.domain.reviews.review;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByUserId(Long userId);

    @Query("SELECT r FROM Review r JOIN Comment c ON r.commentId = c.id WHERE r.userId = :userId AND c.tconst = :tconst")
    Optional<Review> findByUserIdAndTconst(@Param("userId") Long userId, @Param("tconst") String tconst);
    
    @Query("SELECT r FROM Review r JOIN Comment c ON r.commentId = c.id ORDER BY c.likeCount DESC")
    Page<Review> findAllReviewsSortedByLikes(Pageable pageable);

    // Nuevo método para obtener el review por commentId
    Optional<Review> findByCommentId(Long commentId);

    Page<Review> findByUserId(Long userId, Pageable pageable);
}
