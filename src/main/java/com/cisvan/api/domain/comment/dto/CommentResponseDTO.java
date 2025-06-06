package com.cisvan.api.domain.comment.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {

    private Long id;
    private String commentText;
    private Integer likeCount;
    private Boolean containsSpoiler;
    private LocalDateTime createdAt;
    private UserSummaryPrestigeDTO user;
    private boolean likedByMe;
    private Long replyToUserId;
    private String replyToUsername;
}
