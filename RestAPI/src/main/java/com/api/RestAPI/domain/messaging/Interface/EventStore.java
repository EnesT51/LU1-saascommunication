package com.api.RestAPI.domain.messaging.Interface;

import java.util.List;

public interface EventStore {
    void store(String payload);
    List<String> getAllMessages();
}
