package com.cisvan.api.domain.notification.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.comment.Comment;
import com.cisvan.api.domain.comment.CommentRepository;
import com.cisvan.api.domain.notification.Notification;
import com.cisvan.api.domain.notification.NotificationRepository;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.repos.TitleRepository;
import com.cisvan.api.domain.userfollow.UserFollow;
import com.cisvan.api.domain.userfollow.UserFollowRepository;
import com.cisvan.api.domain.userlist.UserList;
import com.cisvan.api.domain.userlist.UserListRepository;
import com.cisvan.api.domain.userprestige.UserPrestige;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.UsersRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UsersRepository userRepository;
    private final TitleRepository titleRepository;
    private final NotificationMessageService notificationMessageService;
    private final UserListRepository userListRepository;
    private final JavaMailSender mailSender;
    private final CommentRepository commentRepository;
    private final UserFollowRepository userFollowRepository;

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void notifyReplyComment(Long replyToUserId, String tconst) {
        // Buscar al usuario que recibirá la notificación
        Optional<Users> userOpt = userRepository.findById(replyToUserId);
        if (userOpt.isEmpty()) {
            return;
        }
    
        Users user = userOpt.get();
    
        // Buscar el título del contenido para el mensaje
        Optional<Title> titleOpt = titleRepository.findById(tconst);
        if (titleOpt.isEmpty()) {
            return;
        }
    
        String referenceName = titleOpt.get().getPrimaryTitle();
    
        // Crear la notificación en la plataforma
        Notification notification = Notification.builder()
                .userId(replyToUserId)
                .code("NTF02")
                .referenceType("CONTENT")
                .referenceId(tconst)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    
        // Si el usuario tiene activadas las notificaciones por correo
        if (user.getEmailNotifications()) {
            String message = notificationMessageService.resolveMessage("NTF02", referenceName);
            sendSimpleEmail(user.getEmail(), "Respuesta a tu comentario", message);
        }
    }

    public void notifyLikeAccumulated(Comment comment) {
        Long userId = comment.getUserId();
        Optional<Users> userOpt = userRepository.findById(userId);
    
        if (userOpt.isEmpty()) {
            return;
        }
    
        Users user = userOpt.get();
    
        // Obtener el ID del contenido (tconst) subiendo por la jerarquía de comentarios
        Optional<String> tconstOpt = commentRepository.findRootTconst(comment.getId());
        
        if (tconstOpt.isEmpty()) {
            return;
        }
    
        String tconst = tconstOpt.get();
    
        // Buscar el título del contenido para el mensaje
        Optional<Title> titleOpt = titleRepository.findById(tconst);
        if (titleOpt.isEmpty()) {
            return;
        }
    
        String referenceName = titleOpt.get().getPrimaryTitle();
        String message = notificationMessageService.resolveMessage("NTF03", referenceName);
    
        // Guardar la notificación en la base de datos
        Notification notification = Notification.builder()
                .userId(user.getId())
                .code("NTF03")
                .referenceType("CONTENT")
                .referenceId(tconst)
                .createdAt(LocalDateTime.now())
                .build();
    
        notificationRepository.save(notification);
    
        // Si el usuario tiene habilitadas las notificaciones por correo, enviar email
        if (user.getEmailNotifications()) {
            sendSimpleEmail(user.getEmail(), "¡Tu comentario acumuló likes!", message);
        }
    }    

    @Scheduled(cron = "0 0 14 * * *") // Every day at 14:00
    @Transactional
    public void notifyUnwatchedContent() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(48);
        List<UserList> entries = userListRepository.findUnwatchedSince(cutoff);

        // Mapa para agrupar contenidos por usuario
        Map<Long, List<String>> userContentMap = new HashMap<>();

        for (UserList entry : entries) {
            Long userId = entry.getUserId();
            String titleId = entry.getTitleId();

            // Obtener el usuario y validar que tenga activadas las notificaciones
            Optional<Users> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty() || !userOpt.get().getEmailNotifications()) {
                continue;
            }

            Optional<Title> titleOpt = titleRepository.findById(titleId);
            if (titleOpt.isEmpty()) {
                continue;
            }

            String titleName = titleOpt.get().getPrimaryTitle();

            // Acumular los contenidos en el mapa del usuario
            userContentMap
                .computeIfAbsent(userId, k -> new ArrayList<>())
                .add(titleName);

            // Guardar la notificación en la base de datos
            Notification notification = Notification.builder()
                    .userId(userId)
                    .code("NTF05")
                    .referenceType("CONTENT")
                    .referenceId(titleId)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }

        // Enviar un correo por usuario con la lista acumulada
        userContentMap.forEach((userId, contentList) -> {
            Optional<Users> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                Users user = userOpt.get();
                String message = buildAccumulatedMessage(contentList);
                sendSimpleEmail(user.getEmail(), "Notificación de contenido no visto", message);
            }
        });
    }

    // Generar el mensaje acumulado con todos los títulos no vistos
    private String buildAccumulatedMessage(List<String> contentList) {
        StringBuilder message = new StringBuilder("Tienes contenido pendiente de ver:\n\n");
        contentList.forEach(title -> message.append("- ").append(title).append("\n"));
        message.append("\n¡No te lo pierdas!");
        return message.toString();
    }

    // Método para enviar el correo electrónico
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("a21100296@ceti.mx");

            mailSender.send(message);
            System.out.println("Correo enviado exitosamente a: " + to);
        } catch (Exception e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }
    }

    @Async
    public void notifyFollowersOfNewReview(Long authorUserId) {
        // Buscar a todos los seguidores del usuario
        List<UserFollow> followers = userFollowRepository.findByFollowedId(authorUserId);

        if (followers.isEmpty()) return;

        // Obtener el nombre del autor para el mensaje
        Optional<Users> authorOpt = userRepository.findById(authorUserId);
        if (authorOpt.isEmpty()) return;

        Users author = authorOpt.get();
        String referenceName = author.getUsername(); // o puedes incluir título si quieres más contexto

        for (UserFollow follow : followers) {
            Long followerId = follow.getFollowerId();

            // Crear notificación
            Notification notification = Notification.builder()
                    .userId(followerId)
                    .code("NTF06")
                    .referenceType("USER")
                    .referenceId(String.valueOf(authorUserId))
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);

            // Si el usuario quiere email
            Optional<Users> followerUserOpt = userRepository.findById(followerId);
            if (followerUserOpt.isPresent() && followerUserOpt.get().getEmailNotifications()) {
                String message = notificationMessageService.resolveMessage("NTF06", referenceName);
                sendSimpleEmail(followerUserOpt.get().getEmail(), "Nueva reseña publicada", message);
            }
        }
    }

    public void notifyPrestigeLevelUp(UserPrestige prestige) {

        Long userId = prestige.getUserId();
        Optional<Users> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return;

        Users user = userOpt.get();

        // Crear y guardar la notificación
        Notification notification = Notification.builder()
                .userId(userId)
                .code("NTF04")
                .referenceType("USER")
                .referenceId(userId.toString())
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        // Enviar email si está activado
        if (user.getEmailNotifications()) {
            String message = "¡Has subido de rango! 🎖️";
            sendSimpleEmail(user.getEmail(), "Ascenso de prestigio", message);
        }
    }

}