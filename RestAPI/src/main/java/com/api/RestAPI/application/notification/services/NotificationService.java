package com.api.RestAPI.application.notification.services;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.infrastructure.notification.entities.Notification;
import com.api.RestAPI.domain.notification.enums.NotificationStatus;

@Service
public class NotificationService {
    
    private final INotificationRepository notificationRepository;

    public NotificationService(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(fixedRate = 5000)
    public void processPendingNotifications() {

        try{
            List<Notification> notifications = notificationRepository.findPendingNotifications(NotificationStatus.PENDING, Instant.now());

            for (Notification notification : notifications) {

                notificationRepository.save(notification);
            }
            
        }catch(Exception e){
            System.out.println("Error processing notifications: " + e.getMessage());
        }
    }
}
