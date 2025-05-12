package com.cisvan.api.domain.userprestigebackup;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_prestige_backup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPrestigeBackup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long backupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "current_rank", nullable = false)
    private Short currentRank;

    @Column(name = "weighted_score", nullable = false)
    private BigDecimal weightedScore;

    @Column(name = "total_likes", nullable = false)
    private Integer totalLikes;

    @Column(name = "comments_with_10", nullable = false)
    private Integer commentsWith10;

    @Column(name = "comments_with_15", nullable = false)
    private Integer commentsWith15;

    @Column(name = "comments_with_50", nullable = false)
    private Integer commentsWith50;

    @Column(name = "trend_direction", length = 1)
    private String trendDirection;

    @Column(name = "last_score_evaluated", nullable = false)
    private BigDecimal lastScoreEvaluated;

    @Column(name = "last_like_checkpoint", nullable = false)
    private Integer lastLikeCheckpoint;

    @Column(name = "backup_timestamp", nullable = false)
    private LocalDateTime backupTimestamp;
}
