package com.mpanov.diploma.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final StringRedisTemplate redisTemplate;

    public void cacheWithTTL(String key, String value) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(1));
    }

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
