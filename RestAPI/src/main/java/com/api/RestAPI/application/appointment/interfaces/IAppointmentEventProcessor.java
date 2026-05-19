package com.api.RestAPI.application.appointment.interfaces;

import com.api.RestAPI.domain.appointment.entities.AppointmentEntity;

public interface IAppointmentEventProcessor {
     AppointmentEntity processAppointmentEvent(String fhirJson);
}
