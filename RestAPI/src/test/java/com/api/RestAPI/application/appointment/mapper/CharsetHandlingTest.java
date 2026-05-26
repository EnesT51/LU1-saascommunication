package com.api.RestAPI.application.appointment.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Date;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;

/**
 * NFR 8: De communicatiemodule dient berichten te kunnen verwerken in diverse karaktersets.
 * Patiëntnamen en beschrijvingen kunnen Arabische, Chinese, Japanse of Europese tekens bevatten.
 */
@DisplayName("Charset Handling Tests - NFR 8")
class CharsetHandlingTest {

    private final AppointmentMapper mapper = new AppointmentMapper();

    @Test
    @DisplayName("NFR 8 - Arabische patiëntnaam wordt correct verwerkt")
    void toEntityHandlesArabicPatientName() {
        String arabicName = "محمد علي";
        AppointmentEntity result = buildEntityWithPatientName(arabicName);

        assertEquals(arabicName, result.getPatientName());
    }

    @Test
    @DisplayName("NFR 8 - Chinese patiëntnaam wordt correct verwerkt")
    void toEntityHandlesChinesePatientName() {
        String chineseName = "王伟";
        AppointmentEntity result = buildEntityWithPatientName(chineseName);

        assertEquals(chineseName, result.getPatientName());
    }

    @Test
    @DisplayName("NFR 8 - Japanse patiëntnaam wordt correct verwerkt")
    void toEntityHandlesJapanesePatientName() {
        String japaneseName = "田中 太郎";
        AppointmentEntity result = buildEntityWithPatientName(japaneseName);

        assertEquals(japaneseName, result.getPatientName());
    }

    @Test
    @DisplayName("NFR 8 - Europese speciale tekens worden correct verwerkt")
    void toEntityHandlesEuropeanSpecialCharacters() {
        String europeanName = "Ångström Müller-Ünlü";
        AppointmentEntity result = buildEntityWithPatientName(europeanName);

        assertEquals(europeanName, result.getPatientName());
    }

    private AppointmentEntity buildEntityWithPatientName(String patientName) {
        Appointment appointment = new Appointment();
        appointment.setId("appointment-charset-test");
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setStart(Date.from(Instant.parse("2026-06-01T10:00:00Z")));
        appointment.setEnd(Date.from(Instant.parse("2026-06-01T10:30:00Z")));

        AppointmentParticipantComponent participant = new AppointmentParticipantComponent();
        participant.setActor(new Reference("Patient/patient-1").setDisplay(patientName));
        appointment.addParticipant(participant);

        return mapper.toEntity(appointment);
    }
}
