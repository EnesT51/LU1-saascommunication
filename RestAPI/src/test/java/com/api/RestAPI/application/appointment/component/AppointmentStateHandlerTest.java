package com.api.RestAPI.application.appointment.component;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.application.globalexceptions.InvalidStatusTransitionException;

@DisplayName("AppointmentStateHandler Tests")
class AppointmentStateHandlerTest {

    private final AppointmentStateHandler stateHandler = new AppointmentStateHandler();

    @Test
    @DisplayName("Should allow creating booked appointment")
    void validateCreationAllowsBookedStatus() {
        assertDoesNotThrow(() -> stateHandler.validateCreation(AppointmentStatus.BOOKED));
    }

    @Test
    @DisplayName("Should reject creating cancelled appointment")
    void validateCreationRejectsCancelledStatus() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> stateHandler.validateCreation(AppointmentStatus.CANCELLED)
        );
    }

    @Test
    @DisplayName("Should allow booked to cancelled transition")
    void validateTransitionAllowsBookedToCancelled() {
        assertDoesNotThrow(() ->
                stateHandler.validateTransition(AppointmentStatus.BOOKED, AppointmentStatus.CANCELLED)
        );
    }

    @Test
    @DisplayName("Should reject cancelled to booked transition")
    void validateTransitionRejectsCancelledToBooked() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> stateHandler.validateTransition(AppointmentStatus.CANCELLED, AppointmentStatus.BOOKED)
        );
    }
}
