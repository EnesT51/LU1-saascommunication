package com.api.RestAPI.infrastructure.HapiFhir.interfaces;

import org.hl7.fhir.r4.model.Appointment;

import com.api.RestAPI.application.globalexceptions.InvalidFhirJsonException;

public interface IFhirParser {
    public Appointment parseAppointment(String fhirJson) throws InvalidFhirJsonException;
}
