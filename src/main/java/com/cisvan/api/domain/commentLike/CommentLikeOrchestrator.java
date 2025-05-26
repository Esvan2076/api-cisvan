package com.cisvan.api.domain.commentLike;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.comment.Comment;
import com.cisvan.api.domain.comment.CommentRepository;
import com.cisvan.api.domain.notification.services.NotificationService;
import com.cisvan.api.domain.userprestige.UserPrestigeService;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.services.UserLogicService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentLikeOrchestrator {

    private final CommentLikeService commentLikeService;
    private final UserLogicService userLogicService;
    private final CommentRepository commentRepository;
    private final UserPrestigeService userPrestigeService;
    private final NotificationService notificationService;

    public boolean toggleLikeComment(Long commentId, HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return false;
        }
    
        Users user = userOpt.get();
        boolean isLiked = commentLikeService.toggleLike(commentId, user.getId());
    
        // Verificar si se debe enviar notificación de likes acumulados
        if (isLiked) {
            checkAndNotifyLikeAccumulation(commentId);
        }

        commentRepository.findById(commentId).ifPresent(comment -> {
            userPrestigeService.checkIfUserShouldReevaluatePrestige(comment.getUserId());
        });
        
        return isLiked;
    }

    @Transactional
    public void checkAndNotifyLikeAccumulation(Long commentId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);

        if (commentOpt.isEmpty()) {
            return;
        }

        Comment comment = commentOpt.get();
        int currentLikes = comment.getLikeCount();
        int lastNotifiedLikes = comment.getLastNotifiedLikes();

        // Verificar si el bloque de 10 likes adicionales ha sido alcanzado
        int newLikes = currentLikes - lastNotifiedLikes;

        if (newLikes >= 10) {
            // Actualizar el contador de la última notificación
            comment.setLastNotifiedLikes(currentLikes);
            commentRepository.save(comment);

            // Notificar al usuario del comentario
            notificationService.notifyLikeAccumulated(comment);
        }
    }

}