package com.api.RestAPI.application.notification.services;

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

class NotificationCleanupServiceTest {

    private final INotificationRepository notificationRepository =
            org.mockito.Mockito.mock(INotificationRepository.class);
    private final NotificationCleanupService cleanupService =
            new NotificationCleanupService(notificationRepository);

    @Test
    @DisplayName("Should delete notifications created more than one year ago")
    void cleanupOldNotificationsUsesOneYearCutoff() {
        when(notificationRepository.deleteByCreatedAtBefore(org.mockito.ArgumentMatchers.any()))
                .thenReturn(4L);

        assertDoesNotThrow(cleanupService::cleanupOldNotifications);

        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(notificationRepository).deleteByCreatedAtBefore(cutoffCaptor.capture());

        Instant now = Instant.now();
        Instant earliestExpected = now.minus(Duration.ofDays(365)).minus(Duration.ofSeconds(5));
        Instant latestExpected = now.minus(Duration.ofDays(365)).plus(Duration.ofSeconds(5));
        Instant actualCutoff = cutoffCaptor.getValue();

        assertTrue(
                !actualCutoff.isBefore(earliestExpected) && !actualCutoff.isAfter(latestExpected),
                "Expected cutoff around one year ago, but was " + actualCutoff
        );
    }
}
