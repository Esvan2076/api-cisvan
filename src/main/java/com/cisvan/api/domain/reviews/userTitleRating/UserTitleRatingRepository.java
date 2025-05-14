package com.cisvan.api.domain.reviews.userTitleRating;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTitleRatingRepository extends JpaRepository<UserTitleRating, UserTitleRatingId> {

    List<UserTitleRating> findByUserId(Long userId);

    List<UserTitleRating> findById_Tconst(String tconst);

    // Método corregido para acceder al campo dentro de UserTitleRatingId
    Optional<UserTitleRating> findById_ReviewId(Long reviewId);

    Optional<UserTitleRating> findByUserIdAndId_Tconst(Long userId, String tconst);

    
}
