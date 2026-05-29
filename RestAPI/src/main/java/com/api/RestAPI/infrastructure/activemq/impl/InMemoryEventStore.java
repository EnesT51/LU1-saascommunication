package com.api.RestAPI.infrastructure.activemq.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.api.RestAPI.domain.notification.Interface.EventStore;

@Component
public class InMemoryEventStore implements EventStore {
    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void store(String payload) {
        messages.add(payload);
    }

    @Override
    public List<String> getAllMessages() {
        synchronized (messages) {
            return new ArrayList<>(messages);
        }
    }
}
