package com.api.RestAPI.infrastructure.activemq.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.api.RestAPI.application.notification.interfaces.MetricsCollector;
import com.api.RestAPI.application.notification.interfaces.SnapshotBuilder;
import com.api.RestAPI.domain.notification.Interface.EventStore;

@Component
public class DefaultSnapshotBuilder implements SnapshotBuilder {

    @Override
    public Map<String, Object> build(String queueName, MetricsCollector metrics, EventStore events) {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("queue", queueName);
        status.put("receivedCount", metrics.getTotalCount());
        status.put("lastReceivedAt", metrics.getLastTimestamp());
        status.put("lastMessage", metrics.getLastMessage());
        status.put("messages", events.getAllMessages());
        return status;
    }
}
