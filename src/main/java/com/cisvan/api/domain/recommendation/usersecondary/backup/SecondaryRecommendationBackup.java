package com.cisvan.api.domain.recommendation.usersecondary.backup;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "secondary_recommendation_backups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecondaryRecommendationBackup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "backup_id")
    private Long backupId;

    @Column(name = "original_secondary_rec_id") // Puede ser null si la original ya no existe
    private Long originalSecondaryRecId;

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

    @Column(name = "original_last_calculated_at", nullable = false)
    private LocalDateTime originalLastCalculatedAt;

    @Builder.Default
    @Column(name = "backup_taken_at", nullable = false)
    private LocalDateTime backupTakenAt = LocalDateTime.now();
}