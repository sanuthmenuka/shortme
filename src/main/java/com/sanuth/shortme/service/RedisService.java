package com.sanuth.shortme.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Service
public class RedisService {
    private static final Logger log = LoggerFactory.getLogger(RedisService.class);
    private final RedisTemplate<String, String> stringRedisTemplate;

    public RedisService(RedisTemplate<String, String> stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void incrementClick(String code) {
        String totalKey = "clicks:code:" + code + ":total";
        String dailyKey = "clicks:code:" + code + ":day:" + LocalDate.now();

        try {
            stringRedisTemplate.opsForValue().increment(totalKey);
            stringRedisTemplate.opsForValue().increment(dailyKey);
            stringRedisTemplate.expire(dailyKey, Duration.ofDays(30));
            log.debug("Incremented click for code: {}", code);
        } catch (Exception e) {
            log.warn("Failed to increment click for code: {} - {}", code, e.getMessage());
        }
    }
}
