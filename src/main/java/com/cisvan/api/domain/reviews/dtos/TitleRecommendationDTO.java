package com.cisvan.api.domain.reviews.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para las recomendaciones de títulos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleRecommendationDTO {
    
    private String tconst;
    private String title;
    private BigDecimal rating; // Calificación del título
    private int matchCount;    // Número de coincidencias
    private BigDecimal matchScore; // Puntuación total de coincidencias
}