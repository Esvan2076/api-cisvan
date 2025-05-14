package com.cisvan.api.domain.reviews.userGenresRating;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGenresRatingRepository extends JpaRepository<UserGenresRating, UserGenresRatingId> {

    List<UserGenresRating> findByUserId(Long userId);

    // Acceso al campo tconst dentro de la clave embebida
    List<UserGenresRating> findById_Tconst(String tconst);
    
    // Método para obtener los géneros según el reviewId
    List<UserGenresRating> findById_ReviewId(Long reviewId);

    // Acceso a la combinación de usuario y tconst dentro de la clave embebida
    List<UserGenresRating> findByUserIdAndId_Tconst(Long userId, String tconst);
}