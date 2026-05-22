package com.api.RestAPI.domain.appointment.Interface;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;

import java.util.Optional;


public interface IAppointmentRepository
{
    Optional<AppointmentEntity> findById(UUID id);
    Optional<AppointmentEntity> findByAppointmentId(String appointmentId);
    AppointmentEntity save(AppointmentEntity appointment);
    List<AppointmentEntity> getAppointments();
    long deleteByCreatedAtBefore(Instant cutoffDate);
}
