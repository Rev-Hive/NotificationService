package com.revhive.notification.service;

import com.revhive.notification.client.SocialServiceClient;
import com.revhive.notification.client.UserServiceClient;
import com.revhive.notification.dto.ConversationDTO;
import com.revhive.notification.model.ChatMessage;
import com.revhive.notification.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final SocialServiceClient socialServiceClient;
    private final UserServiceClient userServiceClient;

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

    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversations(Long userId) {
        List<ChatMessage> allMessages = chatMessageRepository.findAllMessagesForUser(userId);
        
        // Group by other user ID
        Map<Long, List<ChatMessage>> messagesByOtherUser = new HashMap<>();
        for (ChatMessage msg : allMessages) {
            Long otherUserId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            messagesByOtherUser.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(msg);
        }
        
        List<ConversationDTO> conversations = new ArrayList<>();
        for (Map.Entry<Long, List<ChatMessage>> entry : messagesByOtherUser.entrySet()) {
            Long otherUserId = entry.getKey();
            List<ChatMessage> msgs = entry.getValue();
            
            // Find latest message
            ChatMessage latestMsg = msgs.stream()
                    .max(Comparator.comparing(ChatMessage::getTimestamp))
                    .orElse(null);
            
            if (latestMsg == null) continue;
            
            // Calculate unread count (incoming to userId that are not read)
            long unreadCount = msgs.stream()
                    .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
                    .count();
            
            String otherUsername = latestMsg.getSenderId().equals(userId) ? 
                    latestMsg.getReceiverUsername() : latestMsg.getSenderUsername();
            
            // Fetch profile details from user-service
            String avatarUrl = null;
            try {
                Map<String, Object> summary = userServiceClient.getUserSummary(otherUserId);
                if (summary != null) {
                    avatarUrl = (String) summary.get("avatarUrl");
                }
            } catch (Exception e) {
                log.error("Failed to fetch user summary for user {}: {}", otherUserId, e.getMessage());
            }
            
            conversations.add(ConversationDTO.builder()
                    .userId(otherUserId)
                    .username(otherUsername)
                    .avatarUrl(avatarUrl)
                    .lastMessage(latestMsg.getContent())
                    .lastMessageTimestamp(latestMsg.getTimestamp())
                    .unreadCount((int) unreadCount)
                    .online(false)
                    .build());
        }
        
        // Sort by lastMessageTimestamp descending
        conversations.sort(Comparator.comparing(ConversationDTO::getLastMessageTimestamp, 
                Comparator.nullsLast(Comparator.reverseOrder())));
        
        return conversations;
    }

    @Transactional
    public void markMessagesAsRead(Long senderId, Long receiverId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessages(senderId, receiverId);
        if (!unreadMessages.isEmpty()) {
            for (ChatMessage msg : unreadMessages) {
                msg.setRead(true);
            }
            chatMessageRepository.saveAll(unreadMessages);
            log.info("Marked {} messages from sender {} to receiver {} as read", 
                    unreadMessages.size(), senderId, receiverId);
        }
    }
}
