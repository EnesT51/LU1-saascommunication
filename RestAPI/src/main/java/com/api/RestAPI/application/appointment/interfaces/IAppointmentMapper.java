package com.api.RestAPI.application.appointment.interfaces;

import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;
import org.hl7.fhir.r4.model.Appointment;
public interface IAppointmentMapper {
    AppointmentEntity toEntity(Appointment appointment);
}
