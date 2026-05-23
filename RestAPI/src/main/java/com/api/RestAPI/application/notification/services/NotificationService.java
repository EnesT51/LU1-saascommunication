package com.api.RestAPI.application.notification.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.api.RestAPI.application.messageprovider.service.OrganizationProviderMapper;
import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;
import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.MessageQueuePublisher;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.domain.notification.enums.NotificationStatus;
import com.api.RestAPI.domain.notification.enums.NotificationType;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;
import com.api.RestAPI.infrastructure.notification.entities.Notification;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int MAX_RETRIES = 3;
    private static final int FALLBACK_ATTEMPTS = 1;

    private final INotificationRepository notificationRepository;
    private final IAppointmentRepository appointmentRepository;
    private final OrganizationProviderMapper providerMapper;
    private final MessageQueuePublisher messageQueuePublisher;
    private final MeterRegistry meterRegistry;

    public NotificationService(
            INotificationRepository notificationRepository,
            IAppointmentRepository appointmentRepository,
            OrganizationProviderMapper providerMapper,
            MessageQueuePublisher messageQueuePublisher,
            MeterRegistry meterRegistry) {
        this.notificationRepository = notificationRepository;
        this.appointmentRepository = appointmentRepository;
        this.providerMapper = providerMapper;
        this.messageQueuePublisher = messageQueuePublisher;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 5000)
    public void processPendingNotifications() {
        try {
            List<Notification> notifications = notificationRepository
                    .findPendingNotifications(NotificationStatus.PENDING, Instant.now());

            for (Notification notification : notifications) {
                processNotification(notification);
            }
        } catch (Exception e) {
            log.error("Fout bij ophalen notificaties: {}", e.getMessage());
        }
    }

    private void processNotification(Notification notification) {
        Optional<AppointmentEntity> appointmentOpt = appointmentRepository
                .findByAppointmentId(notification.getAppointmentId());

        if (appointmentOpt.isEmpty()) {
            log.warn("Afspraak niet gevonden voor notificatie {}", notification.getId());
            notification.markAsFailed("Afspraak niet gevonden");
            notificationRepository.save(notification);
            recordFailed("unknown", "afspraak_niet_gevonden");
            return;
        }

        AppointmentEntity appointment = appointmentOpt.get();

        if (appointment.getPatientPhoneNumber() == null || appointment.getPatientPhoneNumber().isBlank()) {
            log.warn("Geen telefoonnummer voor afspraak {}", appointment.getAppointmentId());
            notification.markAsFailed("Geen telefoonnummer");
            notificationRepository.save(notification);
            recordFailed("unknown", "geen_telefoonnummer");
            return;
        }

        UUID messageId = UUID.randomUUID();

        ProviderType primary = providerMapper.resolvePrimary(appointment.getOrganizationId());
        boolean sent = tryPublish(notification, appointment, primary, messageId, MAX_RETRIES);

        if (!sent) {
            ProviderType fallback = providerMapper.resolveFallback(primary);
            log.warn("Primaire provider {} mislukt, probeer fallback {}", primary, fallback);
            sent = tryPublish(notification, appointment, fallback, messageId, FALLBACK_ATTEMPTS);
        }

        if (!sent) {
            log.error("Notificatie {} permanent mislukt na primary + fallback", notification.getId());
            recordFailed(primary.name(), "alle_providers_mislukt");
        }
    }

    private boolean tryPublish(Notification notification, AppointmentEntity appointment,
            ProviderType providerType, UUID messageId, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ProviderMessage message = buildMessage(appointment, providerType, notification.getType(), messageId);
                messageQueuePublisher.publish(message);

                notification.markAsSent(providerType.name());
                notificationRepository.save(notification);

                // Metric: verstuurd per provider
                recordSent(providerType.name());

                log.info("Notificatie {} verstuurd via {} (poging {}/{}) voor org {}",
                        notification.getId(), providerType, attempt, maxAttempts, appointment.getOrganizationId());
                return true;

            } catch (Exception e) {
                log.warn("Poging {}/{} mislukt voor notificatie {} via {}: {}",
                        attempt, maxAttempts, notification.getId(), providerType, e.getMessage());

                if (attempt < maxAttempts) {
                    sleepWithBackoff(attempt);
                } else {
                    notification.markAsFailed(providerType + " mislukt: " + e.getMessage());
                    notificationRepository.save(notification);

                    // Metric: mislukt per provider
                    recordFailed(providerType.name(), "publish_mislukt");
                }
            }
        }
        return false;
    }

    private void recordSent(String provider) {
        Counter.builder("notifications_sent_total")
                .description("Aantal succesvol verstuurde notificaties")
                .tag("provider", provider)
                .register(meterRegistry)
                .increment();
    }

    private void recordFailed(String provider, String reason) {
        Counter.builder("notifications_failed_total")
                .description("Aantal mislukte notificaties")
                .tag("provider", provider)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    private ProviderMessage buildMessage(AppointmentEntity appointment, ProviderType providerType,
            NotificationType type, UUID messageId) {
        String tijdLabel = type == NotificationType.REMINDER_24H ? "24 uur" : "1 uur";
        String content = String.format(
                "Beste patient, u heeft over %s een afspraak. Afspraak ID: %s.",
                tijdLabel, appointment.getAppointmentId());

        return new ProviderMessage(providerType, appointment.getPatientPhoneNumber(),
                content, "Afspraak herinnering", messageId);
    }

    private void sleepWithBackoff(int attempt) {
        try {
            long delay = 1000L * (1L << (attempt - 1)); // 1s, 2s, 4s
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
