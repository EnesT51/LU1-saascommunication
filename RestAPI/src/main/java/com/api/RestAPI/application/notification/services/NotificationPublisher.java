package com.api.RestAPI.application.notification.services;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.api.RestAPI.application.notification.interfaces.INotificationMessageFactory;
import com.api.RestAPI.application.notification.interfaces.INotificationMetricsRecorder;
import com.api.RestAPI.application.notification.interfaces.INotificationPublisher;
import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.MessageQueuePublisher;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;
import com.api.RestAPI.infrastructure.notification.entities.Notification;

@Service
public class NotificationPublisher implements INotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);
    private static final int MAX_RETRIES = 3;

    private final MessageQueuePublisher messageQueuePublisher;
    private final INotificationMessageFactory messageFactory;
    private final INotificationRepository notificationRepository;
    private final INotificationMetricsRecorder metricsRecorder;

    public NotificationPublisher(
            MessageQueuePublisher messageQueuePublisher,
            INotificationMessageFactory messageFactory,
            INotificationRepository notificationRepository,
            INotificationMetricsRecorder metricsRecorder) {
        this.messageQueuePublisher = messageQueuePublisher;
        this.messageFactory = messageFactory;
        this.notificationRepository = notificationRepository;
        this.metricsRecorder = metricsRecorder;
    }

    @Override
    public boolean publish(Notification notification, AppointmentEntity appointment,
            ProviderType providerType, UUID messageId) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                ProviderMessage message = messageFactory.create(appointment, providerType,
                        notification.getType(), messageId);
                messageQueuePublisher.publish(message);

                notification.markAsSent(providerType.name());
                notificationRepository.save(notification);
                metricsRecorder.recordSent(providerType.name());

                log.info("Notificatie {} verstuurd via {} (poging {}/{}) voor org {}",
                        notification.getId(), providerType, attempt, MAX_RETRIES, appointment.getOrganizationId());
                return true;
            } catch (Exception e) {
                log.warn("Poging {}/{} mislukt voor notificatie {} via {}: {}",
                        attempt, MAX_RETRIES, notification.getId(), providerType, e.getMessage());

                if (attempt < MAX_RETRIES) {
                    sleepWithBackoff(attempt);
                } else {
                    notification.markAsFailed(providerType + " mislukt: " + e.getMessage());
                    notificationRepository.save(notification);
                    metricsRecorder.recordFailed(providerType.name(), "publish_mislukt");
                }
            }
        }
        return false;
    }

    private void sleepWithBackoff(int attempt) {
        try {
            long delay = 1000L * (1L << (attempt - 1));
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
