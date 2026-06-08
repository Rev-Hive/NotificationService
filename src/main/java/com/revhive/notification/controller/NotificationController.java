package com.revhive.notification.controller;

import com.revhive.notification.dto.NotificationRequest;
import com.revhive.notification.model.Notification;
import com.revhive.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping
    public Notification sendNotification(
            @Valid @RequestBody
            NotificationRequest request
    ) {

        Notification notification =
                Notification.builder()
                        .userId(request.getUserId())
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .type(request.getType())
                        .build();

        return service.sendNotification(
                notification
        );
    }

    @GetMapping("/{userId}")
    public List<Notification> getNotifications(
            @PathVariable Long userId
    ) {

        return service.getUserNotifications(userId);
    }

    @GetMapping("/my-notifications")
    public List<Notification> getMyNotifications(
            @RequestHeader("X-Auth-UserId") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.getUserNotifications(userId);
    }

    @PutMapping("/{notificationId}/read")
    public Notification markAsRead(
            @PathVariable String notificationId,
            @RequestHeader("X-Auth-UserId") Long userId
    ) {
        return service.markAsRead(notificationId);
    }

    @PutMapping("/read-all")
    public void markAllAsRead(
            @RequestHeader("X-Auth-UserId") Long userId
    ) {
        service.markAllAsRead(userId);
    }
}
