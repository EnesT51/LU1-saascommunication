package com.api.RestAPI.domain.message.model;
import com.api.RestAPI.domain.message.enums.ProviderType;
import java.util.UUID;
public class ProviderMessage {

    private ProviderType providerType;
    private String recipient;
    private String content;
    private String subject;
    private UUID id;
    private UUID notificationId;
    public ProviderMessage() {
    }

    public ProviderMessage(
            ProviderType providerType,
            String recipient,
            String content,
            String subject
    ) {
        this(providerType, recipient, content, subject, null);
    }

    public ProviderMessage(
            ProviderType providerType,
            String recipient,
            String content,
            String subject,
            UUID id
    ) {
        this.providerType = providerType;
        this.recipient = recipient;
        this.content = content;
        this.subject = subject;
        this.id = id;
    }

    public ProviderMessage(
            ProviderType providerType,
            String recipient,
            String content,
            String subject,
            UUID id,
            UUID notificationId
    ) {
        this(providerType, recipient, content, subject, id);
        this.notificationId = notificationId;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(ProviderType providerType) {
        this.providerType = providerType;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }
}
