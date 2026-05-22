package com.api.RestAPI.application.appointment.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Date;

import org.hibernate.MappingException;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.application.appointment.dto.AppointmentResponseDto;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;

@DisplayName("AppointmentMapper Tests")
class AppointmentMapperTest {

    private final AppointmentMapper mapper = new AppointmentMapper();

    @Test
    @DisplayName("Should map FHIR appointment to entity")
    void toEntityMapsAppointmentFieldsAndParticipants() {
        Instant start = Instant.parse("2026-05-22T10:00:00Z");
        Instant end = Instant.parse("2026-05-22T10:30:00Z");
        Appointment appointment = new Appointment();
        appointment.setId("appointment-123");
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setStart(Date.from(start));
        appointment.setEnd(Date.from(end));
        appointment.setDescription("Consult");
        appointment.setComment("Bring documents");
        appointment.addParticipant(participant("Patient/patient-1", "Jane Patient"));
        appointment.addParticipant(participant("Practitioner/practitioner-1", "Dr Smith"));
        appointment.addParticipant(participant("Location/location-1", "Room 1"));
        appointment.addExtension(new Extension("patientPhone", new StringType("+31612345678")));

        AppointmentEntity result = mapper.toEntity(appointment);

        assertEquals("appointment-123", result.getAppointmentId());
        assertEquals(start, result.getStart());
        assertEquals(end, result.getEnd());
        assertEquals("Consult", result.getDescription());
        assertEquals("Bring documents", result.getComment());
        assertEquals(AppointmentStatus.BOOKED, result.getStatus());
        assertEquals("patient-1", result.getPatientId());
        assertEquals("Jane Patient", result.getPatientName());
        assertEquals("practitioner-1", result.getPractitionerId());
        assertEquals("Dr Smith", result.getPractitionerName());
        assertEquals("location-1", result.getLocationId());
        assertEquals("Room 1", result.getLocation());
        assertEquals("+31612345678", result.getPatientPhoneNumber());
    }

    @Test
    @DisplayName("Should reject null FHIR appointment")
    void toEntityRejectsNullAppointment() {
        assertThrows(MappingException.class, () -> mapper.toEntity(null));
    }

    @Test
    @DisplayName("Should reject unsupported appointment status")
    void toEntityRejectsUnsupportedStatus() {
        Appointment appointment = new Appointment();
        appointment.setId("appointment-123");
        appointment.setStart(Date.from(Instant.parse("2026-05-22T10:00:00Z")));
        appointment.setEnd(Date.from(Instant.parse("2026-05-22T10:30:00Z")));
        appointment.setStatus(AppointmentStatus.PROPOSED);

        assertThrows(MappingException.class, () -> mapper.toEntity(appointment));
    }

    @Test
    @DisplayName("Should map entity to dto")
    void toDtoMapsEntityFields() {
        Instant start = Instant.parse("2026-05-22T10:00:00Z");
        Instant end = Instant.parse("2026-05-22T10:30:00Z");
        AppointmentEntity entity = new AppointmentEntity();
        entity.setAppointmentId("appointment-123");
        entity.setPatientId("patient-1");
        entity.setPatientName("Jane Patient");
        entity.setPractitionerId("practitioner-1");
        entity.setPractitionerName("Dr Smith");
        entity.setLocationId("location-1");
        entity.setLocation("Room 1");
        entity.setPatientPhoneNumber("+31612345678");
        entity.setDescription("Consult");
        entity.setComment("Bring documents");
        entity.setStatus(AppointmentStatus.BOOKED);
        entity.setStart(start);
        entity.setEnd(end);

        AppointmentResponseDto result = mapper.toDto(entity);

        assertEquals("appointment-123", result.getAppointmentId());
        assertEquals("patient-1", result.getPatientId());
        assertEquals("Jane Patient", result.getPatientName());
        assertEquals("practitioner-1", result.getPractitionerId());
        assertEquals("Dr Smith", result.getPractitionerName());
        assertEquals("location-1", result.getLocationId());
        assertEquals("Room 1", result.getLocation());
        assertEquals("+31612345678", result.getPatientPhoneNumber());
        assertEquals("Consult", result.getDescription());
        assertEquals("Bring documents", result.getComment());
        assertEquals("BOOKED", result.getStatus());
        assertEquals(start, result.getStart());
        assertEquals(end, result.getEnd());
    }

    private AppointmentParticipantComponent participant(String reference, String display) {
        AppointmentParticipantComponent participant = new AppointmentParticipantComponent();
        participant.setActor(new Reference(reference).setDisplay(display));
        return participant;
    }
}
