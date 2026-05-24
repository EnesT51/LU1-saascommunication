package com.api.RestAPI.infrastructure.notification.entities;

import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

import java.time.Instant;
import java.util.UUID;

import com.api.RestAPI.domain.notification.enums.NotificationStatus;
import com.api.RestAPI.domain.notification.enums.NotificationType;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String appointmentId;

    private String organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private Instant scheduledAt;

    private Instant sentAt;

    @Column(nullable = false)
    private int retryCount;

    private String provider;

    @Column(length = 1000)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Notification() {
    }

    public Notification(
            UUID id,
            String appointmentId,
            String organizationId,
            NotificationType type,
            NotificationStatus status,
            Instant scheduledAt,
            Instant sentAt,
            int retryCount,
            String provider,
            String failureReason
    ) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.organizationId = organizationId;
        this.type = type;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.sentAt = sentAt;
        this.retryCount = retryCount;
        this.provider = provider;
        this.failureReason = failureReason;
    }
    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getProvider() {
        return provider;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void markAsSent(String provider) {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
        this.provider = provider;
    }

    public void markAsFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
        this.retryCount++;
    }

    public void resetToPending() {
        this.status = NotificationStatus.PENDING;
    }

    public void markAsCancelled() {
        this.status = NotificationStatus.CANCELLED;
    }

    public void reschedule(Instant newScheduledAt) {
        this.scheduledAt = newScheduledAt;
    }
}
