package com.api.RestAPI.application.messageprovider.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.api.RestAPI.application.notification.interfaces.INotificationMetricsRecorder;
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
    private final INotificationMetricsRecorder metricsRecorder;

    public ProviderDispatcher(
            List<MessageProvider> providers,
            INotificationRepository notificationRepository,
            INotificationMetricsRecorder metricsRecorder
    ) {
        this.providers = providers;
        this.notificationRepository = notificationRepository;
        this.metricsRecorder = metricsRecorder;
    }

    public void dispatch(ProviderMessage message) {
        MessageProvider provider = providers.stream()
                .filter(p -> p.supports() == message.getProviderType())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No provider found for: %s", message.getProviderType())
                ));

        ProviderSendResult result = provider.send(message);

        String providerName = message.getProviderType().name();

        if (result.isSuccess()) {
            log.info("Bericht {} succesvol bezorgd via {}", message.getId(), message.getProviderType());
            updateNotification(message, true, null, providerName);
            metricsRecorder.recordSent(providerName);
            return;
        }

        log.warn("Bericht {} mislukt via {}: {}", message.getId(), message.getProviderType(), result.getErrorMessage());
        updateNotification(message, false, result.getErrorMessage(), providerName);
        metricsRecorder.recordFailed(providerName, "http_provider_mislukt");
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