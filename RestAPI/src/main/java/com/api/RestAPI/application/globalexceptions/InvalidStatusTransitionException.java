package com.api.RestAPI.application.globalexceptions;

public class InvalidStatusTransitionException extends RuntimeException { 
    public InvalidStatusTransitionException(Object current, Object next) {
        super("Ongeldige status overgang: " + current + " -> " + next);
    }
}
