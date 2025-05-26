package com.cisvan.api.domain.trending.trendingsnapshot;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "trending_snapshot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false)
    private String contentId;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "rank_position", nullable = false)
    private Integer rank;
}