package com.cisvan.api.domain.comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import com.cisvan.api.common.OperationResult;
import com.cisvan.api.domain.comment.dto.CommentContentDTO;
import com.cisvan.api.domain.comment.dto.CommentResponseDTO;
import com.cisvan.api.domain.comment.dto.CreateCommentDTO;
import com.cisvan.api.domain.comment.dto.CreateReplyCommentDTO;
import com.cisvan.api.domain.comment.dto.ReportedCommentAdminDTO;
import com.cisvan.api.domain.commentLike.CommentLikeService;
import com.cisvan.api.domain.notification.services.NotificationService;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.services.TitleService;
import com.cisvan.api.domain.trending.TrendingService;
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
    private final NotificationService notificationService;
    private final CommentLikeService commentLikeService;
    private final UserPrestigeService userPrestigeService;
    private final TrendingService trendingService;
    
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

        // Solo si es comentario a título (no respuesta)
        if (dto.getTconst() != null) {
            trendingService.registerCommentPoints(user.getId(), dto.getTconst());
        }

        return Optional.of(result);
    }

    public Optional<Comment> createReplyComment(CreateReplyCommentDTO dto, HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
    
        Users user = userOpt.get();
    
        // Crear el comentario
        Comment comment = Comment.builder()
            .userId(user.getId())
            .parentCommentId(dto.getParentCommentId())
            .replyToUserId(dto.getReplyToUserId())
            .commentText(dto.getCommentText())
            .containsSpoiler(dto.getContainsSpoiler())
            .isReview(false)
            .likeCount(0)
            .createdAt(LocalDateTime.now())
            .build();
    
        Comment result = commentService.saveComment(comment);
    
        // Obtener el ID del contenido (tconst) subiendo por la jerarquía de comentarios
        Optional<String> contentIdOpt = commentService.findRootTconst(dto.getParentCommentId());

        // Si el ID del contenido se obtuvo correctamente:
        contentIdOpt.ifPresent(contentId -> {
            notificationService.notifyReplyComment(dto.getReplyToUserId(), contentId);
            trendingService.registerReplyPoints(user.getId(), contentId); // 👈 Nuevo para tendencia
        });
    
        return Optional.of(result);
    }    

    public List<CommentContentDTO> getCommentsForTitle(String tconst, HttpServletRequest request) {
        // 1. Validar existencia del título
        Optional<Title> titleOpt = titleService.getTitleById(tconst);
        if (titleOpt.isEmpty()) return List.of();
    
        // 2. Obtener usuario autenticado (si hay uno)
        Optional<Users> currentUserOpt = userLogicService.getUserFromRequest(request);
        Long currentUserId = currentUserOpt.map(Users::getId).orElse(null);
    
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
    
                // 4.3 Obtener el número de respuestas recursivamente
                int replyCount = commentService.countRepliesRecursively(comment.getId());
    
                // 4.4 Crear el DTO final
                return CommentContentDTO.builder()
                    .id(comment.getId())
                    .commentText(comment.getCommentText())
                    .likeCount(comment.getLikeCount())
                    .containsSpoiler(comment.getContainsSpoiler())
                    .createdAt(comment.getCreatedAt())
                    .user(authorDto)
                    .likedByMe(liked)
                    .replyCount(replyCount) // Nuevo campo
                    .build();
            })
            .toList();
    }    

    public List<CommentResponseDTO> getRepliesForComment(Long parentCommentId, HttpServletRequest request) {
        // Obtener usuario autenticado (si existe)
        Optional<Users> currentUserOpt = userLogicService.getUserFromRequest(request);
        Long currentUserId = currentUserOpt.map(Users::getId).orElse(null);
    
        // Obtener todas las respuestas recursivamente
        List<Comment> replies = commentService.getRepliesRecursively(parentCommentId);
    
        // Convertir a DTOs enriquecidos
        return replies.stream()
            .map((Comment comment) -> {
                // Obtener información del autor del comentario
                Users author = userService.getById(comment.getUserId()).orElse(null);
                UserPrestigeDTO prestige = userPrestigeService.getPrestigeDTOByUserId(comment.getUserId()).orElse(null);
                UserSummaryPrestigeDTO authorDto = userMapper.toSummaryPrestige(author, prestige);
    
                // Verificar si el usuario autenticado ha dado like
                boolean liked = currentUserId != null && commentLikeService.isLikedByUser(comment.getId(), currentUserId);
    
                // Obtener el nombre del usuario al que se está respondiendo (si existe)
                AtomicReference<String> replyUsername = new AtomicReference<>(null);
                AtomicReference<Long> replyUserId = new AtomicReference<>(null);
    
                if (comment.getReplyToUserId() != null) {
                    userService.getById(comment.getReplyToUserId()).ifPresent(replyUser -> {
                        replyUsername.set(replyUser.getUsername());
                        replyUserId.set(replyUser.getId());
                    });
                }
    
                // Crear el DTO final
                return CommentResponseDTO.builder()
                    .id(comment.getId())
                    .commentText(comment.getCommentText())
                    .likeCount(comment.getLikeCount())
                    .containsSpoiler(comment.getContainsSpoiler())
                    .createdAt(comment.getCreatedAt())
                    .user(authorDto)
                    .likedByMe(liked)
                    .replyToUserId(replyUserId.get())   // ✅ Obtener el valor
                    .replyToUsername(replyUsername.get()) // ✅ Obtener el valor
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

    public Optional<OperationResult> reportComment(Long commentId, HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return Optional.of(OperationResult.error("No autorizado para reportar este comentario."));
        }

        Optional<Comment> commentOpt = commentService.getById(commentId);
        if (commentOpt.isEmpty()) {
            return Optional.of(OperationResult.error("Comentario no encontrado."));
        }

        Comment comment = commentOpt.get();

        if (Boolean.TRUE.equals(comment.getIsReported())) {
            return Optional.of(OperationResult.error("Este comentario ya fue reportado."));
        }

        comment.setIsReported(true);
        commentService.saveComment(comment);

        return Optional.empty(); // éxito
    }

    public List<ReportedCommentAdminDTO> getReportedComments() {
        List<Comment> reportedComments = commentService.getReportedComments();

        return reportedComments.stream().map(comment -> {
            // Usuario que escribió el comentario
            Users user = userService.getById(comment.getUserId()).orElse(null);
            UserPrestigeDTO prestige = userPrestigeService.getPrestigeDTOByUserId(comment.getUserId()).orElse(null);
            UserSummaryPrestigeDTO userDTO = userMapper.toSummaryPrestige(user, prestige);

            // Obtener título (directamente o recursivamente si es respuesta)
            String tconst = comment.getTconst();
            if (tconst == null) {
                tconst = commentService.findRootTconst(comment.getId()).orElse(null);
            }

            String titleName = null;
            if (tconst != null) {
                titleName = titleService.getTitleById(tconst)
                    .map(Title::getPrimaryTitle)
                    .orElse(null);
            }

            // Usuario al que se responde, si aplica
            Long replyToUserId = comment.getReplyToUserId();
            String replyToUsername = null;
            if (replyToUserId != null) {
                replyToUsername = userService.getById(replyToUserId)
                    .map(Users::getUsername)
                    .orElse(null);
            }

            return ReportedCommentAdminDTO.builder()
                .id(comment.getId())
                .commentText(comment.getCommentText())
                .tconst(tconst)
                .primaryTitle(titleName)
                .user(userDTO)
                .replyToUserId(replyToUserId)
                .replyToUsername(replyToUsername)
                .build();
        }).toList();
    }

}
