package com.api.RestAPI.application.notification.interfaces;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.api.RestAPI.infrastructure.notification.entities.Notification;
import com.api.RestAPI.domain.notification.enums.NotificationStatus;
import com.api.RestAPI.presentation.notification.response.NotificationSummaryResponse;

public interface INotificationRepository {
    void save(Notification notification);
    void saveAll(List<Notification> notifications);
    Optional<Notification> findById(UUID id);
    List<Notification> findPendingNotifications(NotificationStatus status, Instant now);
    List<Notification> findFailedNotifications(NotificationStatus status, int maxRetries);
    List<Notification> findPendingForAppointment(String appointmentId);
    List<NotificationSummaryResponse> getSummary();
    long deleteByCreatedAtBefore(Instant cutoffDate);
    // NFR 11: anonymiseer appointmentId na 14 dagen (geen direct identificeerbare afspraakgegevens bewaren)
    int anonymizeAppointmentIdsBefore(Instant cutoff);
}
