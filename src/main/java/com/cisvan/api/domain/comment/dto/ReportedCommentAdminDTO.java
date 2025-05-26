package com.cisvan.api.domain.comment.dto;

import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportedCommentAdminDTO {

    private Long id; // ID del comentario
    private String commentText;

    private String tconst;
    private String primaryTitle;

    private UserSummaryPrestigeDTO user;

    // Campos opcionales si es respuesta
    private Long replyToUserId;
    private String replyToUsername;
}