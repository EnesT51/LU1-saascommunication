package com.api.RestAPI.application.appointment.interfaces;


import com.api.RestAPI.application.appointment.dto.AppointmentResponseDto;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;

import org.hl7.fhir.r4.model.Appointment;
public interface IAppointmentMapper {
    AppointmentEntity toEntity(Appointment appointment);
    AppointmentResponseDto toDto(AppointmentEntity appointmentEntity);

}
