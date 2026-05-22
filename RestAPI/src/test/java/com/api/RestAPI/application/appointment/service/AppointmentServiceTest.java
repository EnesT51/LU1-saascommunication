package com.api.RestAPI.application.appointment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;

import com.api.RestAPI.application.appointment.dto.AppointmentResponseDto;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentEventProcessor;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentMapper;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentStateHandler;
import com.api.RestAPI.application.notification.interfaces.IAppointmentFactory;
import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;
import com.api.RestAPI.infrastructure.HapiFhir.interfaces.IFhirParser;
import com.api.RestAPI.infrastructure.HapiFhir.interfaces.IFhirPayloadValidator;
import com.api.RestAPI.infrastructure.HapiFhir.interfaces.IHapiFhirValidator;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;
import com.api.RestAPI.infrastructure.notification.entities.Notification;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Tests")
class AppointmentServiceTest {

    @Mock
    private IAppointmentRepository appointmentRepository;

    @Mock
    private IAppointmentMapper appointmentMapper;

    @Mock
    private IFhirParser fhirParser;

    @Mock
    private IAppointmentStateHandler stateHandler;

    @Mock
    private IFhirPayloadValidator fhirPayloadValidator;

    @Mock
    private IHapiFhirValidator officialFhirValidator;

    @Mock
    private IAppointmentFactory appointmentFactory;

    @Mock
    private INotificationRepository notificationRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private String testFhirJson;
    private Appointment testFhirAppointment;
    private AppointmentEntity testAppointmentEntity;
    private AppointmentResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        testFhirJson = "{\"resourceType\":\"Appointment\"}";

        testFhirAppointment = new Appointment();
        testFhirAppointment.setId("appointment-123");
        testFhirAppointment.setStatus(AppointmentStatus.BOOKED);

        testAppointmentEntity = new AppointmentEntity();
        testAppointmentEntity.setAppointmentId("appointment-123");
        testAppointmentEntity.setStatus(AppointmentStatus.BOOKED);

