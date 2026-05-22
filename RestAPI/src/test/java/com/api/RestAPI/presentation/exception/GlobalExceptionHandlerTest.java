package com.api.RestAPI.presentation.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.api.RestAPI.application.globalexceptions.InvalidFhirJsonException;
import com.api.RestAPI.application.globalexceptions.InvalidStatusTransitionException;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Should return bad request for invalid FHIR JSON")
    void handleInvalidJsonReturnsBadRequest() {
        ResponseEntity<String> response = handler.handleInvalidJson(new InvalidFhirJsonException("Invalid JSON"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid JSON", response.getBody());
    }

    @Test
    @DisplayName("Should return conflict for invalid status transition")
    void handleTransitionReturnsConflict() {
        InvalidStatusTransitionException exception = new InvalidStatusTransitionException("BOOKED", "PROPOSED");

        ResponseEntity<String> response = handler.handleTransition(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(exception.getMessage(), response.getBody());
    }

    @Test
    @DisplayName("Should return provider-specific message for invalid provider")
    void handleIllegalArgumentReturnsProviderSpecificMessage() {
        ResponseEntity<String> response = handler.handleIllegalArgument(new IllegalArgumentException("No provider found"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Invalid request data: No provider found. Allowed provider values are SWIFTSEND, SECUREPOST, LEGACYLINK, ASYNCFLOW.",
                response.getBody()
        );
    }
}
