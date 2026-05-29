package com.api.RestAPI.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.api.RestAPI.application.globalexceptions.InvalidFhirJsonException;
import com.api.RestAPI.application.globalexceptions.InvalidStatusTransitionException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFhirJsonException.class)
    public ResponseEntity<String> handleInvalidJson(InvalidFhirJsonException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<String> handleTransition(InvalidStatusTransitionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage();
        if (isProviderRelatedIllegalArgument(ex)) {
            return ResponseEntity.badRequest().body(
                "Invalid request data: " + message +
                ". Allowed provider values are SWIFTSEND, SECUREPOST, LEGACYLINK, ASYNCFLOW."
            );
        }

        return ResponseEntity.badRequest().body("Invalid request data: " + message);
    }

    private boolean isProviderRelatedIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }

        String normalizedMessage = message.toLowerCase();
        return normalizedMessage.contains("provider")
            || normalizedMessage.contains("swiftsend")
            || normalizedMessage.contains("securepost")
            || normalizedMessage.contains("legacylink")
            || normalizedMessage.contains("asyncflow");
    }
}
