package com.api.RestAPI.infrastructure.HapiFhir.services;

import org.springframework.stereotype.Component;

import com.api.RestAPI.application.globalexceptions.InvalidFhirJsonException;
import com.api.RestAPI.infrastructure.HapiFhir.interfaces.IFhirPayloadValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FhirPayloadValidator implements IFhirPayloadValidator {

    private final ObjectMapper objectMapper;

    public FhirPayloadValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void validate(String fhirJson) {

        if (fhirJson == null || fhirJson.isBlank()) {
            throw new InvalidFhirJsonException(
                    "FHIR payload cannot be null or empty"
            );
        }
        JsonNode jsonNode;
        try {

            jsonNode = objectMapper.readTree(fhirJson);

        } catch (JsonProcessingException ex) {

            throw new InvalidFhirJsonException(
                    "Invalid JSON structure" + ex.getOriginalMessage()
                    
            );
        }

        if (jsonNode.isEmpty()) {
            throw new InvalidFhirJsonException(
                    "FHIR payload cannot be empty"
            );
        }

        if (!jsonNode.has("resourceType")) {
            throw new InvalidFhirJsonException(
                    "FHIR resourceType ontbreekt"
            );
        }
    }
    
}
