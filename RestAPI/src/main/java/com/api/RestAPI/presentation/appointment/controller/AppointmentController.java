package com.api.RestAPI.presentation.appointment.controller;

import java.util.List;

import com.api.RestAPI.application.appointment.dto.AppointmentResponseDto;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentEventProcessor;
// import com.api.RestAPI.application.messaging.AppointmentEventStore;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentMapper;
import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;

import org.hl7.fhir.r4.model.Appointment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentMapper; 


@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    private final IAppointmentEventProcessor appointmentService;
    private final IAppointmentMapper mapper;
    // private final AppointmentEventStore eventStore;

    public AppointmentController(IAppointmentEventProcessor appointmentService, IAppointmentMapper mapper) {
        this.appointmentService = appointmentService;
        this.mapper = mapper;
        // this.eventStore = eventStore;
    }

    // @GetMapping
    // public ResponseEntity<List<AppointmentEntity>> getAppointments() {
    //     return ResponseEntity.ok(appointmentService.getAppointments());
    // }

    @PostMapping("/save")
    public ResponseEntity<AppointmentResponseDto> saveAppointment(@RequestBody String fhirJson) {
        AppointmentEntity response = appointmentService.processAppointmentEvent(fhirJson);
        AppointmentResponseDto mappedAppointment = mapper.toDto(response);

        HttpStatus status = response.isNewlyCreated() ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(mappedAppointment);
    }
}