        testResponseDto = new AppointmentResponseDto();
    }

    @Test
    @DisplayName("Should process appointment event successfully for new appointment")
    void testProcessAppointmentEventCreate() {
        // Arrange
        when(fhirParser.parseAppointment(testFhirJson)).thenReturn(testFhirAppointment);
        when(appointmentMapper.toEntity(testFhirAppointment)).thenReturn(testAppointmentEntity);
        when(appointmentRepository.findByAppointmentId("appointment-123")).thenReturn(Optional.empty());
        when(appointmentRepository.save(testAppointmentEntity)).thenReturn(testAppointmentEntity);
        when(appointmentFactory.createNotifications(anyString(), any())).thenReturn(List.of());
        when(appointmentMapper.toDto(testAppointmentEntity)).thenReturn(testResponseDto);

        // Act
        AppointmentResponseDto result = appointmentService.processAppointmentEvent(testFhirJson);

        // Assert
        assertNotNull(result);
        verify(fhirPayloadValidator).validate(testFhirJson);
        verify(officialFhirValidator).validate(testFhirJson);
        verify(appointmentRepository).save(testAppointmentEntity);
        verify(notificationRepository).saveAll(anyList());
        assertTrue(testAppointmentEntity.isNewlyCreated());
    }

    @Test
    @DisplayName("Should process appointment event successfully for existing appointment update")
    void testProcessAppointmentEventUpdate() {
        // Arrange
        AppointmentEntity existingEntity = new AppointmentEntity();
        existingEntity.setAppointmentId("appointment-123");
        existingEntity.setStatus(AppointmentStatus.BOOKED);

        when(fhirParser.parseAppointment(testFhirJson)).thenReturn(testFhirAppointment);
        when(appointmentMapper.toEntity(testFhirAppointment)).thenReturn(testAppointmentEntity);
        when(appointmentRepository.findByAppointmentId("appointment-123"))
                .thenReturn(Optional.of(existingEntity));
        when(appointmentRepository.save(existingEntity)).thenReturn(existingEntity);
        when(appointmentFactory.createNotifications(anyString(), any())).thenReturn(List.of());
        when(appointmentMapper.toDto(existingEntity)).thenReturn(testResponseDto);

        // Act
        AppointmentResponseDto result = appointmentService.processAppointmentEvent(testFhirJson);

        // Assert
        assertNotNull(result);
        verify(fhirPayloadValidator).validate(testFhirJson);
        verify(officialFhirValidator).validate(testFhirJson);
        verify(appointmentRepository).save(existingEntity);
        verify(notificationRepository).saveAll(anyList());
        assertFalse(existingEntity.isNewlyCreated());
    }

    @Test
    @DisplayName("Should validate payload with custom validator")
    void testProcessAppointmentEventValidatesPayload() {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid payload"))
                .when(fhirPayloadValidator).validate(testFhirJson);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.processAppointmentEvent(testFhirJson);
        });
        verify(fhirPayloadValidator).validate(testFhirJson);
    }

    @Test
    @DisplayName("Should validate payload with official FHIR validator")
    void testProcessAppointmentEventValidatesFhirStructure() {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid FHIR structure"))
                .when(officialFhirValidator).validate(testFhirJson);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.processAppointmentEvent(testFhirJson);
        });
        verify(officialFhirValidator).validate(testFhirJson);
    }

    @Test
    @DisplayName("Should retrieve all appointments")
    void testGetAppointments() {
        // Arrange
        AppointmentEntity appointment1 = new AppointmentEntity();
        appointment1.setAppointmentId("apt-1");

        AppointmentEntity appointment2 = new AppointmentEntity();
        appointment2.setAppointmentId("apt-2");

        List<AppointmentEntity> expectedAppointments = List.of(appointment1, appointment2);
        when(appointmentRepository.getAppointments()).thenReturn(expectedAppointments);

        // Act
        List<AppointmentEntity> result = appointmentService.getAppointments();

        // Assert
        assertEquals(2, result.size());
        assertEquals("apt-1", result.get(0).getAppointmentId());
        assertEquals("apt-2", result.get(1).getAppointmentId());
        verify(appointmentRepository).getAppointments();
    }

    @Test
    @DisplayName("Should return empty list when no appointments exist")
    void testGetAppointmentsEmpty() {
        // Arrange
        when(appointmentRepository.getAppointments()).thenReturn(List.of());

        // Act
        List<AppointmentEntity> result = appointmentService.getAppointments();

        // Assert
        assertTrue(result.isEmpty());
        verify(appointmentRepository).getAppointments();
    }

    @Test
    @DisplayName("Should handle creation with valid status")
    void testHandleCreateWithValidStatus() {
        // Arrange
        when(fhirParser.parseAppointment(testFhirJson)).thenReturn(testFhirAppointment);
        when(appointmentMapper.toEntity(testFhirAppointment)).thenReturn(testAppointmentEntity);
        when(appointmentRepository.findByAppointmentId("appointment-123")).thenReturn(Optional.empty());
        when(appointmentRepository.save(testAppointmentEntity)).thenReturn(testAppointmentEntity);
        when(appointmentFactory.createNotifications(anyString(), any())).thenReturn(List.of());
        when(appointmentMapper.toDto(testAppointmentEntity)).thenReturn(testResponseDto);
        doNothing().when(stateHandler).validateCreation(AppointmentStatus.BOOKED);

        // Act
        AppointmentResponseDto result = appointmentService.processAppointmentEvent(testFhirJson);

        // Assert
        assertNotNull(result);
        verify(stateHandler).validateCreation(testAppointmentEntity.getStatus());
    }

    @Test
    @DisplayName("Should handle update with valid status transition")
    void testHandleUpdateWithValidStatusTransition() {
        // Arrange
        AppointmentEntity existingEntity = new AppointmentEntity();
        existingEntity.setAppointmentId("appointment-123");
        existingEntity.setStatus(AppointmentStatus.BOOKED);

        AppointmentEntity newEntity = new AppointmentEntity();
        newEntity.setAppointmentId("appointment-123");
        newEntity.setStatus(AppointmentStatus.CANCELLED);

        when(fhirParser.parseAppointment(testFhirJson)).thenReturn(testFhirAppointment);
        when(appointmentMapper.toEntity(testFhirAppointment)).thenReturn(newEntity);
        when(appointmentRepository.findByAppointmentId("appointment-123"))
                .thenReturn(Optional.of(existingEntity));
        when(appointmentRepository.save(existingEntity)).thenReturn(existingEntity);
        when(appointmentFactory.createNotifications(anyString(), any())).thenReturn(List.of());
        when(appointmentMapper.toDto(existingEntity)).thenReturn(testResponseDto);
        doNothing().when(stateHandler).validateTransition(AppointmentStatus.BOOKED, AppointmentStatus.CANCELLED);

        // Act
        appointmentService.processAppointmentEvent(testFhirJson);

        // Assert
        verify(stateHandler).validateTransition(AppointmentStatus.BOOKED, AppointmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should not validate transition when status remains the same")
    void testHandleUpdateWithSameStatus() {
        // Arrange
        AppointmentEntity existingEntity = new AppointmentEntity();
        existingEntity.setAppointmentId("appointment-123");
        existingEntity.setStatus(AppointmentStatus.BOOKED);

        AppointmentEntity newEntity = new AppointmentEntity();
        newEntity.setAppointmentId("appointment-123");
        newEntity.setStatus(AppointmentStatus.BOOKED);

        when(fhirParser.parseAppointment(testFhirJson)).thenReturn(testFhirAppointment);
        when(appointmentMapper.toEntity(testFhirAppointment)).thenReturn(newEntity);
        when(appointmentRepository.findByAppointmentId("appointment-123"))
                .thenReturn(Optional.of(existingEntity));
        when(appointmentRepository.save(existingEntity)).thenReturn(existingEntity);
        when(appointmentFactory.createNotifications(anyString(), any())).thenReturn(List.of());
        when(appointmentMapper.toDto(existingEntity)).thenReturn(testResponseDto);

        // Act
        appointmentService.processAppointmentEvent(testFhirJson);

        // Assert
        verify(stateHandler, never()).validateTransition(any(), any());
    }

    @Test
    @DisplayName("Should create notifications after saving appointment")
    void testNotificationsCreatedAfterSave() {
        // Arrange
        List<Notification> mockNotifications = List.of(new Notification());

        when(fhirParser.parseAppointment(testFhirJson)).thenReturn(testFhirAppointment);
        when(appointmentMapper.toEntity(testFhirAppointment)).thenReturn(testAppointmentEntity);
        when(appointmentRepository.findByAppointmentId("appointment-123")).thenReturn(Optional.empty());
        when(appointmentRepository.save(testAppointmentEntity)).thenReturn(testAppointmentEntity);
        when(appointmentFactory.createNotifications("appointment-123", testAppointmentEntity.getStart()))
                .thenReturn(mockNotifications);
        when(appointmentMapper.toDto(testAppointmentEntity)).thenReturn(testResponseDto);

        // Act
        appointmentService.processAppointmentEvent(testFhirJson);

        // Assert
        verify(appointmentFactory).createNotifications("appointment-123", testAppointmentEntity.getStart());
        verify(notificationRepository).saveAll(mockNotifications);
    }

    @Test
    @DisplayName("Should implement IAppointmentEventProcessor interface")
    void testServiceImplementsInterface() {
        // Assert
        assertTrue(appointmentService instanceof IAppointmentEventProcessor);
    }
}
