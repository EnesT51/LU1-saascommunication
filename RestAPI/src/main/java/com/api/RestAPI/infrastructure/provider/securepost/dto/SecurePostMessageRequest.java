package com.api.RestAPI.infrastructure.provider.securepost.dto;

public class SecurePostMessageRequest {

    private String format;
    private String recipient;
    private String body;
    private String subject;

    public SecurePostMessageRequest(String format, String recipient, String body, String subject) {
        this.format = format;
        this.recipient = recipient;
        this.body = body;
        this.subject = subject;
    }

    public String getFormat() { return format; }
    public String getRecipient() { return recipient; }
    public String getBody() { return body; }
    public String getSubject() { return subject; }
}