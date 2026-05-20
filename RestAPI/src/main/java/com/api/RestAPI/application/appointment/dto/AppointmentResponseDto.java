package com.api.RestAPI.application.appointment.dto;

import java.time.Instant;

import com.api.RestAPI.domain.appointment.enums.AppointmentStatus;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;

public class AppointmentResponseDto {
    private String appointmentId;
    private String patientId;
    private String patientName;
    private String practitionerId;
    private String practitionerName;
    private String description;
    private String comment;
    private String location;
    private String locationId;
    private String patientPhoneNumber;
    private String status;
    private boolean newlyCreated;
    private Instant start;
    private Instant end;
    private boolean isNewlyCreated;
    private AppointmentStatus appointmentStatus;


    public AppointmentStatus getAppointmentStatus() {
        return appointmentStatus;
    }
    public boolean getNewlyCreated() {
        return newlyCreated;
    }
    public String getAppointmentId() {
        return appointmentId;
    }
    public String getPatientId() {
        return patientId;
    }
    public String getPatientName() {
        return patientName;
    }
    public String getPractitionerId() {
        return practitionerId;
    }
    public String getPractitionerName() {
        return practitionerName;
    }
    public String getLocationId() {
        return locationId;
    }
    public String getPatientPhoneNumber() {
        return patientPhoneNumber;
    }
    public String getDescription() {
        return description;
    }
    public String getComment() {
        return comment;
    }
    public String getLocation() {
        return location;
    }
    public String getStatus() {
        return status;
    }
    
    public Instant getStart() {
        return start;
    }
    public Instant getEnd() {
        return end;
    }
    public void setNewlyCreated(boolean newlyCreated) {
        this.newlyCreated = newlyCreated;
    }
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
    public void setPatientPhoneNumber(String patientPhoneNumber) {
        this.patientPhoneNumber = patientPhoneNumber;
    }
    public void setStart(Instant start) {
        this.start = start;
    }
    public void setEnd(Instant end) {
        this.end = end;
    }
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    public void setAppointmentStatus(AppointmentStatus appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    public void setPractitionerId(String practitionerId) {
        this.practitionerId = practitionerId;
    }
    public void setPractitionerName(String practitionerName) {
        this.practitionerName = practitionerName;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void toDto(AppointmentEntity dto) {
        this.appointmentId = dto.getAppointmentId();
        this.patientId = dto.getPatientId();
        this.patientName = dto.getPatientName();
        this.practitionerId = dto.getPractitionerId();
        this.practitionerName = dto.getPractitionerName();
        this.description = dto.getDescription();
        this.comment = dto.getComment();
        this.location = dto.getLocation();
        this.locationId = dto.getLocationId();
        this.patientPhoneNumber = dto.getPatientPhoneNumber();
        this.status = dto.getStatus().toString();
        this.start = dto.getStart();
        this.end = dto.getEnd();
    }

}
