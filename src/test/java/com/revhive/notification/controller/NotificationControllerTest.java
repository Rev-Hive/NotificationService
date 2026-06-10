package com.revhive.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revhive.notification.dto.NotificationRequest;
import com.revhive.notification.model.Notification;
import com.revhive.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService service;

    @InjectMocks
    private NotificationController notificationController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
    }

    @Test
    public void testSendNotification() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setTitle("New Like");
        request.setMessage("User liked your post");
        request.setType("LIKE");

        Notification expected = Notification.builder()
                .id("test-uuid")
                .userId(1L)
                .title("New Like")
                .message("User liked your post")
                .type("LIKE")
                .read(false)
                .build();

        when(service.sendNotification(any(Notification.class))).thenReturn(expected);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-uuid"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.title").value("New Like"));
    }

    @Test
    public void testGetNotifications() throws Exception {
        Notification n = Notification.builder().id("uuid1").userId(1L).build();
        when(service.getUserNotifications(1L)).thenReturn(Arrays.asList(n));

        mockMvc.perform(get("/api/notifications/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("uuid1"));
    }

    @Test
    public void testGetMyNotifications() throws Exception {
        Notification n = Notification.builder().id("uuid2").userId(1L).build();
        when(service.getUserNotifications(1L)).thenReturn(Arrays.asList(n));

        mockMvc.perform(get("/api/notifications/my-notifications")
                        .header("X-Auth-UserId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("uuid2"));
    }

    @Test
    public void testMarkAsRead() throws Exception {
        Notification expected = Notification.builder().id("uuid3").read(true).build();
        when(service.markAsRead("uuid3")).thenReturn(expected);

        mockMvc.perform(put("/api/notifications/uuid3/read")
                        .header("X-Auth-UserId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("uuid3"))
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    public void testMarkAllAsRead() throws Exception {
        mockMvc.perform(put("/api/notifications/read-all")
                        .header("X-Auth-UserId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).markAllAsRead(1L);
    }
}
