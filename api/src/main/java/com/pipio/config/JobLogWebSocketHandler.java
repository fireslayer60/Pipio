package com.pipio.config;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class JobLogWebSocketHandler extends TextWebSocketHandler {

    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final RedisLogSubscriber redisSubscriber;

    public JobLogWebSocketHandler(RedisLogSubscriber redisSubscriber) {
        this.redisSubscriber = redisSubscriber;
    }

   @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String uri = session.getUri().toString();
        log.info("New WebSocket connection: {}", uri);

        String jobId = null;

        // Defensive parsing
        if (uri.contains("/ws/logs/")) {
            String[] parts = uri.split("/ws/logs/");
            if (parts.length > 1 && !parts[1].isBlank()) {
                jobId = parts[1];
            }
        }

        if (jobId == null) {
            log.warn("Invalid WebSocket URI: {}", uri);
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        log.info("Registering session for jobId: {}", jobId);
        sessions.put(jobId, session);
        redisSubscriber.subscribe(jobId, sessions);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().remove(session);
    }
}
