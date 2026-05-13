package com.api.RestAPI.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.RestAPI.messaging.AppointmentEventStore;

@RestController
@RequestMapping("/api/messages/openmrs-appointments")
public class AppointmentEventController {

    private final AppointmentEventStore eventStore;

    public AppointmentEventController(AppointmentEventStore eventStore) {
        this.eventStore = eventStore;
    }

    @GetMapping("/latest")
    public Map<String, Object> latestMessage() {
        return eventStore.snapshot();
    }
}
