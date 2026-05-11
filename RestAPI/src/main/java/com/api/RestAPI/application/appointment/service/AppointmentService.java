package com.api.RestAPI.application.appointment.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.api.RestAPI.domain.appointment.entities.Appointment;
import com.api.RestAPI.domain.appointment.abstractRepository.AppointmentRepository;
import com.api.RestAPI.domain.appointment.abstractRepository.AppointmentRepositoryCustom;

@Service
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final AppointmentRepositoryCustom appointmentRepositoryCustom;

    public AppointmentService(AppointmentRepository appointmentRepository, AppointmentRepositoryCustom appointmentRepositoryCustom) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentRepositoryCustom = appointmentRepositoryCustom;
    }

    public List<Appointment> getAppointments(){
        return appointmentRepository.findAll();
    }
    public List<Appointment> getAppointmentsByStartTimeBetween(LocalDateTime start, LocalDateTime end){
        return appointmentRepositoryCustom.findByStartTimeBetween(start, end);
    }
}
