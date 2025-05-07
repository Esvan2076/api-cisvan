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
public class CommentResponseDTO {

    private Long id;
    private String commentText;
    private Integer likeCount;
    private Boolean containsSpoiler;
    private LocalDateTime createdAt;

    private UserSummaryPrestigeDTO user; // Autor del comentario

    private Boolean likedByMe; // ¿El usuario autenticado le dio like?
}
