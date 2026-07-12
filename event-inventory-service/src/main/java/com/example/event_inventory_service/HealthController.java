package com.example.event_inventory_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/ping")
    public String ping() {
        return "Event-Inventory is up!";
    }

    @GetMapping("/redis-ping")
    public String redisPing() {
        try {
            redisTemplate.opsForValue().set("health", "OK");
            return redisTemplate.opsForValue().get("health");
        } catch (Exception e) {
            return "Redis error: " + e.getMessage();
        }
    }
}