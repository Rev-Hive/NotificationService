package com.revhive.notification.controller;

import com.revhive.notification.model.ChatMessage;
import com.revhive.notification.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage message) {
        log.info("WebSocket chat message received from senderId {} to receiverId {}", 
                message.getSenderId(), message.getReceiverId());
        try {
            ChatMessage saved = chatService.saveMessage(message);
            // Broadcast to the public topic; clients will receive and filter locally
            messagingTemplate.convertAndSend("/topic/messages", saved);
        } catch (Exception e) {
            log.error("Failed to process and save chat message: {}", e.getMessage());
            // Fail silently or handle feedback to client
        }
    }

    @GetMapping("/api/chat/history")
    public List<ChatMessage> getChatHistory(
            @RequestParam Long senderId,
            @RequestParam Long receiverId
    ) {
        log.info("REST chat history request for user {} and user {}", senderId, receiverId);
        return chatService.getChatHistory(senderId, receiverId);
    }
}
