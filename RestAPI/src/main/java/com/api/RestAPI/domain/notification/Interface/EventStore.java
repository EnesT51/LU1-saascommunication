package com.api.RestAPI.domain.notification.Interface;

import java.util.List;

public interface EventStore {
    void store(String payload);
    List<String> getAllMessages();
}
