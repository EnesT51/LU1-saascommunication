package com.api.RestAPI.domain.notification.enums;

public enum NotificationStatus {
    PENDING,
    DISPATCHED,  // in RabbitMQ queue, wacht op bevestiging van HTTP provider
    SENT,
    FAILED,
    CANCELLED
}
