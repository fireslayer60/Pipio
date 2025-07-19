package com.pipio.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class JobLogWebSocketHandler extends TextWebSocketHandler implements MessageListener {

    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final String JOB_ID_PREFIX = "/ws/logs/";
    private static final String REDIS_CHANNEL_PREFIX = "job-logs:";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        final String jobId = getJobIdFromSession(session);
        if (jobId == null) {
            session.close(CloseStatus.BAD_DATA.withReason("Invalid URI: Missing Job ID"));
            return;
        }
        log.info("WebSocket connection established for Job ID: {}", jobId);
        sessions.put(jobId, session);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        final String channel = new String(message.getChannel());
        final String logLine = new String(message.getBody());
        final String jobId = channel.substring(REDIS_CHANNEL_PREFIX.length());

        WebSocketSession session = sessions.get(jobId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(logLine));
            } catch (IOException e) {
                log.error("Failed to send message to WebSocket for Job ID: {}", jobId, e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        final String jobId = getJobIdFromSession(session);
        if (jobId != null) {
            sessions.remove(jobId);
            log.info("WebSocket connection closed for Job ID: {}. Status: {}", jobId, status);
        }
    }

    private String getJobIdFromSession(WebSocketSession session) {
        if (session.getUri() == null) {
            return null;
        }
        String path = session.getUri().getPath();
        int index = path.indexOf(JOB_ID_PREFIX);
        if (index != -1) {
            return path.substring(index + JOB_ID_PREFIX.length());
        }
        return null;
    }
}
