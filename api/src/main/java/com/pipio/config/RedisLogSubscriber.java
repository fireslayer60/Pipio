package com.pipio.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.*;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.*;

@Service
public class RedisLogSubscriber {

    @Autowired
    private RedisMessageListenerContainer container;

    public void subscribe(String jobId, ConcurrentMap<String, WebSocketSession> sessions) {
        String channel = "job-logs:" + jobId;

        container.addMessageListener((MessageListener) (message, pattern) -> {
            String logLine = new String(message.getBody());
            WebSocketSession session = sessions.get(jobId);
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(logLine));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new ChannelTopic(channel));
    }
}
