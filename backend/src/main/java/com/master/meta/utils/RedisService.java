package com.master.meta.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;

    public void store(String name, String token, long expiryInSeconds) {
        redisTemplate.opsForValue().set("auth:refresh_token:" + name, token, Duration.ofSeconds(expiryInSeconds));
    }

    public String get(String name) {
        return redisTemplate.opsForValue().get("auth:refresh_token:" + name);
    }

    public void delete(String name) {
        redisTemplate.delete("auth:refresh_token:" + name);
    }
}
