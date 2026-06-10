package com.revhive.notification.service;

import com.revhive.notification.model.Notification;
import com.revhive.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    private final SimpMessagingTemplate messagingTemplate;

    public Notification sendNotification(
            Notification notification
    ) {

        notification.setCreatedAt(LocalDateTime.now());

        notification.setRead(false);

        Notification saved =
                repository.save(notification);

        messagingTemplate.convertAndSend(
                "/topic/notifications/" +
                        notification.getUserId(),
                saved
        );

        return saved;
    }

    public List<Notification> getUserNotifications(
            Long userId
    ) {

        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(String id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        notification.setRead(true);
        return repository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> notifications = repository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : notifications) {
            if (!n.isRead()) {
                n.setRead(true);
                repository.save(n);
            }
        }
    }
}
