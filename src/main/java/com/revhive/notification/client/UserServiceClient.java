package com.revhive.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "user-service",
        url = "${app.services.user-service:http://localhost:8081}"
)
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}/summary")
    Map<String, Object> getUserSummary(@PathVariable("userId") Long userId);
}
