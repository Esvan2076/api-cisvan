package com.cisvan.api.domain.recommendation.userrecomendationitem;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cisvan.api.domain.recommendation.userrecommendationset.UserRecommendationSet;

@Entity
@Table(name = "user_recommendation_items",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"recommendation_set_id", "rank"}),
        @UniqueConstraint(columnNames = {"recommendation_set_id", "tconst"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRecommendationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_set_id", nullable = false)
    private UserRecommendationSet recommendationSet;

    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst;

    @Column(name = "rank", nullable = false)
    private Short rank; // Usamos Short para 1-30

    @Column(name = "match_count")
    private Integer matchCount;

    @Column(name = "match_score", precision = 12, scale = 4) // Ajustado para mayor precisión
    private BigDecimal matchScore;

    @Column(name = "title_rating_at_recommendation", precision = 4, scale = 2)
    private BigDecimal titleRatingAtRecommendation; // Corresponde al 'rating' del TitleRecommendationDTO

    @Builder.Default
    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt = LocalDateTime.now();
}