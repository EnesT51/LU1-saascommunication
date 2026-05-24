package com.api.RestAPI.application.notification.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.api.RestAPI.application.notification.interfaces.IAppointmentFactory;
import com.api.RestAPI.infrastructure.notification.entities.Notification;
import com.api.RestAPI.domain.notification.enums.NotificationStatus;
import com.api.RestAPI.domain.notification.enums.NotificationType;

@Component
public class NotificationFactory implements IAppointmentFactory {
    
    public List<Notification> createNotifications(String appointmentId, Instant appointmentStart, String organizationId) {
        Notification notification1 = new Notification(
            UUID.randomUUID(),
            appointmentId,
            organizationId,
            NotificationType.REMINDER_24H,
            NotificationStatus.PENDING,
            appointmentStart.minus(24, ChronoUnit.HOURS),
            null,
            0,
            null,
            null
        );

        Notification notification2 = new Notification(
            UUID.randomUUID(),
            appointmentId,
            organizationId,
            NotificationType.REMINDER_1H,
            NotificationStatus.PENDING,
            appointmentStart.minus(1, ChronoUnit.HOURS),
            null,
            0,
            null,
            null
        );
        return List.of(notification1, notification2);
    }
}
