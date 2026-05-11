package com.api.RestAPI.domain.appointment.abstractRepository;

import com.api.RestAPI.domain.appointment.entities.Appointment;
import java.util.List;
import java.time.LocalDateTime;

public interface AppointmentRepositoryCustom {
    public List<Appointment> findByStartTimeBetween(LocalDateTime start,LocalDateTime end);
}
