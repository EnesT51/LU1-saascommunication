package com.api.RestAPI.domain.appointment.entities;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import com.api.RestAPI.domain.appointment.enums.AppointmentStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "appointment")
@Entity
public class AppointmentEntity {

    @Id
    @GeneratedValue
    private UUID id;
    
    private String appointmentId;
    private String patientId;
    private String phoneNumber;
    private Date start;
    private Date end;
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
    public Date getStart() {
        return start;
    }
    public Date getEnd() {
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
    public void setId(UUID id) {
        this.id = id;
    }
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public void setStart(Date start) {
        this.start = start;
    }
    public void setEnd(Date end) {
        this.end = end;
    }
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
    

}
