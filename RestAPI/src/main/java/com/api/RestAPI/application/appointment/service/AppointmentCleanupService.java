package com.api.RestAPI.application.appointment.service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.api.RestAPI.domain.appointment.Interface.AnonymizeAppointmentRepository;
import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AppointmentCleanupService {

    private final IAppointmentRepository repository;
    private final AnonymizeAppointmentRepository anonymizeRepository;

    public AppointmentCleanupService(
            IAppointmentRepository repository,
            AnonymizeAppointmentRepository anonymizeRepository
    ) {
        this.repository = repository;
        this.anonymizeRepository = anonymizeRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldAppointments() {

        Instant anonymizeCutoffDate = Instant.now().minus(14, ChronoUnit.DAYS);
        int anonymizedCount = anonymizeRepository.anonymizeAppointmentsEndedBefore(anonymizeCutoffDate);
        System.out.println("Old appointments anonymized: " + anonymizedCount);
    }
    @Scheduled(cron = "0 30 2 * * *")
    public void deleteExpiredAppointments() {
        Instant cutoffDate = ZonedDateTime.now().minusYears(1).toInstant();
        long deletedCount = repository.deleteByEndBefore(cutoffDate);
        System.out.println("Expired appointments deleted: " + deletedCount);
    }
}
