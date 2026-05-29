package com.api.RestAPI.infrastructure.HapiFhir.component;

import org.hl7.fhir.r4.model.Appointment;
import org.springframework.stereotype.Component;

import com.api.RestAPI.application.globalexceptions.InvalidFhirJsonException;
import com.api.RestAPI.infrastructure.HapiFhir.interfaces.IFhirParser;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@Component
public class FhirParser implements IFhirParser {

    private final FhirContext fhirConfig;
    
    public FhirParser(FhirContext fhirConfig) {
        this.fhirConfig = fhirConfig;
    }
    public Appointment parseAppointment(String fhirJson) throws InvalidFhirJsonException {

        IParser parser = fhirConfig.newJsonParser();
        Appointment appointment = parser.parseResource(Appointment.class, fhirJson);
        if (appointment == null) {
            throw new InvalidFhirJsonException("Failed to parse FHIR JSON into Appointment resource");
        }
        return appointment; 
    }
}
