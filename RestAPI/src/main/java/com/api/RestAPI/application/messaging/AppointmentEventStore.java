package com.api.RestAPI.application.messaging;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.api.RestAPI.application.messaging.interfaces.MetricsCollector;
import com.api.RestAPI.application.messaging.interfaces.SnapshotBuilder;
import com.api.RestAPI.domain.messaging.Interface.EventStore;

@Component
public class AppointmentEventStore {

    private final EventStore eventStore;
    private final MetricsCollector metricsCollector;
    private final SnapshotBuilder snapshotBuilder;
    private final String queueName;

    public AppointmentEventStore(EventStore eventStore, MetricsCollector metricsCollector,
            SnapshotBuilder snapshotBuilder, @Value("${app.queue.name}") String queueName) {
        this.eventStore = eventStore;
        this.metricsCollector = metricsCollector;
        this.snapshotBuilder = snapshotBuilder;
        this.queueName = queueName;
    }

    public void store(String payload) {
        eventStore.store(payload);
        metricsCollector.recordEvent(payload, Instant.now());
    }

    public Map<String, Object> snapshot() {
        return snapshotBuilder.build(queueName, metricsCollector, eventStore);
    }
}
