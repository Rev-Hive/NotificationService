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
}