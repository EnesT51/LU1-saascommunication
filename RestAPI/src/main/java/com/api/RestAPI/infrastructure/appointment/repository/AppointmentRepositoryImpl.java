package com.api.RestAPI.infrastructure.appointment.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.api.RestAPI.domain.appointment.abstractRepository.AppointmentRepositoryCustom;
import com.api.RestAPI.domain.appointment.entities.Appointment;

public class AppointmentRepositoryImpl implements AppointmentRepositoryCustom {


    @Override
    public List<Appointment> findByStartTimeBetween(LocalDateTime start, LocalDateTime end) {
        // TODO Auto-generated method stub
        return List.of(new Appointment(LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
        
    }
}
