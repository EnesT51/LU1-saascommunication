package com.api.RestAPI.infrastructure.notification.Repository;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.infrastructure.notification.entities.Notification;
import com.api.RestAPI.domain.notification.enums.NotificationStatus;

@Repository
public class NotificationRepositoryImpl implements INotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;

    public NotificationRepositoryImpl(JpaNotificationRepository jpaNotificationRepository) {
        this.jpaNotificationRepository = jpaNotificationRepository;
    }

    @Override
    public void save(Notification notification) {
        jpaNotificationRepository.save(notification);
    }

    @Override
    public void saveAll(List<Notification> notifications) {
        jpaNotificationRepository.saveAll(notifications);
    }

    @Override
    public List<Notification> findPendingNotifications(NotificationStatus status, Instant now) {
        return jpaNotificationRepository.findByStatusAndScheduledAtBefore(status, now);
    }

    @Override
    public List<Notification> findFailedNotifications(NotificationStatus status, int maxRetries) {
        return jpaNotificationRepository.findByStatusAndRetryCountLessThan(status, maxRetries);
    }
    
}
