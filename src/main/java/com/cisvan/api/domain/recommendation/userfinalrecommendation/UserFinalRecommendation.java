package com.cisvan.api.domain.recommendation.userfinalrecommendation;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_final_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFinalRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tconst", nullable = false, length = 15)
    private String tconst;

    @Column(name = "final_rank_for_user", nullable = false)
    private Integer rankForUser;

    @Column(name = "cross_match_count_for_user", nullable = false)
    private Integer crossMatchCountForUser;

    @Column(name = "aggregated_match_score_for_user", nullable = false, precision = 10, scale = 4)
    private BigDecimal aggregatedMatchScoreForUser;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
}
