package com.api.RestAPI.application.appointment.interfaces;

public interface IAppointmentEventProcessor {
    String processAppointmentEvent(String fhirJson);
}
