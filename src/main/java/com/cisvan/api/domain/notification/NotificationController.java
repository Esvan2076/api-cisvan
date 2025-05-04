package com.cisvan.api.domain.notification;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationOrchestrator notificationOrchestrator;

    @GetMapping
    public ResponseEntity<?> getMyNotifications(HttpServletRequest request) {
        return ResponseEntity.ok(notificationOrchestrator.getNotificationsByUserId(request));
    }
}