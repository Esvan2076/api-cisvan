package com.cisvan.api.domain.reviews.userNameRating;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNameRatingRepository extends JpaRepository<UserNameRating, UserNameRatingId> {

    List<UserNameRating> findByUserId(Long userId);
    List<UserNameRating> findByIdNconst(String nconst);

    // Corregido para buscar por nconst (que es el identificador del nombre)
    List<UserNameRating> findByUserIdAndId_Nconst(Long userId, String nconst);

    List<UserNameRating> findById_ReviewId(Long reviewId);
}
