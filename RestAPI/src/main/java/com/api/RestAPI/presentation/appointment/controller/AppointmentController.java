package com.api.RestAPI.presentation.appointment.controller;

import com.api.RestAPI.application.appointment.dto.AppointmentResponseDto;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentEventProcessor;
// import com.api.RestAPI.application.messaging.AppointmentEventStore;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    private final IAppointmentEventProcessor appointmentService;

    public AppointmentController(IAppointmentEventProcessor appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/save")
    public ResponseEntity<AppointmentResponseDto> saveAppointment(@RequestBody String fhirJson) {
        AppointmentResponseDto response = appointmentService.processAppointmentEvent(fhirJson);
        
        HttpStatus status = response.getNewlyCreated() ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(response);
    }
}
