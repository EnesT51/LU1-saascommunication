package com.api.RestAPI.application.notification.interfaces;

import com.api.RestAPI.infrastructure.notification.entities.Notification;

public interface INotificationProcessor {
    void process(Notification notification);
}
