package com.cisvan.api.domain.userprestige;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_prestige")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPrestige {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "current_rank", nullable = false)
    private short currentRank;

    @Column(name = "weighted_score", nullable = false)
    private BigDecimal weightedScore;

    @Column(name = "total_likes", nullable = false)
    private int totalLikes;

    @Column(name = "comments_with_10", nullable = false)
    private int commentsWith10;

    @Column(name = "comments_with_15", nullable = false)
    private int commentsWith15;

    @Column(name = "comments_with_50", nullable = false)
    private int commentsWith50;

    @Column(name = "last_rank_change", nullable = false)
    private LocalDateTime lastRankChange;

    @Column(name = "trend_direction")
    private String trendDirection;

    @Column(name = "last_score_evaluated", nullable = false)
    private BigDecimal lastScoreEvaluated;

    @Column(name = "last_like_checkpoint", nullable = false)
    private int lastLikeCheckpoint;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
