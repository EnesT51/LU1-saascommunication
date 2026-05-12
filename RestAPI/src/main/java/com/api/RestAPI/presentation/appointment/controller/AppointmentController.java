package com.api.RestAPI.presentation.appointment.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.api.RestAPI.application.appointment.service.AppointmentService;
import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.api.RestAPI.application.appointment.dto.AppointmentDto;



@RestController
@RequestMapping("/appointment")
public class AppointmentController {
    

    private final AppointmentService appointmentService;

    private AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<AppointmentEntity>> getAppointments(){
        return ResponseEntity.ok(appointmentService.getAppointments());
    }
    @PostMapping("/receive")
    public ResponseEntity<String> receiveAppointment(@RequestBody String fhirJson)
    {
        appointmentService.receiveAppointment(fhirJson);
        return ResponseEntity.ok("Appointment received successfully");
    }
}
