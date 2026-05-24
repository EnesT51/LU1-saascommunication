package com.api.RestAPI.infrastructure.activemq;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.application.appointment.interfaces.IAppointmentEventProcessor;
import com.api.RestAPI.application.globalexceptions.InvalidFhirJsonException;
import com.api.RestAPI.application.notification.AppointmentEventStore;

class AppointmentEventListenerTest {

    private final IAppointmentEventProcessor appointmentEventProcessor =
            org.mockito.Mockito.mock(IAppointmentEventProcessor.class);
    private final AppointmentEventStore eventStore = org.mockito.Mockito.mock(AppointmentEventStore.class);
    private final AppointmentEventListener listener =
            new AppointmentEventListener(appointmentEventProcessor, eventStore);

    @Test
    @DisplayName("Should catch invalid FHIR event without failing JMS listener")
    void receiveCatchesInvalidFhirEvent() {
        String payload = "{\"resourceType\":\"Appointment\"}";
        doThrow(new InvalidFhirJsonException("FHIR validation failed"))
                .when(appointmentEventProcessor).processAppointmentEvent(payload);

        assertDoesNotThrow(() -> listener.receive(payload));

        verify(eventStore).store(payload);
        verify(appointmentEventProcessor).processAppointmentEvent(payload);
    }

    @Test
    @DisplayName("Should still fail JMS listener for unexpected processing errors")
    void receiveRethrowsUnexpectedProcessingErrors() {
        String payload = "{\"resourceType\":\"Appointment\"}";
        doThrow(new IllegalStateException("Database unavailable"))
                .when(appointmentEventProcessor).processAppointmentEvent(payload);

        assertThrows(RuntimeException.class, () -> listener.receive(payload));

        verify(eventStore).store(payload);
        verify(appointmentEventProcessor).processAppointmentEvent(payload);
    }
}
