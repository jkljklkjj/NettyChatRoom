package com.example.service.redis;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void set(String key, String value) {
        Objects.requireNonNull(key, "Key cannot be null");
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long timeout, TimeUnit unit) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(unit, "TimeUnit cannot be null");
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void set(String key, String value, long timeout) {
        Objects.requireNonNull(key, "Key cannot be null");
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
    }

    public String get(String key) {
        Objects.requireNonNull(key, "Key cannot be null");
        return redisTemplate.opsForValue().get(key);
    }

    public boolean exists(String key) {
        Objects.requireNonNull(key, "Key cannot be null");
        return redisTemplate.hasKey(key);
    }

    public void del(String key) {
        Objects.requireNonNull(key, "Key cannot be null");
        redisTemplate.delete(key);
    }

    /**
     * 设置位图的值
     * @param key 位图对应键
     * @param offset 位图偏移量
     * @param value 位图值
     */
    public void setBit(String key, long offset, boolean value) {
        Objects.requireNonNull(key, "Key cannot be null");
        redisTemplate.opsForValue().setBit(key, offset, value);
    }

    public boolean getBit(String key, long offset) {
        Objects.requireNonNull(key, "Key cannot be null");
        Boolean result = redisTemplate.opsForValue().getBit(key, offset);
        return result != null && result;
    }
}
