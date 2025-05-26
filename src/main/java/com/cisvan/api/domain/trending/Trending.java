package com.cisvan.api.domain.trending;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trending_content")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trending {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trending_id")
    private Long trendingId;

    @Column(name = "content_id", nullable = false)
    private String contentId;

    @Builder.Default
    @Column(name = "score", nullable = false)
    private Integer score = 0;

    @Builder.Default
    @Column(name = "historical_score", nullable = false)
    private Long historicalScore = 0L;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
