package com.revhive.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationDTO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String lastMessage;
    private LocalDateTime lastMessageTimestamp;
    private int unreadCount;
    private boolean online;
}
