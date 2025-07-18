// WebSocketConfig.java
package com.pipio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RedisLogSubscriber redisLogSubscriber;

    public WebSocketConfig(RedisLogSubscriber redisLogSubscriber) {
        this.redisLogSubscriber = redisLogSubscriber;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(jobLogWebSocketHandler(), "/ws/logs/**")
                .setAllowedOrigins("*");
    }

    @Bean
    public JobLogWebSocketHandler jobLogWebSocketHandler() {
        return new JobLogWebSocketHandler(redisLogSubscriber);
    }
}
