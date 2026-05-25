package com.api.RestAPI.application.messageprovider.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.message.interfaces.MessageProvider;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.domain.message.model.ProviderSendResult;
import com.api.RestAPI.infrastructure.notification.entities.Notification;

@Service
public class ProviderDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ProviderDispatcher.class);

    private final List<MessageProvider> providers;
    private final INotificationRepository notificationRepository;

    public ProviderDispatcher(
            List<MessageProvider> providers,
            INotificationRepository notificationRepository
    ) {
        this.providers = providers;
        this.notificationRepository = notificationRepository;
    }

    public void dispatch(ProviderMessage message) {
        MessageProvider provider = providers.stream()
                .filter(p -> p.supports() == message.getProviderType())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No provider found for: %s", message.getProviderType())
                ));

        ProviderSendResult result = provider.send(message);

        if (result.isSuccess()) {
            log.info("Bericht {} succesvol bezorgd via {}", message.getId(), message.getProviderType());
            updateNotification(message, true, null, message.getProviderType().name());
            return;
        }

        log.warn("Bericht {} mislukt via {}: {}", message.getId(), message.getProviderType(), result.getErrorMessage());
        updateNotification(message, false, result.getErrorMessage(), message.getProviderType().name());
    }

    private void updateNotification(ProviderMessage message, boolean success, String errorMessage, String provider) {
        if (message.getNotificationId() == null) return;

        Optional<Notification> opt = notificationRepository.findById(message.getNotificationId());
        if (opt.isEmpty()) {
            log.warn("Notificatie {} niet gevonden voor status update", message.getNotificationId());
            return;
        }

        Notification notification = opt.get();
        if (success) {
            notification.markAsSent(provider);
        } else {
            notification.markAsFailed(errorMessage);
        }
        notificationRepository.save(notification);
    }
}