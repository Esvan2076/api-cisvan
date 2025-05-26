package com.cisvan.api.domain.searchtrending;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_trending", indexes = {
    @Index(name = "idx_weighted_score", columnList = "weighted_score DESC"),
    @Index(name = "idx_last_updated", columnList = "last_updated")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchTrending {
    
    @Id
    @Column(name = "result_id", length = 15)
    private String resultId;
    
    @Column(name = "result_type", nullable = false, length = 20)
    private String resultType;
    
    @Column(name = "result_title", nullable = false, length = 255)
    private String resultTitle;
    
    @Column(name = "search_count", nullable = false)
    private Long searchCount;
    
    @Column(name = "weighted_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal weightedScore;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}