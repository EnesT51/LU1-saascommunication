package com.api.RestAPI.application.notification.interfaces;

import java.time.Instant;
import java.util.List;

import com.api.RestAPI.infrastructure.notification.entities.Notification;
import com.api.RestAPI.domain.notification.enums.NotificationStatus;

public interface INotificationRepository {
    void save(Notification notification);
    void saveAll(List<Notification> notifications);
    List<Notification> findPendingNotifications(NotificationStatus status, Instant now);
}
