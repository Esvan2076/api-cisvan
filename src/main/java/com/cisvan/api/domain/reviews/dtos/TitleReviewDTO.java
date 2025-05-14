package com.cisvan.api.domain.reviews.dtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleReviewDTO {
    private String tconst;
    private BigDecimal score;
    private String commentText;
    private boolean containsSpoiler;
    private List<GenreScoreDTO> genres;
    private List<ActorScoreDTO> actors;
    private List<DirectorScoreDTO> directors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenreScoreDTO {
        private String genre;
        @Builder.Default
        private BigDecimal score = BigDecimal.valueOf(0.00);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActorScoreDTO {
        private String nconst;
        @Builder.Default
        private BigDecimal score = BigDecimal.valueOf(0.00);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DirectorScoreDTO {
        private String nconst;
        @Builder.Default
        private BigDecimal score = BigDecimal.valueOf(0.00);
    }
}