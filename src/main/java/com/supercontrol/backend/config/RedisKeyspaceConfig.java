package com.supercontrol.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class RedisKeyspaceConfig {

    private final RedisConnectionFactory connectionFactory;

    @PostConstruct
    public void enableKeyspaceEvents() {
        // Ex = Expired events
        connectionFactory.getConnection()
                .serverCommands()
                .setConfig("notify-keyspace-events", "Ex");
    }
}
