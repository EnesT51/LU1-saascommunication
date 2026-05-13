package com.api.RestAPI.application.appointment.interfaces;

public interface IAppointmentEventProcessor {
    void processAppointmentEvent(String fhirJson);
}
