package com.cisvan.api.domain.notification.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.notification.Notification;
import com.cisvan.api.domain.notification.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}