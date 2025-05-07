package com.cisvan.api.domain.notification.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.notification.Notification;
import com.cisvan.api.domain.notification.NotificationRepository;
import com.cisvan.api.domain.userlist.UserList;
import com.cisvan.api.domain.userlist.UserListRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserListRepository userListRepository;

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Scheduled(cron = "0 0 14 * * *") // Every day at 14:00
    @Transactional
    public void notifyUnwatchedContent() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(48);
        List<UserList> entries = userListRepository.findUnwatchedSince(cutoff);

        for (UserList entry : entries) {
            Notification notification = Notification.builder()
                    .userId(entry.getUserId())
                    .code("NTF05")
                    .referenceType("CONTENT")
                    .referenceId(entry.getTitleId()) // e.g., "tt1234567"
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
    }
}