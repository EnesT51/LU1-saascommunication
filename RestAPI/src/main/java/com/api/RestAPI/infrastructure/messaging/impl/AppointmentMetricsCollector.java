package com.api.RestAPI.infrastructure.messaging.impl;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.api.RestAPI.application.messaging.interfaces.MetricsCollector;

@Component
public class AppointmentMetricsCollector implements MetricsCollector {
    private final AtomicInteger receivedCount = new AtomicInteger();
    private volatile String lastMessage = "No message received yet";
    private volatile Instant lastReceivedAt;

    @Override
    public void recordEvent(String message, Instant timestamp) {
        lastMessage = message;
        lastReceivedAt = timestamp;
        receivedCount.incrementAndGet();
    }

    @Override
    public int getTotalCount() {
        return receivedCount.get();
    }

    @Override
    public Instant getLastTimestamp() {
        return lastReceivedAt;
    }

    @Override
    public String getLastMessage() {
        return lastMessage;
    }
}
