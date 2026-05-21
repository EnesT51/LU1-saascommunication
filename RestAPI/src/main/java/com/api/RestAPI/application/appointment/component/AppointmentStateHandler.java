package com.api.RestAPI.application.appointment.component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.api.RestAPI.application.appointment.interfaces.IAppointmentStateHandler;
import com.api.RestAPI.application.globalexceptions.InvalidStatusTransitionException;
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;

import jakarta.annotation.Nullable;

@Component
public class AppointmentStateHandler implements IAppointmentStateHandler {

    private static final Set<AppointmentStatus> ALLOWED_CREATION_STATUSES = Set.of(AppointmentStatus.BOOKED);

    private static final Map<AppointmentStatus, Set<AppointmentStatus>> transitions = new EnumMap<>(AppointmentStatus.class);

    static {
        transitions.put(AppointmentStatus.BOOKED, Set.of(AppointmentStatus.CANCELLED)); // voorbeeld: van BOOKED naar CANCELLED of opnieuw BOOKED
        transitions.put(AppointmentStatus.CANCELLED, Set.of()); // eindstatus
        // voeg andere overgangen toe
    }

    public void validateTransition(@Nullable AppointmentStatus currentStatus, AppointmentStatus newStatus) {
        Set<AppointmentStatus> allowed = transitions.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            // Nieuwe afspraak
            if (!ALLOWED_CREATION_STATUSES.contains(newStatus)) {
                throw new InvalidStatusTransitionException(currentStatus, newStatus);
            }
        }
    }
    public void validateCreation(AppointmentStatus newStatus) {
        if (!ALLOWED_CREATION_STATUSES.contains(newStatus)) {
            throw new InvalidStatusTransitionException(null, newStatus);
        }
    }
}
