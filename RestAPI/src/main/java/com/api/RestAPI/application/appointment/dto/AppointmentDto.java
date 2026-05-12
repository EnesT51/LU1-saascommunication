package com.api.RestAPI.application.appointment.dto;

import java.time.LocalDateTime;

import com.api.RestAPI.domain.appointment.enums.AppointmentStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class AppointmentDto {
    
    private String appointmentId;
    private String patientId;
    private String phoneNumber;
    private LocalDateTime start;
    private LocalDateTime end;
    private String instructions;
    private String location;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;


    public String getAppointmentId() {
        return appointmentId;
    }
    public String getPatientId() {
        return patientId;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public LocalDateTime getStart() {
        return start;
    }
    public LocalDateTime getEnd() {
        return end;
    }
    public String getInstructions() {
        return instructions;
    }
    public String getLocation() {
        return location;
    }
    public AppointmentStatus getStatus() {
        return status;
    }
}
