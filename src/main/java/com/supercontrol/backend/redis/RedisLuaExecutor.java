package com.supercontrol.backend.redis;

import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import com.supercontrol.backend.queue.QueueKey;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisLuaExecutor {

    private final RedisTemplate<String, String> redisTemplate;

    public String executeGameStart(
            String startToken,
            String userId,
            Long machineId) {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptSource(
                new ResourceScriptSource(
                        new ClassPathResource("lua/game_start.lua")));
        script.setResultType(String.class);

        String tokenKey = "startToken:" + startToken;
        String queueKey = QueueKey.queueZset(machineId);
        String readyKey = "queue:ready:" + machineId;

        System.out.println("[SuperCon Log] Lua ARGV:");
        System.out.println("[SuperCon Log] userId = " + userId);
        System.out.println("[SuperCon Log] machineId = " + machineId);
        System.out.println("[SuperCon Log] startToken = " + startToken);

        String result = redisTemplate.execute(
                script,
                List.of(tokenKey, queueKey, readyKey),
                userId,
                machineId.toString());

        return result;

    }
}
