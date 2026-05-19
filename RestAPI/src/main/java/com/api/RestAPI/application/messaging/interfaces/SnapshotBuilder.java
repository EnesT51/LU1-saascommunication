package com.api.RestAPI.application.messaging.interfaces;

import java.util.Map;

import com.api.RestAPI.application.messaging.interfaces.MetricsCollector;
import com.api.RestAPI.domain.messaging.Interface.EventStore;

public interface SnapshotBuilder {
    Map<String, Object> build(String queueName, MetricsCollector metrics, EventStore events);
}
