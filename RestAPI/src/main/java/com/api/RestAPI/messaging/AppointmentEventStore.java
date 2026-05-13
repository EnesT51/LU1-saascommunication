package com.api.RestAPI.messaging;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventStore {

    private final AtomicInteger receivedCount = new AtomicInteger();
    private final String queueName;

    private volatile String lastMessage = "No message received yet";
    private volatile Instant lastReceivedAt;

    public AppointmentEventStore(@Value("${app.queue.name}") String queueName) {
        this.queueName = queueName;
    }

    public void store(String payload) {
        lastMessage = payload;
        lastReceivedAt = Instant.now();
        receivedCount.incrementAndGet();
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("queue", queueName);
        status.put("receivedCount", receivedCount.get());
        status.put("lastReceivedAt", lastReceivedAt);
        status.put("lastMessage", lastMessage);
        return status;
    }
}
