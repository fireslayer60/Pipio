package com.pipio.service;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;


@Service
public class LogPublisher {

    private  final String LOG_CHANNEL_PREFIX = "job-logs:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void publishLog(String jobId, String logLine) {
        String channel = LOG_CHANNEL_PREFIX + jobId;
        redisTemplate.convertAndSend(channel, logLine);
    }
}