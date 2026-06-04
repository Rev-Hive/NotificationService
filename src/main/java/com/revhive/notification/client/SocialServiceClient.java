package com.revhive.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "social-service",
        url = "${app.services.social-service:http://localhost:8084}"
)
public interface SocialServiceClient {

    @GetMapping("/api/v1/follows/check")
    Map<String, Object> isFollowing(
            @RequestParam("followerId") Long followerId,
            @RequestParam("followingId") Long followingId
    );
}
