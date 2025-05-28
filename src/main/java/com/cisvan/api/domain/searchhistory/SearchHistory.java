package com.cisvan.api.domain.searchhistory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_history", indexes = {
    @Index(name = "idx_user_created", columnList = "user_id, created_at DESC"),
    @Index(name = "idx_search_term", columnList = "search_term"),
    @Index(name = "idx_created_at", columnList = "created_at DESC")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "search_term", nullable = false, length = 255)
    private String searchTerm;
    
    @Column(name = "result_type", nullable = false, length = 20)
    private String resultType; // "movie", "serie", "person"
    
    @Column(name = "result_id", nullable = false, length = 15)
    private String resultId; // tconst o nconst
    
    @Column(name = "result_title", nullable = false, length = 255)
    private String resultTitle;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    // Método explícito para actualizar el timestamp
    public void updateTimestamp() {
        this.createdAt = LocalDateTime.now();
    }
}