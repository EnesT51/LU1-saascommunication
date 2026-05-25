package com.api.RestAPI.application.notification.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.api.RestAPI.application.messageprovider.service.OrganizationProviderMapper;
import com.api.RestAPI.application.notification.interfaces.INotificationMessageFactory;
import com.api.RestAPI.application.notification.interfaces.INotificationMetricsRecorder;
import com.api.RestAPI.application.notification.interfaces.INotificationProcessor;
import com.api.RestAPI.application.notification.interfaces.INotificationPublisher;
import com.api.RestAPI.application.notification.interfaces.INotificationRepository;
import com.api.RestAPI.domain.appointment.Interface.IAppointmentRepository;
import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.interfaces.DeadLetterMessageQueuePublisher;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;
import com.api.RestAPI.infrastructure.notification.entities.Notification;

@Service
public class NotificationProcessor implements INotificationProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationProcessor.class);

    private final IAppointmentRepository appointmentRepository;
    private final INotificationRepository notificationRepository;
    private final OrganizationProviderMapper providerMapper;
    private final INotificationPublisher notificationPublisher;
    private final INotificationMessageFactory messageFactory;
    private final DeadLetterMessageQueuePublisher deadLetterPublisher;
    private final INotificationMetricsRecorder metricsRecorder;

    public NotificationProcessor(
            IAppointmentRepository appointmentRepository,
            INotificationRepository notificationRepository,
            OrganizationProviderMapper providerMapper,
            INotificationPublisher notificationPublisher,
            INotificationMessageFactory messageFactory,
            DeadLetterMessageQueuePublisher deadLetterPublisher,
            INotificationMetricsRecorder metricsRecorder) {
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
        this.providerMapper = providerMapper;
        this.notificationPublisher = notificationPublisher;
        this.messageFactory = messageFactory;
        this.deadLetterPublisher = deadLetterPublisher;
        this.metricsRecorder = metricsRecorder;
    }

    @Override
    public void process(Notification notification) {
        Optional<AppointmentEntity> appointmentOpt = appointmentRepository
                .findByAppointmentId(notification.getAppointmentId());

        if (appointmentOpt.isEmpty()) {
            failNotification(notification, "Afspraak niet gevonden", "afspraak_niet_gevonden");
            return;
        }

        AppointmentEntity appointment = appointmentOpt.get();

        if (appointment.getPatientPhoneNumber() == null || appointment.getPatientPhoneNumber().isBlank()) {
            log.warn("Geen telefoonnummer voor afspraak {}", appointment.getAppointmentId());
            failNotification(notification, "Geen telefoonnummer", "geen_telefoonnummer");
            return;
        }

        UUID messageId = UUID.randomUUID();
        ProviderType primary = providerMapper.resolvePrimary(appointment.getOrganizationId());

        if (publishWithFallbacks(notification, appointment, primary, messageId)) {
            return;
        }

        log.error("Notificatie {} permanent mislukt na alle {} providers - naar DLQ",
                notification.getId(), ProviderType.values().length);
        metricsRecorder.recordFailed(primary.name(), "alle_providers_mislukt");
        publishToDeadLetterQueue(notification, appointment, primary, messageId);
    }

    private boolean publishWithFallbacks(Notification notification, AppointmentEntity appointment,
            ProviderType primary, UUID messageId) {
        if (notificationPublisher.publish(notification, appointment, primary, messageId)) {
            return true;
        }

        List<ProviderType> fallbacks = providerMapper.resolveFallbacks(primary);
        for (ProviderType fallback : fallbacks) {
            log.warn("Provider {} mislukt, probeer fallback {}", primary, fallback);
            if (notificationPublisher.publish(notification, appointment, fallback, messageId)) {
                return true;
            }
        }
        return false;
    }

    private void failNotification(Notification notification, String userReason, String metricReason) {
        log.warn("{} voor notificatie {}", userReason, notification.getId());
        notification.markAsFailed(userReason);
        notificationRepository.save(notification);
        metricsRecorder.recordFailed("unknown", metricReason);
    }

    private void publishToDeadLetterQueue(Notification notification, AppointmentEntity appointment,
            ProviderType primary, UUID messageId) {
        try {
            ProviderMessage dlqMessage = messageFactory.create(appointment, primary, notification.getType(), messageId, notification.getId());
            deadLetterPublisher.publishToDlq(dlqMessage);
            log.warn("Notificatie {} gepubliceerd naar notifications.dlq", notification.getId());
        } catch (Exception e) {
            log.error("Kon notificatie {} niet naar DLQ sturen: {}", notification.getId(), e.getMessage());
        }
    }
}
