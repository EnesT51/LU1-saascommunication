package com.api.RestAPI.application.notification.services;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.api.RestAPI.application.notification.interfaces.INotificationRepository;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class NotificationCleanupService {

    private static final Logger log = LoggerFactory.getLogger(NotificationCleanupService.class);

    private final INotificationRepository notificationRepository;

    public NotificationCleanupService(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldNotifications() {
        Instant cutoffDate = ZonedDateTime.now().minusYears(1).toInstant();
        long deletedCount = notificationRepository.deleteByCreatedAtBefore(cutoffDate);
        log.info("Verlopen notificaties verwijderd (>1 jaar): {}", deletedCount);
    }
}
