package com.cisvan.api.domain.recommendation.usersecondary;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_secondary_recommendations",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "rank_for_user"}),
        @UniqueConstraint(columnNames = {"user_id", "tconst"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSecondaryRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst;

    @Column(name = "rank_for_user", nullable = false)
    private Short rankForUser;

    @Column(name = "cross_match_count_for_user", nullable = false)
    private Integer crossMatchCountForUser;

    @Column(name = "aggregated_match_score_for_user", precision = 12, scale = 4)
    private BigDecimal aggregatedMatchScoreForUser;

    @Builder.Default
    @Column(name = "last_calculated_at", nullable = false)
    private LocalDateTime lastCalculatedAt = LocalDateTime.now(); // El procedimiento lo establecerá con CURRENT_TIMESTAMP
}