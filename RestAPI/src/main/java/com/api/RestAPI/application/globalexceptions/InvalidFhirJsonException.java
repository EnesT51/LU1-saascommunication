package com.api.RestAPI.application.globalexceptions;

public class InvalidFhirJsonException extends RuntimeException {
    public InvalidFhirJsonException(String message) {
        super(message);
    }
}
