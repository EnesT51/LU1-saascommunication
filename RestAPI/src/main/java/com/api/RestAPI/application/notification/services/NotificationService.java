package com.api.RestAPI.application.notification.services;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.api.RestAPI.application.notification.interfaces.INotificationProcessor;
import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.notification.enums.NotificationStatus;
import com.api.RestAPI.infrastructure.notification.entities.Notification;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int MAX_SCHEDULE_RETRIES = 3;

    private final INotificationRepository notificationRepository;
    private final INotificationProcessor notificationProcessor;

    public NotificationService(
            INotificationRepository notificationRepository,
            INotificationProcessor notificationProcessor) {
        this.notificationRepository = notificationRepository;
        this.notificationProcessor = notificationProcessor;
    }

    @Scheduled(fixedRate = 1800000)
    public void retryFailedNotifications() {
        try {
            List<Notification> failed = notificationRepository
                    .findFailedNotifications(NotificationStatus.FAILED, MAX_SCHEDULE_RETRIES);

            if (failed.isEmpty()) {
                return;
            }

            log.info("Retrying {} failed notification(s)", failed.size());
            for (Notification notification : failed) {
                log.warn("Resetting FAILED notification {} (attempt {}/{}) back to PENDING",
                        notification.getId(), notification.getRetryCount() + 1, MAX_SCHEDULE_RETRIES);
                notification.resetToPending();
                notificationRepository.save(notification);
            }
        } catch (Exception e) {
            log.error("Error during failed notification retry: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 5000)
    public void processPendingNotifications() {
        try {
            List<Notification> notifications = notificationRepository
                    .findPendingNotifications(NotificationStatus.PENDING, Instant.now());

            for (Notification notification : notifications) {
                notificationProcessor.process(notification);
            }
        } catch (Exception e) {
            log.error("Fout bij ophalen notificaties: {}", e.getMessage());
        }
    }
}
