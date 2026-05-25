package com.api.RestAPI.application.messageprovider.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.api.RestAPI.application.notification.interfaces.INotificationMetricsRecorder;
import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.DeadLetterMessageQueuePublisher;
import com.api.RestAPI.domain.message.interfaces.MessageProvider;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.domain.message.model.ProviderSendResult;
import com.api.RestAPI.infrastructure.notification.entities.Notification;

@Service
public class ProviderDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ProviderDispatcher.class);
    private static final int MAX_ATTEMPTS_PER_PROVIDER = 3;

    private final List<MessageProvider> providers;
    private final INotificationRepository notificationRepository;
    private final INotificationMetricsRecorder metricsRecorder;
    private final OrganizationProviderMapper providerMapper;
    private final DeadLetterMessageQueuePublisher deadLetterPublisher;

    public ProviderDispatcher(
            List<MessageProvider> providers,
            INotificationRepository notificationRepository,
            INotificationMetricsRecorder metricsRecorder,
            OrganizationProviderMapper providerMapper,
            DeadLetterMessageQueuePublisher deadLetterPublisher
    ) {
        this.providers = providers;
        this.notificationRepository = notificationRepository;
        this.metricsRecorder = metricsRecorder;
        this.providerMapper = providerMapper;
        this.deadLetterPublisher = deadLetterPublisher;
    }

    public void dispatch(ProviderMessage message) {
        ProviderType primary = message.getProviderType();

        // Stap 1: probeer primary provider met 3 retries
        if (tryProviderWithRetries(message, primary)) {
            return;
        }

        // Stap 2: alle fallback providers af, elk 3 retries
        List<ProviderType> fallbacks = providerMapper.resolveFallbacks(primary);
        for (ProviderType fallback : fallbacks) {
            log.warn("Provider {} mislukt na {} pogingen, probeer fallback {}",
                    primary, MAX_ATTEMPTS_PER_PROVIDER, fallback);
            ProviderMessage fallbackMessage = withProvider(message, fallback);
            if (tryProviderWithRetries(fallbackMessage, fallback)) {
                return;
            }
        }

        // Stap 3: alle providers gefaald → DLQ + FAILED
        log.error("Bericht {} permanent mislukt na alle {} providers - naar DLQ",
                message.getId(), ProviderType.values().length);
        updateNotification(message, false, "Alle providers gefaald", primary.name());
        metricsRecorder.recordFailed(primary.name(), "alle_providers_mislukt");
        publishToDeadLetterQueue(message);
    }

    /**
     * Probeert het bericht via één specifieke provider met MAX_ATTEMPTS_PER_PROVIDER pogingen
     * en exponential backoff (1s, 2s, 4s).
     * Geeft true terug bij succes, false als alle pogingen mislukken.
     */
    private boolean tryProviderWithRetries(ProviderMessage message, ProviderType providerType) {
        MessageProvider provider = findProvider(providerType);
        String providerName = providerType.name();

        for (int attempt = 1; attempt <= MAX_ATTEMPTS_PER_PROVIDER; attempt++) {
            ProviderSendResult result = provider.send(message);

            if (result.isSuccess()) {
                log.info("Bericht {} succesvol bezorgd via {} (poging {}/{})",
                        message.getId(), providerType, attempt, MAX_ATTEMPTS_PER_PROVIDER);
                updateNotification(message, true, null, providerName);
                metricsRecorder.recordSent(providerName);
                return true;
            }

            log.warn("Poging {}/{} mislukt via {} voor bericht {}: {}",
                    attempt, MAX_ATTEMPTS_PER_PROVIDER, providerType, message.getId(), result.getErrorMessage());

            if (attempt < MAX_ATTEMPTS_PER_PROVIDER) {
                sleepWithBackoff(attempt);
            }
        }
        return false;
    }

    private MessageProvider findProvider(ProviderType providerType) {
        return providers.stream()
                .filter(p -> p.supports() == providerType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No provider found for: %s", providerType)
                ));
    }

    private ProviderMessage withProvider(ProviderMessage original, ProviderType newProvider) {
        return new ProviderMessage(
                newProvider,
                original.getRecipient(),
                original.getContent(),
                original.getSubject(),
                original.getId(),
                original.getNotificationId()
        );
    }

    private void sleepWithBackoff(int attempt) {
        try {
            long delay = 1000L * (1L << (attempt - 1));
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void publishToDeadLetterQueue(ProviderMessage message) {
        try {
            deadLetterPublisher.publishToDlq(message);
            log.warn("Bericht {} gepubliceerd naar notifications.dlq", message.getId());
        } catch (Exception e) {
            log.error("Kon bericht {} niet naar DLQ sturen: {}", message.getId(), e.getMessage());
        }
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
