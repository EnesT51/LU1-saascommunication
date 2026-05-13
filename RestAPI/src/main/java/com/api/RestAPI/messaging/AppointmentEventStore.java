package com.api.RestAPI.messaging;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventStore {

    private final AtomicInteger receivedCount = new AtomicInteger();
    private final String queueName;
    private final List<String> messages = Collections.synchronizedList(new ArrayList<String>());

    private volatile String lastMessage = "No message received yet";
    private volatile Instant lastReceivedAt;

    public AppointmentEventStore(@Value("${app.queue.name}") String queueName) {
        this.queueName = queueName;
    }

    public void store(String payload) {
        messages.add(payload);
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
        synchronized (messages) {
            status.put("messages", new ArrayList<String>(messages));
        }
        return status;
    }
}
