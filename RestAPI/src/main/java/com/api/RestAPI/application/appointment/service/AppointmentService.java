package com.api.RestAPI.application.appointment.service;

import org.springframework.stereotype.Service;

import java.util.List;

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

import jakarta.transaction.Transactional;
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;
import org.hl7.fhir.r4.model.Appointment;

@Service
@Transactional
public class AppointmentService implements IAppointmentEventProcessor {

    private final IAppointmentRepository appointmentRepository;
    private final IAppointmentMapper appointmentMapper;
    private final IFhirParser fhirParser;
    private final IAppointmentStateHandler stateHandler;
    private final IFhirPayloadValidator fhirPayloadValidator;
    private final IHapiFhirValidator officialFhirValidator;
    private final IAppointmentFactory appointmentFactory;
    private final INotificationRepository notificationRepository;
    public AppointmentService(
            IAppointmentRepository appointmentRepository,
            IAppointmentMapper appointmentMapper,
            IFhirParser fhirParser,
            IAppointmentStateHandler stateHandler,
            IFhirPayloadValidator fhirPayloadValidator,
            IHapiFhirValidator officialFhirValidator,
            IAppointmentFactory appointmentFactory, INotificationRepository notificationRepository) {

        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
        this.fhirParser = fhirParser;
        this.stateHandler = stateHandler;
        this.fhirPayloadValidator = fhirPayloadValidator;
        this.officialFhirValidator = officialFhirValidator;
        this.appointmentFactory = appointmentFactory;
        this.notificationRepository = notificationRepository;
    }
    @Override
    public AppointmentResponseDto processAppointmentEvent(String fhirJson) {
        fhirPayloadValidator.validate(fhirJson);
        officialFhirValidator.validate(fhirJson);
        AppointmentEntity savedAppointment = saveAppointment(fhirJson);
        notificationRepository.saveAll(appointmentFactory.createNotifications(savedAppointment.getAppointmentId(), savedAppointment.getStart()));
        return appointmentMapper.toDto(savedAppointment);
    }

    public List<AppointmentEntity> getAppointments() {
        return appointmentRepository.getAppointments();
    }

    private AppointmentEntity saveAppointment(String fhirJson) {
        Appointment fhirAppointment = fhirParser.parseAppointment(fhirJson);
        String fhirId = fhirAppointment.getIdElement().getIdPart();
        AppointmentEntity newData = appointmentMapper.toEntity(fhirAppointment);

        return appointmentRepository.findByAppointmentId(fhirId)
                .map(existing -> handleUpdate(existing, newData))
                .orElseGet(() -> handleCreate(newData));
    }

    private AppointmentEntity handleCreate(AppointmentEntity newAppointment) {
        stateHandler.validateCreation(newAppointment.getStatus());

        AppointmentEntity saved = appointmentRepository.save(newAppointment);
        saved.setNewlyCreated(true);

        return saved;
    }

    private AppointmentEntity handleUpdate(AppointmentEntity existing, AppointmentEntity newData) {
        validateStatusTransition(existing, newData);
        existing.updateFrom(newData);
        AppointmentEntity updated = appointmentRepository.save(existing);
        updated.setNewlyCreated(false);
        return updated;
    }

    private void validateStatusTransition(AppointmentEntity existing, AppointmentEntity newData) {
        AppointmentStatus currentStatus = existing.getStatus();
        AppointmentStatus newStatus = newData.getStatus();
        if (!currentStatus.equals(newStatus)) {
            stateHandler.validateTransition(currentStatus, newStatus);
        }
    }


}
