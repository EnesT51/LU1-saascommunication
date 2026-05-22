package com.api.RestAPI.application.appointment.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;

import jakarta.transaction.Transactional;

@Service
public class AppointmentCleanupService {

    private final IAppointmentRepository repository;

    public AppointmentCleanupService(
            IAppointmentRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldAppointments() {

        Instant cutoffDate = Instant.now().minus(14, ChronoUnit.DAYS);
        long deletedCount = repository.deleteByCreatedAtBefore(cutoffDate);
        System.out.println("Old appointments deleted: " + deletedCount);
    }
}
