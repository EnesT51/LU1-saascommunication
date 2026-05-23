package com.api.RestAPI.presentation.messageprovider.response;

import com.api.RestAPI.domain.message.enums.MessageStatus;
import com.api.RestAPI.domain.message.enums.ProviderType;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProviderMessageResponse {

    private UUID id;
    private ProviderType providerType;
    private String recipient;
    private MessageStatus status;
    private String trackingId;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public ProviderMessageResponse(UUID id, ProviderType providerType, String recipient, MessageStatus status,
                                   String trackingId, String errorMessage, LocalDateTime createdAt, LocalDateTime sentAt) {
        this.id = id;
        this.providerType = providerType;
        this.recipient = recipient;
        this.status = status;
        this.trackingId = trackingId;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    public UUID getId() { return id; }
    public ProviderType getProviderType() { return providerType; }
    public String getRecipient() { return recipient; }
    public MessageStatus getStatus() { return status; }
    public String getTrackingId() { return trackingId; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }
}