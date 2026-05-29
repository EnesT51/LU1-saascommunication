package com.api.RestAPI.infrastructure.rabbitmq.consumer;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.infrastructure.notification.entities.Notification;
import com.api.RestAPI.infrastructure.rabbitmq.config.RabbitMQConfig;

@Component
public class DeadLetterProviderConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterProviderConsumer.class);

    private final INotificationRepository notificationRepository;

    public DeadLetterProviderConsumer(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @RabbitListener(queues = {
            RabbitMQConfig.SWIFTSEND_DLQ,
            RabbitMQConfig.SECUREPOST_DLQ,
            RabbitMQConfig.LEGACYLINK_DLQ,
            RabbitMQConfig.ASYNCFLOW_DLQ
    })
    public void consumeDeadLetter(ProviderMessage message) {
        log.error("Bericht {} permanent mislukt via {} — in dead letter queue",
                message.getId(), message.getProviderType());

        if (message.getNotificationId() != null) {
            Optional<Notification> opt = notificationRepository.findById(message.getNotificationId());
            opt.ifPresent(notification -> {
                notification.markAsFailed("Permanent mislukt — dead letter queue");
                notificationRepository.save(notification);
                log.warn("Notificatie {} teruggezet naar FAILED via DLQ", message.getNotificationId());
            });
        }
    }
}