package com.api.RestAPI.domain.message.model;
import com.api.RestAPI.domain.message.enums.ProviderType;
public class ProviderMessage {

    private ProviderType providerType;
    private String recipient;
    private String content;
    private String subject;

    public ProviderMessage() {
    }

    public ProviderMessage(
            ProviderType providerType,
            String recipient,
            String content,
            String subject
    ) {
        this.providerType = providerType;
        this.recipient = recipient;
        this.content = content;
        this.subject = subject;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public String getSubject() {
        return subject;
    }
}