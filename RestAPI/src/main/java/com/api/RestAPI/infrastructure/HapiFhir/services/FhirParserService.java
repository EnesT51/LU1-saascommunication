package com.api.RestAPI.infrastructure.HapiFhir.services;

import org.hl7.fhir.r4.model.Appointment;
import org.springframework.stereotype.Service;

import com.api.RestAPI.infrastructure.HapiFhir.interfaces.IFhirParser;
import com.api.RestAPI.infrastructure.config.FhirConfig;

import ca.uhn.fhir.parser.IParser;

@Service
public class FhirParserService implements IFhirParser {

    private final FhirConfig fhirConfig;
    
    public FhirParserService(FhirConfig fhirConfig) {
        this.fhirConfig = fhirConfig;
    }
    public Appointment parseAppointment(String fhirJson) throws IllegalArgumentException {

        IParser parser = fhirConfig.fhirContext().newJsonParser();
        Appointment appointment = parser.parseResource(Appointment.class, fhirJson);
        if (appointment == null) {
            throw new IllegalArgumentException("Failed to parse FHIR JSON into Appointment resource");
        }
        return appointment; 
    }
}
