package com.tenacy.snaplink.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("test")
public class CacheInitializer {
    private final RedisConnectionFactory redisConnectionFactory;

    @PostConstruct
    public void clearCache() {
        RedisConnection connection = redisConnectionFactory.getConnection();
        connection.flushDb();
        connection.close();
    }
}