package com.revhive.notification.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationSocketHandler {

    public void connected(String sessionId) {

        log.info(
                "WebSocket Connected : {}",
                sessionId
        );
    }

    public void disconnected(String sessionId) {

        log.info(
                "WebSocket Disconnected : {}",
                sessionId
        );
    }
}