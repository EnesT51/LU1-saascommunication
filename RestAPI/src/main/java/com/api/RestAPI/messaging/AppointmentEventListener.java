package com.api.RestAPI.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventListener {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventListener.class);

    private final AppointmentEventStore eventStore;

    public AppointmentEventListener(AppointmentEventStore eventStore) {
        this.eventStore = eventStore;
    }

    @JmsListener(destination = "${app.queue.name}")
    public void receive(String payload) {
        eventStore.store(payload);
        log.info("Received appointment event from ActiveMQ: {}", payload);
    }
}
