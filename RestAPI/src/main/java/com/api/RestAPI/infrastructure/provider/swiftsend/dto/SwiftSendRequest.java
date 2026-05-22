package com.api.RestAPI.infrastructure.provider.swiftsend.dto;

import java.util.List;

public class SwiftSendRequest {

    private String type;
    private List<String> recipients;
    private String content;

    public SwiftSendRequest(String type, List<String> recipients, String content) {
        this.type = type;
        this.recipients = recipients;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public String getContent() {
        return content;
    }
}