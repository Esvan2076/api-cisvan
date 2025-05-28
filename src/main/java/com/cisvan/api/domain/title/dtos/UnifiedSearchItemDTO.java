package com.cisvan.api.domain.title.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnifiedSearchItemDTO {
    private String id; // tconst o nconst
    private String type; // "movie", "serie", "person"
    private String title; // primaryTitle o primaryName
    private String subtitle; // año, profesión, actores, etc
    private boolean isRecent;
    private boolean isPopular;
    private int priority; // para ordenamiento en frontend
}
