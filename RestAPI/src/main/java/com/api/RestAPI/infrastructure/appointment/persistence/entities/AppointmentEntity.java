package com.api.RestAPI.infrastructure.appointment.persistence.entities;

import java.time.Instant;
import java.util.UUID;

import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Table(name = "appointment")
@Entity
public class AppointmentEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private String appointmentId;
    private String patientId;
    private String patientName;
    private String practitionerId;
    private String practitionerName;

    private Instant start;
    private Instant end;

    private String patientPhoneNumber;

    @Transient
    private boolean newlyCreated;

    private String description;
    @Column(columnDefinition = "TEXT")
    private String comment;

    private String locationId;
    private String location;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    public UUID getId() {
        return id;
    }
    public boolean isNewlyCreated() {
        return newlyCreated;
    }
    public String getPatientPhoneNumber() {
        return patientPhoneNumber;
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

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
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

    public String getLocationId() {
        return locationId;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    public void setNewlyCreated(boolean newlyCreated) {
        this.newlyCreated = newlyCreated;
    }   

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
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

    public void setStart(Instant start) {
        this.start = start;
    }

    public void setEnd(Instant end) {
        this.end = end;
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

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
    public void setPatientPhoneNumber(String patientPhoneNumber) {
        this.patientPhoneNumber = patientPhoneNumber;
    }
    public void updateFrom(AppointmentEntity updated) {

        this.status = updated.status;
        this.start = updated.start;
        this.end = updated.end;
        this.description = updated.description;
        this.comment = updated.comment;
        this.patientId = updated.patientId;
        this.patientName = updated.patientName;
        this.practitionerId = updated.practitionerId;
        this.practitionerName = updated.practitionerName;
        this.appointmentId = updated.appointmentId;
    }
}
