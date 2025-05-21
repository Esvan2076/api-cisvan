package com.cisvan.api.domain.recommendation.userrecommendationset;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cisvan.api.domain.recommendation.userrecomendationitem.UserRecommendationItem;

@Entity
@Table(name = "user_recommendation_sets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRecommendationSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder.Default
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "recommendationSet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("rank ASC") // Para que los ítems se recuperen ordenados por rank
    @Builder.Default
    private List<UserRecommendationItem> items = new ArrayList<>();

    // Métodos helper para mantener la consistencia de la relación bidireccional
    public void addItem(UserRecommendationItem item) {
        items.add(item);
        item.setRecommendationSet(this);
    }

    public void removeItem(UserRecommendationItem item) {
        items.remove(item);
        item.setRecommendationSet(null);
    }
}