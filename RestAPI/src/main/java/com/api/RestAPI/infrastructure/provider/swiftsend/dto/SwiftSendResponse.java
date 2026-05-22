package com.api.RestAPI.infrastructure.provider.swiftsend.dto;

import java.util.List;

public class SwiftSendResponse {

    private boolean success;
    private String messageId;
    private List<String> failedRecipients;
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public String getMessageId() {
        return messageId;
    }

    public List<String> getFailedRecipients() {
        return failedRecipients;
    }

    public String getError() {
        return error;
    }
}