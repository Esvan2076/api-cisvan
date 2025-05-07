package com.cisvan.api.domain.comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.comment.dto.CommentResponseDTO;
import com.cisvan.api.domain.comment.dto.CreateCommentDTO;
import com.cisvan.api.domain.commentLike.CommentLikeService;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.services.TitleService;
import com.cisvan.api.domain.userprestige.UserPrestigeService;
import com.cisvan.api.domain.userprestige.dtos.UserPrestigeDTO;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeDTO;
import com.cisvan.api.domain.users.mapper.UserMapper;
import com.cisvan.api.domain.users.services.UserLogicService;
import com.cisvan.api.domain.users.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentOrchestrator {

    private final CommentService commentService;
    private final UserLogicService userLogicService;
    private final UserService userService;
    private final TitleService titleService;
    private final UserMapper userMapper;
    private final CommentLikeService commentLikeService;
    private final UserPrestigeService userPrestigeService;
    
    public Optional<Comment> createComment(CreateCommentDTO dto, HttpServletRequest request) {

        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        Users user = userOpt.get();
    
        Comment comment = Comment.builder()
            .userId(user.getId())
            .tconst(dto.getTconst())
            .parentCommentId(dto.getParentCommentId())
            .commentText(dto.getCommentText())
            .containsSpoiler(dto.getContainsSpoiler())
            .isReview(false)
            .likeCount(0)
            .createdAt(LocalDateTime.now())
            .build();
    
            Comment result = commentService.saveComment(comment);
        return Optional.of(result);
    }

    public List<CommentResponseDTO> getCommentsForTitle(String tconst, HttpServletRequest request) {
        // 1. Validar existencia del título
        Optional<Title> titleOpt = titleService.getTitleById(tconst);
        if (titleOpt.isEmpty()) return List.of();

        // 2. Obtener usuario autenticado (si hay uno)
        Optional<Users> currentUserOpt = userLogicService.getUserFromRequest(request);
        Long currentUserId = currentUserOpt.map(Users::getId).orElse(null);
        System.out.println("Usuario autenticado ID: " + currentUserId);
        // 3. Obtener todos los comentarios (NO reseñas)
        List<Comment> comments = commentService.getCommentsForTitle(tconst);

        // 4. Convertir a DTOs enriquecidos
        return comments.stream()
            .map(comment -> {
                // 4.1 Obtener información del autor
                Users author = userService.getById(comment.getUserId()).orElse(null);
                UserPrestigeDTO prestige = userPrestigeService.getPrestigeDTOByUserId(comment.getUserId()).orElse(null);

                UserSummaryPrestigeDTO authorDto = userMapper.toSummaryPrestige(author, prestige);

                // 4.2 Saber si el usuario actual dio like
                boolean liked = currentUserId != null && commentLikeService.isLikedByUser(comment.getId(), currentUserId);

                // 4.3 Crear el DTO final
                return CommentResponseDTO.builder()
                    .id(comment.getId())
                    .commentText(comment.getCommentText())
                    .likeCount(comment.getLikeCount())
                    .containsSpoiler(comment.getContainsSpoiler())
                    .createdAt(comment.getCreatedAt())
                    .user(authorDto)
                    .likedByMe(liked)
                    .build();
            })
            .toList();
    }

    public void deleteComment(Long commentId, HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) return;
    
        Long userId = userOpt.get().getId();
        commentService.deleteComment(commentId, userId);
    }
}
