package com.api.RestAPI.application.appointment.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.appointment.Interface.AnonymizeAppointmentRepository;
import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;

class AppointmentCleanupServiceTest {

    private final IAppointmentRepository appointmentRepository =
            org.mockito.Mockito.mock(IAppointmentRepository.class);
    private final AnonymizeAppointmentRepository anonymizeRepository =
            org.mockito.Mockito.mock(AnonymizeAppointmentRepository.class);
    private final INotificationRepository notificationRepository =
            org.mockito.Mockito.mock(INotificationRepository.class);
    private final AppointmentCleanupService cleanupService =
            new AppointmentCleanupService(appointmentRepository, anonymizeRepository, notificationRepository);

    @Test
    @DisplayName("Should anonymize appointments that ended more than 14 days ago")
    void cleanupOldAppointmentsUsesAppointmentEndCutoff() {
        when(anonymizeRepository.anonymizeAppointmentsEndedBefore(org.mockito.ArgumentMatchers.any()))
                .thenReturn(3);

        assertDoesNotThrow(cleanupService::cleanupOldAppointments);

        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(anonymizeRepository).anonymizeAppointmentsEndedBefore(cutoffCaptor.capture());
        assertCutoffAround(cutoffCaptor.getValue(), Duration.ofDays(14));
    }

    @Test
    @DisplayName("Should delete appointments that ended more than one year ago")
    void deleteExpiredAppointmentsUsesOneYearCutoff() {
        when(appointmentRepository.deleteByEndBefore(org.mockito.ArgumentMatchers.any()))
                .thenReturn(2L);

        assertDoesNotThrow(cleanupService::deleteExpiredAppointments);

        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(appointmentRepository).deleteByEndBefore(cutoffCaptor.capture());
        assertCutoffAround(cutoffCaptor.getValue(), Duration.ofDays(365));
    }

    private void assertCutoffAround(Instant actualCutoff, Duration expectedAge) {
        Instant now = Instant.now();
        Instant earliestExpected = now.minus(expectedAge).minus(Duration.ofSeconds(5));
        Instant latestExpected = now.minus(expectedAge).plus(Duration.ofSeconds(5));

        assertTrue(
                !actualCutoff.isBefore(earliestExpected) && !actualCutoff.isAfter(latestExpected),
                "Expected cutoff around " + expectedAge + " ago, but was " + actualCutoff
        );
    }
}
