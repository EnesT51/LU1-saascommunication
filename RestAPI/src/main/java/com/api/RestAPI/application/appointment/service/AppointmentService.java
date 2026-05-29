package com.api.RestAPI.application.appointment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import com.api.RestAPI.application.appointment.dto.AppointmentResponseDto;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentEventProcessor;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentMapper;
import com.api.RestAPI.application.appointment.interfaces.IAppointmentStateHandler;
import com.api.RestAPI.application.notification.interfaces.IAppointmentFactory;
import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;
import com.api.RestAPI.domain.notification.enums.NotificationType;
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

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

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

        // Alleen bij een nieuwe afspraak notificaties aanmaken
        // Bij updates (annulering, wijziging) regelt handleUpdate() de notificaties
        if (savedAppointment.isNewlyCreated()) {
            // Eis 1: "Voor afspraken die reeds zijn aangevangen worden geen notificaties verstuurd"
            if (savedAppointment.getStart() != null && savedAppointment.getStart().isAfter(Instant.now())) {
                notificationRepository.saveAll(
                        appointmentFactory.createNotifications(
                                savedAppointment.getAppointmentId(),
                                savedAppointment.getStart(),
                                savedAppointment.getOrganizationId()));
            } else {
                log.warn("Afspraak {} is al aangevangen of heeft geen starttijd — geen notificaties aangemaakt",
                        savedAppointment.getAppointmentId());
            }
        }

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

        // Afspraak geannuleerd → alle openstaande notificaties annuleren
        if (AppointmentStatus.CANCELLED.equals(newData.getStatus())) {
            List<Notification> pending = notificationRepository.findPendingForAppointment(existing.getAppointmentId());
            for (Notification n : pending) {
                n.markAsCancelled();
                notificationRepository.save(n);
            }
            log.info("Afspraak {} geannuleerd: {} openstaande notificatie(s) geannuleerd",
                    existing.getAppointmentId(), pending.size());
        }
        // Starttijd gewijzigd → notificatietijden herplannen
        else if (newData.getStart() != null && !Objects.equals(existing.getStart(), newData.getStart())) {
            List<Notification> pending = notificationRepository.findPendingForAppointment(existing.getAppointmentId());
            for (Notification n : pending) {
                Instant newScheduledAt = n.getType() == NotificationType.REMINDER_24H
                        ? newData.getStart().minus(24, ChronoUnit.HOURS)
                        : newData.getStart().minus(1, ChronoUnit.HOURS);
                n.reschedule(newScheduledAt);
                notificationRepository.save(n);
            }
            log.info("Afspraak {} verplaatst naar {}: {} notificatie(s) herplant",
                    existing.getAppointmentId(), newData.getStart(), pending.size());
        }

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
