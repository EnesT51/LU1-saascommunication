package com.api.RestAPI.application.appointment.interfaces;

import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;

import jakarta.annotation.Nullable;

public interface IAppointmentStateHandler {
    void validateTransition(@Nullable AppointmentStatus current, AppointmentStatus next);
    void validateCreation(AppointmentStatus newStatus);
}
