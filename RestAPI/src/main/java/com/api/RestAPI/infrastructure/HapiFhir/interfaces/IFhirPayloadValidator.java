package com.api.RestAPI.infrastructure.HapiFhir.interfaces;

public interface IFhirPayloadValidator {
    
    void validate(String fhirJson);
}
