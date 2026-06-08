package com.revhive.notification.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Long userId;

    private String title;

    private String type;

    @Column(length = 1000)
    private String message;

    @Column(name = "is_read")
    private boolean read;

    private LocalDateTime createdAt;
}