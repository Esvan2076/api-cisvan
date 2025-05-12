package com.cisvan.api.domain.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentContentDTO {

    private Long id;
    private String commentText;
    private Integer likeCount;
    private Boolean containsSpoiler;
    private LocalDateTime createdAt;
    
    private UserSummaryPrestigeDTO user;
    private Boolean likedByMe;
    private Integer replyCount;
}
