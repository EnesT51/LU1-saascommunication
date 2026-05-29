package com.api.RestAPI.presentation.appointment.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.api.RestAPI.application.appointment.dto.AppointmentResponseDto;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentEventProcessor;

@DisplayName("AppointmentController Tests")
class AppointmentControllerTest {

    @Test
    @DisplayName("Should return created when appointment is newly created")
    void saveAppointmentReturnsCreatedForNewAppointment() {
        IAppointmentEventProcessor processor = mock(IAppointmentEventProcessor.class);
        AppointmentController controller = new AppointmentController(processor);
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setNewlyCreated(true);
        when(processor.processAppointmentEvent("{json}")).thenReturn(dto);

        ResponseEntity<AppointmentResponseDto> response = controller.saveAppointment("{json}");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(processor).processAppointmentEvent("{json}");
    }

    @Test
    @DisplayName("Should return ok when appointment is updated")
    void saveAppointmentReturnsOkForExistingAppointment() {
        IAppointmentEventProcessor processor = mock(IAppointmentEventProcessor.class);
        AppointmentController controller = new AppointmentController(processor);
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setNewlyCreated(false);
        when(processor.processAppointmentEvent("{json}")).thenReturn(dto);

        ResponseEntity<AppointmentResponseDto> response = controller.saveAppointment("{json}");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }
}
