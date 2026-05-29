package com.api.RestAPI.application.appointment.interfaces;

import com.api.RestAPI.application.appointment.dto.AppointmentResponseDto;

public interface IAppointmentEventProcessor {
     AppointmentResponseDto processAppointmentEvent(String fhirJson);
}
