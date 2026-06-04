package com.revhive.notification.service;

import com.revhive.notification.client.SocialServiceClient;
import com.revhive.notification.model.ChatMessage;
import com.revhive.notification.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final SocialServiceClient socialServiceClient;

    public ChatMessage saveMessage(ChatMessage message) {
        // Validate if sender follows receiver
        if (!canMessage(message.getSenderId(), message.getReceiverId())) {
            log.warn("Blocked message send from user {} to user {}: follow relation missing", 
                    message.getSenderId(), message.getReceiverId());
            throw new IllegalArgumentException("You can only message users you follow.");
        }
        message.setTimestamp(LocalDateTime.now());
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getChatHistory(Long user1, Long user2) {
        return chatMessageRepository.findChatHistory(user1, user2);
    }

    public boolean canMessage(Long senderId, Long receiverId) {
        try {
            Map<String, Object> response = socialServiceClient.isFollowing(senderId, receiverId);
            if (response != null && response.get("isFollowing") != null) {
                return (Boolean) response.get("isFollowing");
            }
        } catch (Exception e) {
            log.error("Failed to check follow status between sender {} and receiver {}: {}", 
                    senderId, receiverId, e.getMessage());
        }
        // Fail secure by default
        return false;
    }
}
