package com.api.RestAPI.presentation.appointment.controller;

import java.util.List;
import java.util.Map;

import com.api.RestAPI.application.appointment.service.AppointmentService;
// import com.api.RestAPI.application.messaging.AppointmentEventStore;
import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;
    // private final AppointmentEventStore eventStore;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
        // this.eventStore = eventStore;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentEntity>> getAppointments() {
        return ResponseEntity.ok(appointmentService.getAppointments());
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveAppointment(@RequestBody String fhirJson) {
        appointmentService.saveAppointment(fhirJson);
        return ResponseEntity.ok("Appointment saved successfully");
    }

    // @GetMapping("/events")
    // public ResponseEntity<Map<String, Object>> getEventLog() {
    //     return ResponseEntity.ok(eventStore.snapshot());
    // }
}
