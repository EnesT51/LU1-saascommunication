package com.api.RestAPI.infrastructure.notification.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.RestAPI.infrastructure.notification.entities.Notification;
import com.api.RestAPI.domain.notification.enums.NotificationStatus;

@Repository
public interface JpaNotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByStatusAndScheduledAtBefore(NotificationStatus status, Instant now);
    long deleteByCreatedAtBefore(Instant cutoffDate);
}
