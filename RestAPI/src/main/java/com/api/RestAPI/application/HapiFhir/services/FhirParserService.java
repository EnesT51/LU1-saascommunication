package com.api.RestAPI.application.HapiFhir.services;

import org.hl7.fhir.r4.model.Appointment;

import com.api.RestAPI.application.HapiFhir.interfaces.IFhirParser;
import com.api.RestAPI.infrastructure.config.FhirConfig;

import ca.uhn.fhir.parser.IParser;

public class FhirParserService implements IFhirParser {

    private final FhirConfig fhirConfig;
    
    public FhirParserService(FhirConfig fhirConfig) {
        this.fhirConfig = fhirConfig;
    }
    @Override
    public Appointment parseAppointment(String fhirJson) throws Exception {

        IParser parser = fhirConfig.fhirContext().newJsonParser();
        Appointment appointment = parser.parseResource(Appointment.class, fhirJson);
        if (appointment == null) {
            throw new IllegalArgumentException("Failed to parse FHIR JSON into Appointment resource");
        }
        return appointment; 
    }
    
}
