package com.api.RestAPI.infrastructure.HapiFhir.component;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.application.globalexceptions.InvalidFhirJsonException;
import com.fasterxml.jackson.databind.ObjectMapper;

@DisplayName("FhirPayloadValidator Tests")
class FhirPayloadValidatorTest {

    private final FhirPayloadValidator validator = new FhirPayloadValidator(new ObjectMapper());

    @Test
    @DisplayName("Should accept payload with resourceType")
    void validateAcceptsPayloadWithResourceType() {
        assertDoesNotThrow(() -> validator.validate("{\"resourceType\":\"Appointment\"}"));
    }

    @Test
    @DisplayName("Should reject null payload")
    void validateRejectsNullPayload() {
        assertThrows(InvalidFhirJsonException.class, () -> validator.validate(null));
    }

    @Test
    @DisplayName("Should reject blank payload")
    void validateRejectsBlankPayload() {
        assertThrows(InvalidFhirJsonException.class, () -> validator.validate(" "));
    }

    @Test
    @DisplayName("Should reject invalid JSON")
    void validateRejectsInvalidJson() {
        assertThrows(InvalidFhirJsonException.class, () -> validator.validate("{invalid"));
    }

    @Test
    @DisplayName("Should reject payload without resourceType")
    void validateRejectsPayloadWithoutResourceType() {
        assertThrows(InvalidFhirJsonException.class, () -> validator.validate("{\"id\":\"appointment-123\"}"));
    }
}
