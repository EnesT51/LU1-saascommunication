package com.api.RestAPI.application.messaging.interfaces;

import java.time.Instant;

public interface MetricsCollector {
    void recordEvent(String message, Instant timestamp);
    int getTotalCount();
    Instant getLastTimestamp();
    String getLastMessage();
}
