package com.api.RestAPI.infrastructure.persistence.message.entity;

import com.api.RestAPI.domain.message.enums.MessageStatus;
import com.api.RestAPI.domain.message.enums.ProviderType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "provider_messages")
public class ProviderMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    private String recipient;

    @Column(length = 2000)
    private String content;

    private String subject;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    private String trackingId;

    @Column(length = 2000)
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public ProviderMessageEntity() {
    }

    public ProviderMessageEntity(ProviderType providerType, String recipient, String content, String subject, MessageStatus status) {
        this.providerType = providerType;
        this.recipient = recipient;
        this.content = content;
        this.subject = subject;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public ProviderType getProviderType() { return providerType; }
    public String getRecipient() { return recipient; }
    public String getContent() { return content; }
    public String getSubject() { return subject; }
    public MessageStatus getStatus() { return status; }
    public String getTrackingId() { return trackingId; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }

    public void markAsSent(String trackingId) {
        this.status = MessageStatus.SENT;
        this.trackingId = trackingId;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = MessageStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}