package com.api.RestAPI.application.appointment.service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.appointment.Interface.AnonymizeAppointmentRepository;
import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AppointmentCleanupService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentCleanupService.class);

    private final IAppointmentRepository repository;
    private final AnonymizeAppointmentRepository anonymizeRepository;
    private final INotificationRepository notificationRepository;

    public AppointmentCleanupService(
            IAppointmentRepository repository,
            AnonymizeAppointmentRepository anonymizeRepository,
            INotificationRepository notificationRepository
    ) {
        this.repository = repository;
        this.anonymizeRepository = anonymizeRepository;
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldAppointments() {
        Instant cutoff = Instant.now().minus(14, ChronoUnit.DAYS);

        // Stap 1: anonimiseer patiëntgegevens in de afspraken (NFR 10)
        int anonymizedAppointments = anonymizeRepository.anonymizeAppointmentsEndedBefore(cutoff);
        log.info("Afspraken geanonimiseerd (PII verwijderd): {}", anonymizedAppointments);

        // Stap 2: anonimiseer appointmentId in notificaties (NFR 11)
        // Meta-info mag geen direct identificeerbare afspraakgegevens bevatten
        // maar bewaart wel: organizationId, provider, status — voldoende voor facturatie-overzicht
        int anonymizedNotifications = notificationRepository.anonymizeAppointmentIdsBefore(cutoff);
        log.info("Notificatie-appointmentIds geanonimiseerd (NFR 11): {}", anonymizedNotifications);
    }

    @Scheduled(cron = "0 30 2 * * *")
    public void deleteExpiredAppointments() {
        Instant cutoff = ZonedDateTime.now().minusYears(1).toInstant();
        long deletedAppointments = repository.deleteByEndBefore(cutoff);
        log.info("Verlopen afspraken verwijderd (>1 jaar): {}", deletedAppointments);
    }
}
