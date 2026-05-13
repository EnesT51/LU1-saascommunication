package com.api.RestAPI.domain.appointment.Interface;

import java.util.List;
import java.util.UUID;

import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;
import java.util.Optional;


public interface IAppointmentRepository
{
    Optional<AppointmentEntity> findById(UUID id);
    Optional<AppointmentEntity> findByAppointmentId(String appointmentId);
    List<AppointmentEntity> getAppointments();
}
