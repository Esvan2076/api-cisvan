package com.cisvan.api.domain.recommendation.userrecommendationset.backup;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "primary_recommendation_item_backups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrimaryRecommendationItemBackup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "backup_id")
    private Long backupId;

    @Column(name = "original_item_id", nullable = false)
    private Long originalItemId;

    @Column(name = "original_set_id", nullable = false)
    private Long originalSetId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst;

    @Column(name = "rank_in_set", nullable = false)
    private Short rankInSet;

    @Column(name = "match_count")
    private Integer matchCount;

    @Column(name = "match_score", precision = 12, scale = 4)
    private BigDecimal matchScore;

    @Column(name = "title_rating_at_recommendation", precision = 4, scale = 2)
    private BigDecimal titleRatingAtRecommendation;

    @Column(name = "item_saved_at", nullable = false)
    private LocalDateTime itemSavedAt;

    @Builder.Default
    @Column(name = "backup_taken_at", nullable = false)
    private LocalDateTime backupTakenAt = LocalDateTime.now();
}
