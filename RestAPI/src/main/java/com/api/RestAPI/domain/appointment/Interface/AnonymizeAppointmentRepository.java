package com.api.RestAPI.domain.appointment.Interface;

import java.time.Instant;

import org.springframework.data.repository.query.Param;

public interface AnonymizeAppointmentRepository {
    int anonymizeAppointmentsOlderThan(@Param("cutoffDate")Instant cutoffDate);
}
