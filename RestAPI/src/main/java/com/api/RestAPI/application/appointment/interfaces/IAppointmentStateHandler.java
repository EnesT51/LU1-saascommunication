package com.api.RestAPI.application.appointment.interfaces;

import com.api.RestAPI.domain.appointment.enums.AppointmentStatus;

import jakarta.annotation.Nullable;

public interface IAppointmentStateHandler {
    void validateTransition(@Nullable AppointmentStatus current, AppointmentStatus next);
    void validateCreation(AppointmentStatus newStatus);
}
