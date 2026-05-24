package com.api.RestAPI.application.notification.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.api.RestAPI.domain.notification.enums.NotificationStatus;
import com.api.RestAPI.domain.notification.enums.NotificationType;
import com.api.RestAPI.infrastructure.notification.entities.Notification;

@DisplayName("NotificationFactory Tests")
class NotificationFactoryTest {

    private final NotificationFactory notificationFactory = new NotificationFactory();

    @Test
    @DisplayName("Should create 24 hour and 1 hour reminders")
    void createNotificationsCreatesExpectedReminders() {
        Instant appointmentStart = Instant.parse("2026-05-22T10:00:00Z");

        List<Notification> result = notificationFactory.createNotifications("appointment-123", appointmentStart);

        assertEquals(2, result.size());
        assertReminder(result.get(0), NotificationType.REMINDER_24H, appointmentStart.minus(24, ChronoUnit.HOURS));
        assertReminder(result.get(1), NotificationType.REMINDER_1H, appointmentStart.minus(1, ChronoUnit.HOURS));
    }

    private void assertReminder(Notification notification, NotificationType type, Instant scheduledAt) {
        assertNotNull(notification.getId());
        assertEquals("appointment-123", notification.getAppointmentId());
        assertEquals(type, notification.getType());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertEquals(scheduledAt, notification.getScheduledAt());
        assertEquals(0, notification.getRetryCount());
    }
}
