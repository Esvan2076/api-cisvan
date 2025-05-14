package com.cisvan.api.domain.reviews.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {

    private Long reviewId;
    private CommentContentDTO comment;
    private String titleName;
    private BigDecimal score;

    private List<GenreRatingDTO> genres;
    private List<ActorRatingDTO> actors;
    private List<DirectorRatingDTO> directors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentContentDTO {
        private Long id;
        private String commentText;
        private Integer likeCount;
        private Boolean containsSpoiler;
        private LocalDateTime createdAt;
        private UserSummaryPrestigeDTO user;
        private Boolean likedByMe;
        private Integer replyCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreRatingDTO {
        private String name;
        private BigDecimal score;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActorRatingDTO {
        private String primaryName;
        private BigDecimal score;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DirectorRatingDTO {
        private String primaryName;
        private BigDecimal score;
    }
}