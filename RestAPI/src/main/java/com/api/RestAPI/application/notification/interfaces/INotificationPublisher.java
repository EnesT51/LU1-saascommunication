package com.api.RestAPI.application.notification.interfaces;

import java.util.UUID;

import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;
import com.api.RestAPI.infrastructure.notification.entities.Notification;

public interface INotificationPublisher {
    boolean publish(Notification notification, AppointmentEntity appointment, ProviderType providerType, UUID messageId);
}
