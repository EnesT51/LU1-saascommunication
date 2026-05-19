package com.api.RestAPI.application.appointment.dto;

import java.util.Date;

import org.hl7.fhir.r4.model.Appointment;

import com.api.RestAPI.domain.appointment.enums.AppointmentStatus;

public class AppointmentResponseDto {
    private String appointmentId;
    private String patientId;
    private String patientName;
    private String practitionerId;
    private String practitionerName;
    private String description;
    private String comment;
    private String location;
    private String status;
    private boolean newlyCreated;
    private Date start;
    private Date end;
    private AppointmentStatus appointmentStatus;


    public AppointmentStatus getAppointmentStatus() {
        return appointmentStatus;
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
    
    public Date getStart() {
        return start;
    }
    public Date getEnd() {
        return end;
    }
    public void setStart(Date start) {
        this.start = start;
    }
    public void setEnd(Date end) {
        this.end = end;
    }
    public void setNewlyCreated(boolean newlyCreated) {
        this.newlyCreated = newlyCreated;
    }
    public boolean getNewlyCreated() {
        return newlyCreated;
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



    // Getters en setters
}
