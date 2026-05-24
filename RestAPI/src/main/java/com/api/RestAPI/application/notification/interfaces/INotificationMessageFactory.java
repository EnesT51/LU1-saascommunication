package com.api.RestAPI.application.notification.interfaces;

import java.util.UUID;

import com.api.RestAPI.domain.message.enums.ProviderType;
import com.api.RestAPI.domain.message.model.ProviderMessage;
import com.api.RestAPI.domain.notification.enums.NotificationType;
import com.api.RestAPI.infrastructure.appointment.persistence.entities.AppointmentEntity;

public interface INotificationMessageFactory {
    ProviderMessage create(AppointmentEntity appointment, ProviderType providerType,
            NotificationType notificationType, UUID messageId);
}
