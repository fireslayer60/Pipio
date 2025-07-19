package com.pipio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RedisMessageListenerContainer redisMessageListenerContainer;

    public WebSocketConfig(RedisMessageListenerContainer redisMessageListenerContainer) {
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        JobLogWebSocketHandler jobLogWebSocketHandler = jobLogWebSocketHandler();
        registry.addHandler(jobLogWebSocketHandler, "/ws/logs/**")
                .setAllowedOrigins("*");

        // Subscribe the handler directly to the Redis channel pattern
        redisMessageListenerContainer.addMessageListener(jobLogWebSocketHandler, new PatternTopic("job-logs:*"));
    }

    @Bean
    public JobLogWebSocketHandler jobLogWebSocketHandler() {
        return new JobLogWebSocketHandler();
    }
}
