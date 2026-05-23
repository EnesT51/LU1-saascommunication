package com.api.RestAPI.application.notification.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.api.RestAPI.application.notification.interfaces.INotificationRepository;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class NotificationCleanupService {
    
    private final INotificationRepository notificationRepository;

    public NotificationCleanupService(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldNotifications() {

        Instant cutoffDate = Instant.now().minus(1, ChronoUnit.YEARS);
        long deletedCount = notificationRepository.deleteByCreatedAtBefore(cutoffDate);
        
        System.out.println("Deleted " + deletedCount + " old notifications.");
    }
}
