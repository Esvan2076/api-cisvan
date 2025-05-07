package com.cisvan.api.domain.notification;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.notification.dtos.NotificationDTO;
import com.cisvan.api.domain.notification.services.NotificationMessageService;
import com.cisvan.api.domain.notification.services.NotificationService;
import com.cisvan.api.domain.title.services.TitleService;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.services.UserLogicService;
import com.cisvan.api.domain.users.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationOrchestrator {

    private final NotificationService notificationService;
    private final UserLogicService userLogicService;
    private final UserService userService;
    private final TitleService titleService;
    private final NotificationMessageService notificationMessageService;

    public List<NotificationDTO> getNotificationsByUserId(HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return List.of();
        }
    
        Users user = userOpt.get();
        List<Notification> notifications = notificationService.getNotificationsForUser(user.getId());
    
        return notifications.stream()
            .map((Notification notification) -> {
                AtomicReference<String> referenceName = new AtomicReference<>(null);
    
                String refType = notification.getReferenceType();
                Object refId = notification.getReferenceId();

                if ("USER".equalsIgnoreCase(refType) && refId != null) {
                    try {
                        Long userId = Long.parseLong(refId.toString());
                        userService.getById(userId).ifPresent(u -> referenceName.set(u.getUsername()));
                    } catch (NumberFormatException ignored) {}
                }
    
                if ("CONTENT".equalsIgnoreCase(refType) && refId != null) {
                    titleService.getTitleById(refId.toString()).ifPresent(t -> referenceName.set(t.getPrimaryTitle()));
                }
    
                // 🔥 Usar servicio para generar el mensaje final
                String message = notificationMessageService.resolveMessage(notification.getCode(), referenceName.get());
    
                return NotificationDTO.builder()
                        .id(notification.getId())
                        .message(message)
                        .referenceType(refType)
                        .referenceId(refId)
                        .createdAt(notification.getCreatedAt())
                        .build();
            }).toList();
    }    
    
}
