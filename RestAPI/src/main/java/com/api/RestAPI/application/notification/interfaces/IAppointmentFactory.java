package com.api.RestAPI.application.notification.interfaces;

import java.time.Instant;
import java.util.List;

import com.api.RestAPI.infrastructure.notification.entities.Notification;

public interface IAppointmentFactory {
    public List<Notification> createNotifications(String appointmentId, Instant appointmentStart);
}
