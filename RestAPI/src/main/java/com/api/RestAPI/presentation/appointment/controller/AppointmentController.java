package com.api.RestAPI.presentation.appointment.controller;

import java.util.List;

import com.api.RestAPI.application.appointment.interfaces.IAppointmentEventProcessor;
// import com.api.RestAPI.application.messaging.AppointmentEventStore;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    private final IAppointmentEventProcessor appointmentService;
    // private final AppointmentEventStore eventStore;

    public AppointmentController(IAppointmentEventProcessor appointmentService) {
        this.appointmentService = appointmentService;
        // this.eventStore = eventStore;
    }

    // @GetMapping
    // public ResponseEntity<List<AppointmentEntity>> getAppointments() {
    //     return ResponseEntity.ok(appointmentService.getAppointments());
    // }

    @PostMapping("/save")
    public ResponseEntity<String> saveAppointment(@RequestBody String fhirJson) {
        String response = appointmentService.processAppointmentEvent(fhirJson);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
