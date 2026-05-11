package com.api.RestAPI.presentation.appointment.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.api.RestAPI.application.appointment.service.AppointmentService;
import com.api.RestAPI.domain.appointment.entities.Appointment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    

    private AppointmentService appointmentService;

    private AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/getAll")
    public List<Appointment> getAppointments(){
        return appointmentService.getAppointments();
    }

    @GetMapping("/getAppointmentsByStartTimeBetween")
    public List<Appointment> getAppointmentsByStartTimeBetween(@RequestParam LocalDateTime start, @RequestParam LocalDateTime end){
        return appointmentService.getAppointmentsByStartTimeBetween(start, end);
    }
}
