package com.cisvan.api.domain.reviews.userGenresRating;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_genres_rating")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGenresRating {

    @EmbeddedId
    private UserGenresRatingId id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder.Default
    @Column(name = "rating", nullable = false, precision = 4, scale = 2)
    private BigDecimal rating = BigDecimal.valueOf(0.00);

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
