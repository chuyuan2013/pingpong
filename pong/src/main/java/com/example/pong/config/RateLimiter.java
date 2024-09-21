package com.example.pong.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimiter {
    private final int MAX_REQUESTS_PER_SECOND = 1;
    private static final AtomicInteger requestCounter = new AtomicInteger(0);
    public synchronized boolean allowRequest() {
        if (requestCounter.get() < MAX_REQUESTS_PER_SECOND) {
            requestCounter.incrementAndGet();
            return true;
        }else{
            log.info("Pong service rate limited");
        }
        return false;
    }

    @Scheduled(fixedRate = 1000) // 每秒重置
    public synchronized void resetCount() {
        if(requestCounter.get() >= 1 ){
            requestCounter.set(0);
        }
    }
}
