package com.api.RestAPI.domain.appointment.abstractRepository;

import java.util.List;
import java.util.UUID;

import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;
import java.util.Optional;  


public interface AppointmentRepository
{
    Optional<AppointmentEntity> findById(UUID id);
    List<AppointmentEntity> getAppointments();
}
