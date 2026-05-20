package com.api.RestAPI.infrastructure.HapiFhir.interfaces;

import org.hl7.fhir.r4.model.Appointment;

public interface IFhirParser {
    public Appointment parseAppointment(String fhirJson) throws IllegalArgumentException;
}
