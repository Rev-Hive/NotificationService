package com.revhive.notification.service;

import com.revhive.notification.model.Notification;
import com.revhive.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService service;

    private Notification notification;

    @BeforeEach
    public void setUp() {
        notification = Notification.builder()
                .id("test-uuid-123")
                .userId(1L)
                .title("New Like")
                .message("@user liked your post")
                .type("LIKE")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    public void testSendNotification() {
        when(repository.save(any(Notification.class))).thenReturn(notification);

        Notification result = service.sendNotification(notification);

        assertNotNull(result);
        assertEquals("test-uuid-123", result.getId());
        verify(repository, times(1)).save(notification);
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/notifications/1"),
                eq(notification)
        );
    }

    @Test
    public void testGetUserNotifications_Sorted() {
        List<Notification> expectedList = Arrays.asList(notification);
        when(repository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(expectedList);

        List<Notification> result = service.getUserNotifications(1L);

        assertEquals(expectedList, result);
        verify(repository, times(1)).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    public void testMarkAsRead() {
        when(repository.findById("test-uuid-123")).thenReturn(Optional.of(notification));
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = service.markAsRead("test-uuid-123");

        assertTrue(result.isRead());
        verify(repository, times(1)).save(notification);
    }

    @Test
    public void testMarkAsRead_NotFound() {
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.markAsRead("non-existent");
        });
    }

    @Test
    public void testMarkAllAsRead() {
        Notification n1 = Notification.builder().id("id1").read(false).build();
        Notification n2 = Notification.builder().id("id2").read(false).build();
        when(repository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(n1, n2));

        service.markAllAsRead(1L);

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(repository, times(2)).save(any(Notification.class));
    }
}
