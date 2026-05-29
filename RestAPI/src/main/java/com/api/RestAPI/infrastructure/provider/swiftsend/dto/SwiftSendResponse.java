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

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public List<String> getFailedRecipients() {
        return failedRecipients;
    }

    public void setFailedRecipients(List<String> failedRecipients) {
        this.failedRecipients = failedRecipients;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}