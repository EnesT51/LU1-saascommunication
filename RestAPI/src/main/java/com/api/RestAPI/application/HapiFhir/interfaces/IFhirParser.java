package com.api.RestAPI.application.HapiFhir.interfaces;

import org.hl7.fhir.r4.model.Appointment;

public interface IFhirParser {
    Appointment parseAppointment(String fhirJson) throws Exception;
}
